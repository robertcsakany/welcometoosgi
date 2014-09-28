package hu.blackbelt.welcometoosgi.webcontentservlet.webcontentservlet;

import org.apache.felix.scr.annotations.*;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.osgi.service.component.ComponentContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

@Component(metatype = true, immediate=false, configurationFactory = true)
@Service(value=ContentResourceProvider.class, serviceFactory = true)
@Properties(value = {
		@Property(name = "root", value = ""),
		@Property(name = "fileSystemRoot", value = ""),
		@Property(name = "ds.factory.enabled", boolValue = false),
		@Property(name = org.osgi.framework.Constants.SERVICE_RANKING, intValue = 1)
})
public class FileSystemContentResourceProvider implements ContentResourceProvider {

	private String root = "/";
	private String fileSystemRoot = "";
	private int rank = 1;

	@Override
	public String getLastModificationDate(String path) throws IOException {
		File f = new File(getFileSystemPath(path));
		DateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
		df.setTimeZone(TimeZone.getTimeZone("GMT"));
		return df.format(f.lastModified());
	}

	@Override
	public InputStream getContent(String path) throws IOException {
		return new FileInputStream(getFileSystemPath(path));
	}

	@Override
	public String getRootPath() {
		return root;
	}

	@Override
	public int getRank() {
		return rank;
	}

	@Activate
	protected void activate(ComponentContext context) {
		/*provider = PropertiesUtil.toString(context.getProperties().get("provider"), this.getClass().getName());
		root = PropertiesUtil.toString(context.getProperties().get("root"), "/");
		fileSystemRoot = PropertiesUtil.toString(context.getProperties().get("fileSystemRoot"), "");
		rank = PropertiesUtil.toInteger(context.getProperties().get(org.osgi.framework.Constants.SERVICE_RANKING), 0); */
		root = PropertiesUtil.toString(context.getProperties().get("root"), null);
		fileSystemRoot = PropertiesUtil.toString(context.getProperties().get("fileSystemRoot"), null);
		rank = PropertiesUtil.toInteger(context.getProperties().get(org.osgi.framework.Constants.SERVICE_RANKING), 1);
	}

	public String getFileSystemPath(String path) {
		String mp = path;
		if (!mp.startsWith("/")) {
			mp = "/" + mp;
		}

		String mr = root;
		if (!mr.startsWith("/")) {
			mr = "/" + mr;
		}
		if (!mr.endsWith("/")) {
			mr += "/";
		}

		String fr = fileSystemRoot;
		if (!fr.endsWith("/")) {
			fr += "/";
		}

		return fr+mp.substring(mr.length());
	}

}
