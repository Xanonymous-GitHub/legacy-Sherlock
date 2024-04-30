package uk.ac.warwick.dcs.sherlock.launch

import org.springframework.boot.builder.SpringApplicationBuilder
import uk.ac.warwick.dcs.sherlock.api.annotation.SherlockModule
import uk.ac.warwick.dcs.sherlock.api.util.Side
import uk.ac.warwick.dcs.sherlock.engine.SherlockEngine

@SherlockModule(side = Side.SERVER)
class SherlockProdServer {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SherlockServer.engine = SherlockEngine(Side.SERVER)
            SpringApplicationBuilder(SherlockServer::class.java)
                .headless(true)
                .profiles("server")
                .run(*args)
        }
    }
}
