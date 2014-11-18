package hu.blackbelt.welcometoosgi.transaction.api;

import hu.blackbelt.internal.ByteArrayClassLoader;
import javassist.*;
import javassist.bytecode.AccessFlag;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.annotation.EnumMemberValue;
import org.apache.felix.scr.annotations.Activate;
import org.osgi.framework.hooks.weaving.WeavingHook;
import org.osgi.framework.hooks.weaving.WovenClass;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.annotation.Annotation;


/**
 * Instrumenting the class with transactional behaviour. All implements that declared on instance's interfaces
 * will be instrumented. To instrument a method the following requirement have to fullfilled:
 *  - The MANFIEST.MF have to contain the Blackbelt-Transactional tag with the type of TransactionalManager on bundle level
 *  - The class have to annotated with Transactional
 *  - When the method contains @Transactional annotation, override the class defined annotation
 */
public class TransactionalMethodCallsWeavingHook implements WeavingHook {

	public static final String BLACKBELT_WELCOMETOOSGI_TRANSACTIONAL = "Blackbelt-WelcomeToOSGi-Transactional";
	private final Logger log = LoggerFactory.getLogger(getClass());

	@Activate
	protected void activate(ComponentContext context) {
		log.info("Create Transactional Weaving hook");
	}

	/**
	 * Wave the class as transactional when contains Transactional as class annotation
	 * @param wovenClass
	 */
	@Override
	public void weave(WovenClass wovenClass) {
		if(!wovenClass.getClassName().equals(this.getClass().getName())) {
			Class classToIntstrument = getClassToIntstrument(wovenClass);
			if (classToIntstrument != null) {

				// Get the transactional context from MANFEST.MF
				String transactionalContext = (String)wovenClass.getBundleWiring().getBundle().getHeaders().get(BLACKBELT_WELCOMETOOSGI_TRANSACTIONAL);
				log.info("Instrumenting {}", wovenClass.getClassName());

				try {
					ClassPool cp = ClassPool.getDefault();
					ClassPath classPath = new ClassClassPath(classToIntstrument);
					cp.insertClassPath(classPath);
					CtClass cc = cp.get(wovenClass.getClassName());
					if (cc.isFrozen()) {
						cc.detach();
						cc = cp.get(wovenClass.getClassName());
					}

					if (!cc.isFrozen()) {
						// Get class interface. If interface contain method, the method is automatically transactional and
						// inherots class annotation
						Transactional classTransactionalAnnotation = (Transactional) classToIntstrument.getAnnotation(Transactional.class);
						Transactional.TxType txType = classTransactionalAnnotation.value();

						for (CtMethod m : cc.getDeclaredMethods()) {
							if (m.hasAnnotation(Transactional.class)) {
								MethodInfo minfo = m.getMethodInfo();
								AnnotationsAttribute attr = (AnnotationsAttribute)
										minfo.getAttribute(AnnotationsAttribute.visibleTag);
								javassist.bytecode.annotation.Annotation an = attr.getAnnotation(Transactional.class.getName());
								if (an != null) {
									txType = Transactional.TxType.valueOf(((EnumMemberValue) an.getMemberValue("value")).getValue());
								}
							}

							// Search for method
							boolean foundInterfaceMethod = false;

							CtClass[] interfaces = null;
							try {
								interfaces = m.getDeclaringClass().getInterfaces();
								for (CtClass iface : interfaces) {
									try {
										if (iface.getMethod(m.getName(), m.getSignature()) != null) {
											foundInterfaceMethod = true;
										}
									} catch (NotFoundException e1) {
									}
								}
							} catch (NotFoundException e) {
							}

							int methodAccess = m.getMethodInfo().getAccessFlags();

							boolean methodIsAccessible = (AccessFlag.isPrivate(methodAccess) || AccessFlag.isProtected(methodAccess) || AccessFlag.isPublic(methodAccess)) && !Modifier.isNative(methodAccess);
							if (m.hasAnnotation(Transactional.class) ||
									foundInterfaceMethod) {
								if (methodIsAccessible && txType != Transactional.TxType.NOT_SUPPORTED) {
									m.insertBefore(TransactionManagerLocator.class.getName() + ".getTransactionManager(\"" + transactionalContext + "\").beginTransaction("+Transactional.TxType.class.getName()+"." + txType.toString() + ");");
									m.insertAfter(TransactionManagerLocator.class.getName()+".getTransactionManager(\"" + transactionalContext + "\").commitTransaction("+Transactional.TxType.class.getName()+"." + txType.toString() + ");");
									CtClass etype = cp.get(Throwable.class.getName());
									m.addCatch("{ " + TransactionManagerLocator.class.getName() + ".getTransactionManager(\"" + transactionalContext + "\").rollbackTransaction("+Transactional.TxType.class.getName()+"." + txType.toString() + "); throw $e; }", etype);
								}
							}
						}
					}
					wovenClass.setBytes(cc.toBytecode());
					cp.removeClassPath(classPath);
				} catch (NotFoundException e) {
					log.error("Could not instrument class: "+wovenClass.getClassName(), e);
					throw new RuntimeException("Could not instrument class: "+wovenClass.getClassName(), e);
				} catch (CannotCompileException e) {
					log.error("Could not instrument class: "+wovenClass.getClassName(), e);
					throw new RuntimeException("Could not instrument class: "+wovenClass.getClassName(), e);
				} catch (IOException e) {
					log.error("Could not instrument class: "+wovenClass.getClassName(), e);
					throw new RuntimeException("Could not instrument class: "+wovenClass.getClassName(), e);
				}
			}
		}
	}

	/**
	 * If the woven class contains @Transactional annotation, it is handled as Transactional class
	 * @param wovenClass
	 * @return The class have to be intrumented
	 */
	private Class getClassToIntstrument(WovenClass wovenClass) {


		if (wovenClass.getBundleWiring().getBundle().getHeaders().get(BLACKBELT_WELCOMETOOSGI_TRANSACTIONAL) == null) {
			return null;
		}

		Class originalClass = originalClass = new ByteArrayClassLoader(wovenClass.getBundleWiring().getClassLoader())
				.findClass(wovenClass.getClassName(), wovenClass.getBytes());

		if (originalClass != null) {
			Annotation[] annotations = originalClass.getAnnotations();

			if (annotations != null) {
				for (Annotation annotation : annotations) {
					if (annotation instanceof Transactional) {
						Transactional transactional = (Transactional) annotation;
						log.debug("Transactional annotation found: " + transactional.value());
						return originalClass;
					}
				}
			}
		}
		return null;
	}
}