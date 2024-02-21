package uk.ac.warwick.dcs.sherlock.launch

import org.springframework.boot.builder.SpringApplicationBuilder
import uk.ac.warwick.dcs.sherlock.api.annotation.SherlockModule
import uk.ac.warwick.dcs.sherlock.api.util.Side
import uk.ac.warwick.dcs.sherlock.engine.SherlockEngine
import javax.swing.JFrame
import javax.swing.JOptionPane
import kotlin.system.exitProcess

@SherlockModule(side = Side.CLIENT)
class SherlockClient {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            System.setProperty("spring.devtools.restart.enabled", "false") // fix stupid double instance bug

            SherlockServer.engine = SherlockEngine(Side.CLIENT)

            if (!SherlockServer.engine!!.isValidInstance) {
                val jf = JFrame()
                jf.isAlwaysOnTop = true
                JOptionPane.showMessageDialog(
                    jf,
                    "Sherlock is already running",
                    "Sherlock error",
                    JOptionPane.ERROR_MESSAGE
                )
                exitProcess(1)
            } else {
                //If "-Dmodules" is in the JVM arguments, set the path to provided
                val modulesPath = System.getProperty("modules")
                if (modulesPath != null && modulesPath.isNotEmpty()) {
                    SherlockEngine.setOverrideModulesPath(modulesPath)
                }

                //If "-Doverride=True" is in the JVM arguments, make Spring thing it is running as a server
                val override = System.getProperty("override")
                if (override != null && override == "True") {
                    SpringApplicationBuilder(SherlockServer::class.java).headless(false).profiles("server").run(*args)
                } else {
                    SpringApplicationBuilder(SherlockServer::class.java).headless(false).profiles("client").run(*args)
                }
            }
        }
    }
}
