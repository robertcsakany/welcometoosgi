package hu.blackbelt.welcometoosgi.transaction.api;


import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

public class TransactionManagerLocator {

	public static final String TRANSACTION_MANAGER_TYPE = "transactionManager.type";

	public static TransactionManager getTransactionManager(String type) {
		BundleContext ctx = FrameworkUtil.getBundle(TransactionManagerLocator.class).getBundleContext();
		if (ctx == null) {
			throw new RuntimeException("Could not get TransactionManager for "+type+" because the BundleContext is empty");
		}
		ServiceReference[] serviceReference = null;
		try {
			serviceReference = ctx.getAllServiceReferences(TransactionManager.class.getName(), "("+TRANSACTION_MANAGER_TYPE+"="+type+")");
		} catch (InvalidSyntaxException e) {
			throw new RuntimeException("Could not get TransactionManager for "+type, e);
		}
		if (serviceReference == null || serviceReference.length == 0) {
			throw new RuntimeException("There is no TransactionManager for "+type);
		}
		if (serviceReference.length != 1) {
			throw new RuntimeException("There is several TransactionManager for "+type);
		}
		return (TransactionManager) ctx.getService(serviceReference[0]);
	}

}
