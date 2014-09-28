package hu.blackbelt.welcometoosgi.webcontentservlet.webcontentservlet;

import org.apache.felix.scr.annotations.*;
import org.apache.felix.scr.annotations.Properties;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.SynchronousBundleListener;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component(metatype = true, immediate=true)
@Service(value={ContentResourceProvider.class, SynchronousBundleListener.class})
@Properties(value = {
		@Property(name = "provider", value = "BundleContentResourceProvider"),
		@Property(name = org.osgi.framework.Constants.SERVICE_RANKING, intValue = 0)
})

public class BundleContentResourceProvider implements ContentResourceProvider, SynchronousBundleListener {

	private static final String WEB_CONTENT_TAG = "Web-Content";

	Logger log = LoggerFactory.getLogger(BundleContentResourceProvider.class);
	ConfigurationAdmin configurationAdmin;
	ServiceMediator services;

	private int rank = 0;

	private List<OrderedBundle> bundles = new ArrayList<OrderedBundle>();
	private Map<String, URL> urlCache = new ConcurrentHashMap<String, URL>();
	private Map<String, Date> bundleDateCache = new ConcurrentHashMap<String, Date>();

	public void registerContentProvider(Bundle bundle, int priority) {
		bundles.add(new OrderedBundle(priority, bundle));
		Collections.sort(bundles);
	}

	public void unregisterContentProvider(Bundle bundle) {
		bundles.remove(new OrderedBundle(0, bundle));
		// TODO: better way. We don't want to clear the whole cache
		urlCache.clear();
		bundleDateCache.clear();
	}

	@Override
	public String getLastModificationDate(String path) throws IOException {
		if (bundleDateCache.containsKey(path)) {
			Date lastModificationDate = bundleDateCache.get(path);
			DateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
			df.setTimeZone(TimeZone.getTimeZone("GMT"));
			return df.format(lastModificationDate);
		} else {
			return null;
		}
	}

	public String getRootPath() {
		return "/";
	}

	@Override
	public int getRank() {
		return rank;
	}

	@Override
	public InputStream getContent(String path) throws IOException {
	
		if (urlCache.containsKey(path)) {
			return urlCache.get(path).openStream();
		}
		for (OrderedBundle ob : bundles) {
			URL url = ob.bundle.getResource(path);
			if (url != null) {
				urlCache.put(path, url);
				bundleDateCache.put(path, new Date());
				return url.openStream();
			}
		}
		return null;
	}




	/**
	 * Loads and unloads any configuration provided by the bundle whose state changed. If the bundle has been started, the
	 * configuration is loaded. If the bundle is about to stop, the configurations are unloaded.
	 *
	 * @param event The <code>BundleEvent</code> representing the bundle state change.
	 */
	@Override
	public void bundleChanged(BundleEvent event) {

		//
		// NOTE:
		// This is synchronous - take care to not block the system !!
		//

		switch (event.getType()) {
			case BundleEvent.STARTING:
				try {
					registerBundle(event.getBundle());
				} catch (Throwable t) {
					log.error("bundleChanged: Problem loading header information of bundle " + event.getBundle().getSymbolicName()
							+ " (" + event.getBundle().getBundleId() + ")", t);
				} finally {
				}
				break;
			case BundleEvent.STOPPED:
				try {
					unregisterBundle(event.getBundle());
				} catch (Throwable t) {
					log.error("bundleChanged: Problem unloading header information of bundle " + event.getBundle().getSymbolicName()
							+ " (" + event.getBundle().getBundleId() + ")", t);
				} finally {
				}
				break;
		}
	}

	@Activate
	protected void activate(ComponentContext context) {
		rank = PropertiesUtil.toInteger(context.getProperties().get(org.osgi.framework.Constants.SERVICE_RANKING), 0);

		services = new ServiceMediator(context.getBundleContext());
		configurationAdmin = services.getConfigurationAdminService(ServiceMediator.NO_WAIT);
		context.getBundleContext().addBundleListener(this);

		int ignored = 0;
		try {
			Bundle[] bundles = context.getBundleContext().getBundles();
			for (int i = 0; i < bundles.length; i++) {
				Bundle bundle = bundles[i];

				if ((bundle.getState() & (Bundle.ACTIVE)) != 0) {
					// load configurations from bundles which are ACTIVE
					try {
						registerBundle(bundle);
					} catch (Throwable t) {
						log.error("bundleChanged: Problem loading header information of bundle " + bundle.getSymbolicName()
								+ " (" + bundle.getBundleId() + ")", t);
					} finally {
					}
				} else {
					ignored++;
				}

				if ((bundle.getState() & (Bundle.ACTIVE)) == 0) {
					// remove configurations from bundles which are not ACTIVE
					try {
						unregisterBundle(bundle);
					} catch (Throwable t) {
						log.error("bundleChanged: Problem unloading header information of bundle " + bundle.getSymbolicName()
								+ " (" + bundle.getBundleId() + ")", t);
					} finally {
					}
				} else {
					ignored++;
				}

			}
			log.debug("Out of " + bundles.length + " bundles, " + ignored
					+ " were not in a suitable state for web content loading");
		} catch (Throwable t) {
			log.error("activate: Problem while loading WebContent", t);
		} finally {
		}
	}

	@Deactivate
	public void deactivate(BundleContext context) throws Exception {
		context.removeBundleListener(this);
		int ignored = 0;
		try {
			Bundle[] bundles = context.getBundles();
			for (int i = 0; i < bundles.length; i++) {
				Bundle bundle = bundles[i];

				if ((bundle.getState()) == 0) {
					// remove configurations from bundles which are not ACTIVE
					try {
						unregisterBundle(bundle);
					} catch (Throwable t) {
						log.error("Problem unloading header information of bundle " + bundle.getSymbolicName() + " ("
								+ bundle.getBundleId() + ")", t);
					} finally {
					}
				} else {
					ignored++;
				}

			}
			log.debug("Out of " + bundles.length + " bundles, " + ignored
					+ " were not in a suitable state for web content loading");
		} catch (Throwable t) {
			log.error("activate: Problem while loading web content loader", t);
		} finally {
		}

		if (services != null) {
			services.deactivate();
			services = null;
		}
	}

	/**
	 * Register a bundle - check for WebContent MANIFEST.MF tag.
	 *
	 * @param bundle
	 */
	public void registerBundle(final Bundle bundle) throws Exception {
		if (bundle.getHeaders().get(WEB_CONTENT_TAG) != null) {
			log.info("Web-Content tag found in: " + bundle.getSymbolicName() + " Registering");
			int priority = 0;
			try {
				priority = Integer.parseInt(bundle.getHeaders().get(WEB_CONTENT_TAG).toString());
			} catch (Throwable th) {
				log.warn("Unable to parse Web-Content priotity");
			}
			registerContentProvider(bundle, priority);
		}
	}

	/**
	 * UnRegister a bundle - check for WebContent MANIFEST.MF tag.
	 *
	 * @param bundle The bundle.
	 */
	public void unregisterBundle(final Bundle bundle) throws Exception {

		if (bundle.getHeaders().get(WEB_CONTENT_TAG) != null) {
			log.info("Web-Content tag found in: " + bundle.getSymbolicName() + " Unregistering");
			unregisterContentProvider(bundle);
		}
	}
}

