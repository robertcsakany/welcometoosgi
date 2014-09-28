package hu.blackbelt.welcometoosgi.webcontentservlet.webcontentservlet;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.*;
import org.apache.sling.commons.mime.MimeTypeService;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.Dictionary;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.apache.felix.scr.annotations.ReferenceCardinality.OPTIONAL_MULTIPLE;
import static org.apache.felix.scr.annotations.ReferencePolicy.DYNAMIC;

@Component(immediate = true, metatype = true)
@Properties(value = {
		@Property(name = "alias", value = "/"),
		@Property(name = "servlet-name", value = "webcontent-servlet"),
		@Property(name = "defaultIndex", value = "/index.html")

})
@References(value = {
		@Reference(
				referenceInterface = ContentResourceProvider.class,
				cardinality = OPTIONAL_MULTIPLE,
				strategy = ReferenceStrategy.EVENT,
				policy = DYNAMIC,
				bind = "bindContentResourceProvider",
				unbind = "unbindContentResourceProvider")
})
public class ContentServlet extends HttpServlet {

	private static final long serialVersionUID = 3527994797153914038L;
	Logger log = LoggerFactory.getLogger(ContentServlet.class);

	private ServiceRegistration registration;
	private String defaultIndex = "/index.html";

	@Reference
	private MimeTypeService mimeTypeProvider;

	List<ContentResourceProvider> contentResourceProviders = new CopyOnWriteArrayList<ContentResourceProvider>();

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String path = getUrlPath(req);
		if (StringUtils.isEmpty(path) || path.equalsIgnoreCase("/")) {
			redirectToIndex(req, resp);
			return;
		}

		// Checking last modification header. If the bundle hasn't been modified, the browser have to use
		// the cached version
		if (req.getHeader("If-Modified-Since") != null) {
			if (req.getHeader("If-Modified-Since").equals(getContentResourceProviderForPath(path).getLastModificationDate(path))) {
				resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
				return;
			}
		}

		InputStream is = getContentResourceProviderForPath(path).getContent(path);
		if (is == null) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Not found");
			return;
		}
		resp.setHeader("Last-Modified", getContentResourceProviderForPath(path).getLastModificationDate(path));

		String extension = FilenameUtils.getExtension(path);
		String contentType = mimeTypeProvider.getMimeType(extension);

		resp.setContentType(contentType);
		log.debug("content type of " + path + " is " + contentType);
		IOUtils.copy(is, resp.getOutputStream());
	}

	private void redirectToIndex(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String url = response.encodeRedirectURL(request.getContextPath()) + defaultIndex;
		response.sendRedirect(url);
	}

	private String getUrlPath(HttpServletRequest req) {
		// WebSphere: Request URI: /mpsdv1/trendallocation/index.html Path: /trendallocation/index.html Context Path: /mpsdv1
		if (StringUtils.isNotEmpty(req.getContextPath())) {
			return req.getRequestURI().substring(req.getContextPath().length());
		} else {
			return req.getRequestURI();
		}
	}

	@Activate
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void activate(ComponentContext context) {
		Dictionary dict = new java.util.Properties();

		if (context.getProperties().get("alias") != null) {
			dict.put("alias", context.getProperties().get("alias"));
			dict.put("servlet-name", context.getProperties().get("servlet-name"));
			log.info("Activating");
			this.registration = context.getBundleContext().registerService(Servlet.class.getName(), this, dict);
		}
		defaultIndex = PropertiesUtil.toString(context.getProperties().get("defaultIndex"), "/index.html");
	}

	@Deactivate
	protected void deactivate(BundleContext context) {
		if (this.registration != null && this.registration.getReference() != null) {
			log.info("DeActivating");
			context.ungetService(registration.getReference());
		}
		this.registration = null;
	}

	protected void bindContentResourceProvider(ContentResourceProvider provider) {
		log.info("Bind provider: "+provider.getClass()+" "+provider.getRootPath()+" "+provider.getRank());
		contentResourceProviders.add(provider);
	}

	protected void unbindContentResourceProvider(ContentResourceProvider provider) {
		log.info("UnBind provider: "+provider.getClass()+" "+provider.getRootPath()+" "+provider.getRank());
		contentResourceProviders.remove(provider);
	}

	private ContentResourceProvider getContentResourceProviderForPath(String path) {
		// Iterate over all providers, the highest rank and longest path will be the provider for us
		log.debug("Searching for path: "+path);
		int lastRank = -1;
		int lastPathMatchSize = -1;
		ContentResourceProvider ret = null;
		for (ContentResourceProvider provider : contentResourceProviders) {
			log.debug("Testing provider: "+provider.getClass().toString()+" "+provider.getRootPath()+" "+provider.getRank());
			if (StringUtils.isNotEmpty(provider.getRootPath()) && path.startsWith(provider.getRootPath())) {
				if (lastRank < provider.getRank()) {
					ret = provider;
					lastRank = provider.getRank();
					lastPathMatchSize = provider.getRootPath().length();
				} else if (lastRank == provider.getRank() && lastPathMatchSize < provider.getRootPath().length()) {
					ret = provider;
					lastPathMatchSize = provider.getRootPath().length();
				}
			}
		}
		log.debug("Selected provider: "+ret.getClass().toString()+" "+ret.getRootPath()+" "+ret.getRank());
		return ret;
	}
}
