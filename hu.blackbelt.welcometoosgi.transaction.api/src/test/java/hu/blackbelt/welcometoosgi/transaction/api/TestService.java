package hu.blackbelt.welcometoosgi.transaction.api;

public interface TestService {

	void transactionalMethod();

	void transactionalMethodWithParam(String s);

	String transactionalMethodWithParamRet(String s);

	void transactionalMethodThrowingException();

	int getTransactionalMethodCnt();

	int getTransactionalMethodThrowingExceptionCnt();

	int getTransactionalMethodWithParamCnt();

	int getTransactionalMethodWithParamRetCnt();
}
