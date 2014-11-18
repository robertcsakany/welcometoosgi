package hu.blackbelt.welcometoosgi.sample.transaction;


import hu.blackbelt.welcometoosgi.transaction.api.Transactional;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Transactional
@Component(immediate = true)
@Service(value=TestService.class)
public class TestServiceImpl implements TestService {

	Logger log = LoggerFactory.getLogger(TestServiceImpl.class);

	@Activate
	protected void activate(ComponentContext context) {
		log.info("Activate Transaction Test service");
		transactionalMethod();
		try {
			transactionalMethodThrowingException();
		} catch (Throwable th) {
			log.error("transactionalMethodThrowingException", th);
		}
	}

	@Transactional(Transactional.TxType.REQUIRED)
	@Override
	public void transactionalMethod() {
		log.info("transactionalMethod");
	}

	@Transactional(Transactional.TxType.REQUIRED)
	@Override
	public void transactionalMethodThrowingException() {
		log.info("transactionalMethodThrowingException");
		throw new RuntimeException("Bang!");
	}

}
