package hu.blackbelt.welcometoosgi.webcontentservlet.webcontentservlet;

import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Implements a mediator pattern class for services from the OSGi container.
 */
class ServiceMediator {
	public static long WAIT_UNLIMITED = 0;
	public static long NO_WAIT = -1;

	private final String bundleName;
	private final long bundleId;
	private final BundleContext bundleContext;
	private final ServiceTracker configAdminTracker;
	private final ServiceTracker logTracker;

	ServiceMediator(BundleContext context) {
		bundleContext = context;
		bundleName = (bundleContext.getBundle().getSymbolicName() == null) ? bundleContext.getBundle().getLocation()
						: bundleContext.getBundle().getSymbolicName();
		bundleId = bundleContext.getBundle().getBundleId();

		ServiceTracker logTracker = null;
		try {
			logTracker = new ServiceTracker(bundleContext, LogService.class.getName(), null);
			logTracker.open();
		} catch (Throwable ex) {
			// This means we don't have access to the log service package since it
			// is optional, so don't track log services.
			logTracker = null;
		}
		this.logTracker = logTracker;

		configAdminTracker = new ServiceTracker(bundleContext, ConfigurationAdmin.class.getName(), null);
		configAdminTracker.open();
	}

	/**
	 * Returns a reference to the <tt>ConfigurationAdmin</tt> (Felix).
	 * 
	 * @param wait time in milliseconds to wait for the reference if it isn't available.
	 * @return the reference to the <tt>ConfigurationAdmin</tt> as obtained from the OSGi service layer.
	 */
	public ConfigurationAdmin getConfigurationAdminService(long wait) {
		ConfigurationAdmin configAdmin = null;
		try {
			if (wait < 0) {
				configAdmin = (ConfigurationAdmin) configAdminTracker.getService();
			} else {
				configAdmin = (ConfigurationAdmin) configAdminTracker.waitForService(wait);
			}
		} catch (InterruptedException e) {
			e.printStackTrace(System.err);
		}

		return configAdmin;
	}

	public Object getLogServiceLatch(long wait) {
		Object log = null;
		if (logTracker != null) {
			try {
				if (wait < 0) {
					log = logTracker.getService();
				} else {
					log = logTracker.waitForService(wait);
				}
			} catch (InterruptedException e) {
				e.printStackTrace(System.err);
			}
		}
		return log;
	}

	public void info(String msg) {
		Object log = getLogServiceLatch(NO_WAIT);
		if (log != null) {
			((LogService) log).log(LogService.LOG_INFO, msg);
		} else {
			sysout(msg);
		}
	}

	public void error(String msg, Throwable t) {
		Object log = getLogServiceLatch(NO_WAIT);
		if (log != null) {
			((LogService) log).log(LogService.LOG_ERROR, msg);
		} else {
			syserr(msg, t);
		}
	}

	public void error(String msg) {
		Object log = getLogServiceLatch(NO_WAIT);
		if (log != null) {
			((LogService) log).log(LogService.LOG_ERROR, msg);
		} else {
			syserr(msg, null);
		}
	}

	public void debug(String msg) {
		Object log = getLogServiceLatch(NO_WAIT);
		if (log != null) {
			((LogService) log).log(LogService.LOG_DEBUG, msg);
		} else {
			sysout(msg);
		}
	}

	public void warn(String msg) {
		Object log = getLogServiceLatch(NO_WAIT);
		if (log != null) {
			((LogService) log).log(LogService.LOG_WARNING, msg);
		} else {
			syserr(msg, null);
		}
	}

	private void sysout(String msg) {
		// Assemble String
		StringBuffer sbuf = new StringBuffer();
		sbuf.append(bundleName);
		sbuf.append(" [");
		sbuf.append(bundleId);
		sbuf.append("] ");
		sbuf.append(msg);
		System.out.println(sbuf.toString());
	}

	private void syserr(String msg, Throwable t) {
		// Assemble String
		StringBuffer sbuf = new StringBuffer();
		sbuf.append(bundleName);
		sbuf.append(" [");
		sbuf.append(bundleId);
		sbuf.append("] ");
		sbuf.append(msg);
		System.err.println(sbuf.toString());
		if (t != null) {
			t.printStackTrace(System.err);
		}
	}

	/**
	 * Deactivates this mediator, nulling out all references. If called when the bundle is stopped, the framework should actually
	 * take care of unregistering the <tt>ServiceListener</tt>.
	 */
	public void deactivate() {
		if (logTracker != null) {
			logTracker.close();
		}
		configAdminTracker.close();
	}

}
