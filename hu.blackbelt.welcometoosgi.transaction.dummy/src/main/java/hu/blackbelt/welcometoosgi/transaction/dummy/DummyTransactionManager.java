package hu.blackbelt.welcometoosgi.transaction.dummy;


import hu.blackbelt.welcometoosgi.transaction.api.TransactionManager;
import hu.blackbelt.welcometoosgi.transaction.api.TransactionManagerLocator;
import hu.blackbelt.welcometoosgi.transaction.api.Transactional;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@Service
@Properties(value={
		@Property(name = TransactionManagerLocator.TRANSACTION_MANAGER_TYPE, value = "DUMMY")
})
public class DummyTransactionManager implements TransactionManager {

	private Logger log = LoggerFactory.getLogger(DummyTransactionManager.class);

	public void beginTransaction(Transactional.TxType type) {
		log.info("Begin transaction " + type.toString());
	}

	public void commitTransaction(Transactional.TxType type) {
		log.info("Commit transaction "+type.toString());
	}

	public void rollbackTransaction(Transactional.TxType type) {
		log.info("Rollback transaction "+type.toString());
	}

}
