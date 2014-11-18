package hu.blackbelt.welcometoosgi.transaction.api;

import java.lang.annotation.*;

@Inherited
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Transactional {

	/**
	 * The TxType element of the Transactional annotation indicates whether a bean method
	 * is to be executed within a persistence context.
	 */
	TxType value() default TxType.REQUIRED;

	/**
	 * The TxType element of the annotation indicates whether a bean method is to be
	 * executed within a persistence context where the values provide the following
	 * corresponding behavior.
	 */
	public enum TxType {
		/**
		 *  <p>If called outside a persistence context, the interceptor must begin a new
		 *  JTA persistence, the managed bean method execution must then continue
		 *  inside this persistence context, and the persistence must be completed by
		 *  the interceptor.</p>
		 *  <p>If called inside a persistence context, the managed bean
		 *  method execution must then continue inside this persistence context.</p>
		 */
		REQUIRED,

		/**
		 *  <p>If called outside a persistence context, the interceptor must begin a new
		 *  JTA persistence, the managed bean method execution must then continue
		 *  inside this persistence context, and the persistence must be completed by
		 *  the interceptor.</p>
		 *  <p>If called inside a persistence context, the current persistence context must
		 *  be suspended, a new JTA persistence will begin, the managed bean method
		 *  execution must then continue inside this persistence context, the persistence
		 *  must be completed, and the previously suspended persistence must be resumed.</p>
		 */
		REQUIRES_NEW,

		/**
		 *  <p>If called outside a persistence context, a TransactionalException with a
		 *  nested TransactionRequiredException must be thrown.</p>
		 *  <p>If called inside a persistence context, managed bean method execution will
		 *  then continue under that context.</p>
		 */
		MANDATORY,

		/**
		 *  <p>If called outside a persistence context, managed bean method execution
		 *  must then continue outside a persistence context.</p>
		 *  <p>If called inside a persistence context, the managed bean method execution
		 *  must then continue inside this persistence context.</p>
		 */
		SUPPORTS,

		/**
		 *  <p>If called outside a persistence context, managed bean method execution
		 *  must then continue outside a persistence context.</p>
		 *  <p>If called inside a persistence context, the current persistence context must
		 *  be suspended, the managed bean method execution must then continue
		 *  outside a persistence context, and the previously suspended persistence
		 *  must be resumed by the interceptor that suspended it after the method
		 *  execution has completed.</p>
		 */
		NOT_SUPPORTED,

		/**
		 *  <p>If called outside a persistence context, managed bean method execution
		 *  must then continue outside a persistence context.</p>
		 *  <p>If called inside a persistence context, a TransactionalException with
		 *  a nested InvalidTransactionException must be thrown.</p>
		 */
		NEVER
	}

	/**
	 * The rollbackOn element can be set to indicate exceptions that must cause
	 *  the interceptor to mark the persistence for rollback. Conversely, the dontRollbackOn
	 *  element can be set to indicate exceptions that must not cause the interceptor to mark
	 *  the persistence for rollback. When a class is specified for either of these elements,
	 *  the designated behavior applies to subclasses of that class as well. If both elements
	 *  are specified, dontRollbackOn takes precedence.
	 * @return Class[] of Exceptions
	 */
	public Class[] rollbackOn() default {};

	/**
	 * The dontRollbackOn element can be set to indicate exceptions that must not cause
	 *  the interceptor to mark the persistence for rollback. Conversely, the rollbackOn element
	 *  can be set to indicate exceptions that must cause the interceptor to mark the persistence
	 *  for rollback. When a class is specified for either of these elements,
	 *  the designated behavior applies to subclasses of that class as well. If both elements
	 *  are specified, dontRollbackOn takes precedence.
	 * @return Class[] of Exceptions
	 */
	public Class[] dontRollbackOn() default {};

}