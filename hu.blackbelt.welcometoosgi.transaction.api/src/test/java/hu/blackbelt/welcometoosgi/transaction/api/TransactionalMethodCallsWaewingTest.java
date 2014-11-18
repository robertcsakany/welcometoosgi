package hu.blackbelt.welcometoosgi.transaction.api;



import com.googlecode.catchexception.CatchException;
import hu.blackbelt.internal.ByteArrayClassLoader;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.osgi.framework.Bundle;
import org.osgi.framework.hooks.weaving.WovenClass;
import org.osgi.framework.wiring.BundleWiring;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Dictionary;

@RunWith(PowerMockRunner.class)
@PrepareForTest({TransactionManagerLocator.class})
//@RunWith(MockitoJUnitRunner.class)
public class TransactionalMethodCallsWaewingTest {

	Logger log = LoggerFactory.getLogger(TransactionalMethodCallsWaewingTest.class);

	@InjectMocks
	TransactionalMethodCallsWeavingHook hook = new TransactionalMethodCallsWeavingHook();

	TestService target;

	@Captor
	ArgumentCaptor<byte[]> classByteCaptor;

	@Mock
	WovenClass wovenClassMock;

	@Mock
	BundleWiring bundleWiringMock;

	@Mock
	Bundle bundleMock;

	@Mock
	Dictionary<String, String> headersMock;

	private boolean setup = false;

	@Mock
	TransactionManager transactionManagerMock;

	@Before
	public void setup() throws IOException, IllegalAccessException, InstantiationException {
		if (!setup) {
			String classAsPath = TestServiceImpl.class.getName().replace('.', '/') + ".class";
			InputStream stream = this.getClass().getClassLoader().getResourceAsStream(classAsPath);

			byte[] b = IOUtils.toByteArray(stream);

			Mockito.when(wovenClassMock.getBytes()).thenReturn(b);
			Mockito.when(wovenClassMock.getClassName()).thenReturn(TestServiceImpl.class.getName());
			Mockito.when(wovenClassMock.getBundleWiring()).thenReturn(bundleWiringMock);
			Mockito.when(bundleWiringMock.getBundle()).thenReturn(bundleMock);
			Mockito.when(bundleWiringMock.getClassLoader()).thenReturn(TestService.class.getClassLoader());

			Mockito.when(bundleMock.getHeaders()).thenReturn(headersMock);
			final String tmType = "TEST";
			Mockito.when(headersMock.get(Mockito.anyString())).thenReturn(tmType);

			PowerMockito.mockStatic(TransactionManagerLocator.class);
			PowerMockito.when(TransactionManagerLocator.getTransactionManager(tmType)).thenReturn(transactionManagerMock);

			hook.weave(wovenClassMock);

			Mockito.verify(wovenClassMock).setBytes(classByteCaptor.capture());

			ByteArrayClassLoader l = new ByteArrayClassLoader(TestService.class.getClassLoader());
			byte[] n = classByteCaptor.getValue();

			target = (TestService) l.findClass(TestServiceImpl.class.getName(), n).newInstance();
			setup = true;
		}
	}

	@Test
	public void transactionalMethodTest() throws IllegalAccessException, InstantiationException, IOException {
		target.transactionalMethod();
		Assert.assertEquals(1, target.getTransactionalMethodCnt());
		Assert.assertEquals(0, target.getTransactionalMethodThrowingExceptionCnt());
		Assert.assertEquals(0, target.getTransactionalMethodWithParamCnt());
		Assert.assertEquals(0, target.getTransactionalMethodWithParamRetCnt());

		Mockito.verify(transactionManagerMock, Mockito.times(1)).beginTransaction(Mockito.any(Transactional.TxType.class));
		Mockito.verify(transactionManagerMock, Mockito.times(1)).commitTransaction(Mockito.any(Transactional.TxType.class));
		Mockito.verify(transactionManagerMock, Mockito.times(0)).rollbackTransaction(Mockito.any(Transactional.TxType.class));

	}

	@Test
	public void transactionalMethodThrowingExceptionTest() throws IllegalAccessException, InstantiationException, IOException {
		CatchException.verifyException(target, RuntimeException.class).transactionalMethodThrowingException();
		Mockito.verify(transactionManagerMock, Mockito.times(1)).beginTransaction(Mockito.any(Transactional.TxType.class));
		Mockito.verify(transactionManagerMock, Mockito.times(0)).commitTransaction(Mockito.any(Transactional.TxType.class));
		Mockito.verify(transactionManagerMock, Mockito.times(1)).rollbackTransaction(Mockito.any(Transactional.TxType.class));
	}

	@Test
	public void transactionalMethodWithParamTest() throws IllegalAccessException, InstantiationException, IOException {
		target.transactionalMethodWithParam("rr");
		Assert.assertEquals(0, target.getTransactionalMethodCnt());
		Assert.assertEquals(0, target.getTransactionalMethodThrowingExceptionCnt());
		Assert.assertEquals(1, target.getTransactionalMethodWithParamCnt());
		Assert.assertEquals(0, target.getTransactionalMethodWithParamRetCnt());

		Mockito.verify(transactionManagerMock, Mockito.times(1)).beginTransaction(Mockito.any(Transactional.TxType.class));
		Mockito.verify(transactionManagerMock, Mockito.times(1)).commitTransaction(Mockito.any(Transactional.TxType.class));
		Mockito.verify(transactionManagerMock, Mockito.times(0)).rollbackTransaction(Mockito.any(Transactional.TxType.class));
	}

	@Test
	public void transactionalMethodWithParamRetTest() throws IllegalAccessException, InstantiationException, IOException {
		String ret = target.transactionalMethodWithParamRet("rr");
		Assert.assertEquals(0, target.getTransactionalMethodCnt());
		Assert.assertEquals(0, target.getTransactionalMethodThrowingExceptionCnt());
		Assert.assertEquals(0, target.getTransactionalMethodWithParamCnt());
		Assert.assertEquals(1, target.getTransactionalMethodWithParamRetCnt());
		Assert.assertEquals("eh", ret);

		Mockito.verify(transactionManagerMock, Mockito.times(1)).beginTransaction(Mockito.any(Transactional.TxType.class));
		Mockito.verify(transactionManagerMock, Mockito.times(1)).commitTransaction(Mockito.any(Transactional.TxType.class));
		Mockito.verify(transactionManagerMock, Mockito.times(0)).rollbackTransaction(Mockito.any(Transactional.TxType.class));
	}

}
