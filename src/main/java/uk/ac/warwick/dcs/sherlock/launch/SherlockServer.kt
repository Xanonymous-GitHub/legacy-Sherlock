package uk.ac.warwick.dcs.sherlock.launch

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.boot.web.servlet.ServletComponentScan
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.context.event.EventListener
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import uk.ac.warwick.dcs.sherlock.api.util.Side
import uk.ac.warwick.dcs.sherlock.engine.SherlockEngine
import javax.sql.DataSource
import kotlin.system.exitProcess

@SpringBootApplication
@ComponentScan("uk.ac.warwick.dcs.sherlock.module.web")
@ServletComponentScan("uk.ac.warwick.dcs.sherlock.module.web")
@EnableJpaRepositories("uk.ac.warwick.dcs.sherlock.module.web")
@EntityScan("uk.ac.warwick.dcs.sherlock.module.web")
class SherlockServer : SpringBootServletInitializer() {
    @EventListener(ApplicationReadyEvent::class)
    fun afterStartup() {
        engine!!.initialise()
    }

    override fun configure(application: SpringApplicationBuilder): SpringApplicationBuilder {
        engine = SherlockEngine(Side.SERVER)
        if (!engine!!.isValidInstance) {
            System.err.println("Sherlock is already running, closing....")
            exitProcess(1)
        } else {
            application.profiles("server")
            return application.sources(SherlockServer::class.java)
        }
    }

    @Bean
    @Primary
    @Profile("client")
    fun dataSource(): DataSource {
        return DataSourceBuilder
            .create()
            .username("sa")
            .password("")
            .url("jdbc:h2:file:" + SherlockEngine.configuration.dataPath + "/Sherlock-Web")
            .driverClassName("org.h2.Driver")
            .build()
    }

    companion object {
        @JvmField
        var engine: SherlockEngine? = null

        @JvmStatic
        fun main(args: Array<String>) {
        }
    }
}