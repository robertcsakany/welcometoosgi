package hu.blackbelt.welcometoosgi.transaction.api;


public interface TransactionManager {

	void beginTransaction(Transactional.TxType type);

	void commitTransaction(Transactional.TxType type);

	void rollbackTransaction(Transactional.TxType type);

}
