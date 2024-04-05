package uk.ac.warwick.dcs.sherlock.module.web.controllers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import uk.ac.warwick.dcs.sherlock.module.web.configuration.SecurityConfig.Companion.getLocalEmail
import uk.ac.warwick.dcs.sherlock.module.web.configuration.SecurityConfig.Companion.getLocalPassword

/**
 * The controller that deals with the login page
 */
@Controller
class SecurityController {
    @Autowired
    private val environment: Environment? = null

    /**
     * Handles requests to the login page, automatically fills in the
     * login form when running as a client
     *
     * @param model holder for model attributes
     *
     * @return the path to the login page
     */
    @GetMapping("/login")
    fun login(model: Model): String {
        //Automatically login if running locally
        if (listOf(*environment!!.activeProfiles).contains("client")) {
            model.addAttribute("local_username", getLocalEmail())
            model.addAttribute("local_password", getLocalPassword())
            return "security/loginLocal"
        }

        return "security/login"
    }
}
