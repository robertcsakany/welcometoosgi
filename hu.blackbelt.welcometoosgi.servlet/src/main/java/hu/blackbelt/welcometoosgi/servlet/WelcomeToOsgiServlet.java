package hu.blackbelt.welcometoosgi.servlet;

import hu.blackbelt.welcometoosgi.api.WelcomeToOsgi;
import org.apache.felix.scr.annotations.*;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component(immediate = true, metatype = true)
@Service(Servlet.class)
@Properties(value = {
		@Property(name = "alias", value = "/welcometoosgi")
})
public class WelcomeToOsgiServlet extends HttpServlet {

	private static final long serialVersionUID = 3527994797153914038L;

	Logger log = LoggerFactory.getLogger(WelcomeToOsgiServlet.class);

	@Reference()
	private WelcomeToOsgi welcomeToOsgiService;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		log.debug("GET " + req.getRequestURI());
		resp.getWriter().append("<html><body><p>"+welcomeToOsgiService.welcomeToOsgi()+"</p></body></html>");
	}
}
