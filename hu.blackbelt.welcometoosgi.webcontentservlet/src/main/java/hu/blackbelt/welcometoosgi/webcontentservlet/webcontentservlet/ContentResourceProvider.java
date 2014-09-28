package hu.blackbelt.welcometoosgi.webcontentservlet.webcontentservlet;

import java.io.IOException;
import java.io.InputStream;

public interface ContentResourceProvider {
	
	String getLastModificationDate(String path) throws IOException;

	InputStream getContent(String path) throws IOException;

	String getRootPath();

	int getRank();

}
