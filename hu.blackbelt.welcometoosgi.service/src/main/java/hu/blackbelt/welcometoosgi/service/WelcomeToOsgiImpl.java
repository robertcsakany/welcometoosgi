package hu.blackbelt.welcometoosgi.service;

import hu.blackbelt.welcometoosgi.api.WelcomeToOsgi;
import org.apache.felix.scr.annotations.*;
import org.osgi.service.component.ComponentContext;

/**
 * Created by robson on 7/25/14.
 */

@Component(metatype = true, immediate = true)
@Service(WelcomeToOsgi.class)
@Properties(value = {
		@Property(name = "fooBar", value = "bar")
})
public class WelcomeToOsgiImpl implements WelcomeToOsgi {

	private String foo = "";

	@Override
	public String welcomeToOsgi() {
		return foo;
	}

	@Activate
	public void activate(ComponentContext context) {
		foo = (String)context.getProperties().get("fooBar");
	}

	@Deactivate
	public void deactive() {
		// TODO
	}
}
