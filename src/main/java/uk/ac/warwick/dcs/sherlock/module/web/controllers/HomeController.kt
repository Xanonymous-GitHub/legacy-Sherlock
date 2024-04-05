package uk.ac.warwick.dcs.sherlock.module.web.controllers

import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.RequestMapping
import uk.ac.warwick.dcs.sherlock.module.web.configuration.properties.WebmasterProperties

/**
 * The controller that deals with the homepage for anonymous users
 */
@Controller
class HomeController {
    /**
     * Handles requests to the homepage, loads the contact details from the
     * webmaster properties and redirects to the dashboard home if the
     * user is logged in
     *
     * @param webmasterProperties details of the webmaster stored in the app properties
     * @param model holder for model attributes
     *
     * @return the path to the homepage
     */
    @RequestMapping("/")
    fun index(webmasterProperties: WebmasterProperties, model: Model): String {
        //Redirect if the user is logged in
        if (SecurityContextHolder.getContext().authentication !is AnonymousAuthenticationToken) {
            return "redirect:dashboard/index"
        }

        model.addAttribute("institution", webmasterProperties.institution)
        model.addAttribute("contact", webmasterProperties.contact)
        model.addAttribute("link", webmasterProperties.link)

        return "home/index"
    }
}
