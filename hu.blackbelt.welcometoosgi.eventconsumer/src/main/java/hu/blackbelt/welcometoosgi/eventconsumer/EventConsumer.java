package hu.blackbelt.welcometoosgi.eventconsumer;

import org.apache.felix.scr.annotations.*;
import org.apache.felix.scr.annotations.Component;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Component(metatype = true, immediate = true)
@Service
@Properties(value = {
		@Property(name = EventConstants.EVENT_TOPIC, value = {"test/event/GENERATED"})
})
public class EventConsumer implements EventHandler {

	Logger log = LoggerFactory.getLogger(EventConsumer.class);

	private String foo = "";

	@Override
	public void handleEvent(Event event) {
		log.info("Event catched - "+event.getTopic());
		log.info("Event property - "+event.getProperty("test"));
	}
}
