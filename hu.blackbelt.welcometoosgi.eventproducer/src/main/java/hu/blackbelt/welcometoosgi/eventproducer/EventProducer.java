package hu.blackbelt.welcometoosgi.eventproducer;

import org.apache.felix.scr.annotations.*;
import org.apache.felix.scr.annotations.Component;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.Hashtable;

@Component(metatype = true, immediate = true)
@Properties(value = {
		@Property(name = "fooBar", value = "bar")
})
public class EventProducer {

	Logger log = LoggerFactory.getLogger(EventProducer.class);

	private String foo = "";

	@Reference
	EventAdmin eventAdmin;

	@Activate
	public void activate(ComponentContext context) {
		Dictionary properties = new Hashtable();
		properties.put("test", context.getProperties().get("fooBar"));
		Event generatedEvent = new Event("test/event/GENERATED", properties);
		log.info("Sending event");
		eventAdmin.sendEvent(generatedEvent);
	}
}
