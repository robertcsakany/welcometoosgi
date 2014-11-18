package hu.blackbelt.welcometoosgi.transaction.api;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Transactional
public class TestServiceImpl implements TestService {

	Logger log = LoggerFactory.getLogger(TestServiceImpl.class);

	private int transactionalMethodCnt = 0;
	private int transactionalMethodThrowingExceptionCnt = 0;
	private int transactionalMethodWithParamCnt = 0;
	private int transactionalMethodWithParamRetCnt = 0;


	@Override
	public void transactionalMethod() {
		transactionalMethodCnt++;
		log.info("transactionalMethod");
	}

	@Override
	public void transactionalMethodThrowingException() {
		transactionalMethodThrowingExceptionCnt++;
		log.info("transactionalMethodThrowingException");
		throw new RuntimeException("Bang!");
	}

	@Override
	public void transactionalMethodWithParam(String s) {
		transactionalMethodWithParamCnt++;
		log.info("transactionalMethodWithParam "+s);
	}

	@Override
	public String transactionalMethodWithParamRet(String s) {
		transactionalMethodWithParamRetCnt++;
		log.info("transactionalMethodWithParamRet "+s);
		return "eh";
	}

	@Transactional(Transactional.TxType.NOT_SUPPORTED)
	public int getTransactionalMethodCnt() {
		return transactionalMethodCnt;
	}

	@Transactional(Transactional.TxType.NOT_SUPPORTED)
	public int getTransactionalMethodThrowingExceptionCnt() {
		return transactionalMethodThrowingExceptionCnt;
	}

	@Transactional(Transactional.TxType.NOT_SUPPORTED)
	public int getTransactionalMethodWithParamCnt() {
		return transactionalMethodWithParamCnt;
	}

	@Transactional(Transactional.TxType.NOT_SUPPORTED)
	public int getTransactionalMethodWithParamRetCnt() {
		return transactionalMethodWithParamRetCnt;
	}
}
