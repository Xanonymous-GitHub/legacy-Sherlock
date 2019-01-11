package uk.ac.warwick.dcs.sherlock.launch;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.EventListener;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import uk.ac.warwick.dcs.sherlock.api.annotation.EventHandler;
import uk.ac.warwick.dcs.sherlock.api.annotation.SherlockModule;
import uk.ac.warwick.dcs.sherlock.api.event.EventInitialisation;
import uk.ac.warwick.dcs.sherlock.api.event.EventPostInitialisation;
import uk.ac.warwick.dcs.sherlock.api.event.EventPreInitialisation;
import uk.ac.warwick.dcs.sherlock.api.util.Side;
import uk.ac.warwick.dcs.sherlock.engine.SherlockEngine;

@SherlockModule(side = Side.SERVER)
@SpringBootApplication
@ComponentScan("uk.ac.warwick.dcs.sherlock.module.web")
@ServletComponentScan("uk.ac.warwick.dcs.sherlock.module.web")
@EnableJpaRepositories("uk.ac.warwick.dcs.sherlock.module.web")
@EntityScan("uk.ac.warwick.dcs.sherlock.module.web")
public class SherlockServer extends SpringBootServletInitializer {

	static SherlockEngine engine;

	public static void main(String[] args) {
	}

	@EventListener (ApplicationReadyEvent.class)
	public void afterStartup() {
		engine.initialise();
	}

	@EventHandler
	public void initialisation(EventInitialisation event) {
	}

	@EventHandler
	public void postInitialisation(EventPostInitialisation event) {
	}

	@EventHandler
	public void preInitialisation(EventPreInitialisation event) {
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		engine = new SherlockEngine(Side.SERVER);
		application.profiles("server");
		return application.sources(SherlockServer.class);
	}
}