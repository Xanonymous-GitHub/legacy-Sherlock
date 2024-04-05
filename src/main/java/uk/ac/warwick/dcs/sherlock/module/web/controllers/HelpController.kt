package uk.ac.warwick.dcs.sherlock.module.web.controllers

import jakarta.servlet.http.HttpServletRequest
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import org.springframework.core.io.support.PropertiesLoaderUtils
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.support.RequestContextUtils
import uk.ac.warwick.dcs.sherlock.module.web.exceptions.LoadingHelpFailed
import java.io.IOException
import java.util.*

/**
 * The controller that deals with the help pages (help centre, terms and privacy policy)
 */
@Controller
class HelpController {
    /**
     * Handles requests to the help page
     *
     * @param model holder for model attributes (autofilled by Spring)
     * @param request the http request information (autofilled by Spring)
     *
     * @return the path to the help page
     *
     * @throws LoadingHelpFailed if the help.properties file for both the current
     * locale and default failed to load
     */
    @RequestMapping("/help")
    @Throws(LoadingHelpFailed::class)
    fun index(model: Model, request: HttpServletRequest?): String {
        val locale = RequestContextUtils.getLocale(request!!).toLanguageTag()
        val properties = try {
            loadProperties(locale)
        } catch (e: LoadingHelpFailed) {
            loadProperties("")
        }

        val questions: MutableMap<String, String> = TreeMap()
        for (s in properties.stringPropertyNames()) {
            if (!s.endsWith("_answer")) {
                questions[properties.getProperty(s, "")] = properties.getProperty(s + "_answer", "")
            }
        }

        model.addAttribute("questions", questions)
        return "help/index"
    }

    /**
     * Handles requests to the terms page
     *
     * @return the path to the terms page
     */
    @RequestMapping("/terms")
    fun terms(): String {
        return "help/terms"
    }

    /**
     * Handles requests to the privacy page
     *
     * @return the path to the privacy page
     */
    @RequestMapping("/privacy")
    fun privacy(): String {
        return "help/privacy"
    }

    /**
     * Loads the help.properties file for the supplied locale
     *
     * @param locale the locale to load the properties file for
     *
     * @return the properties object
     *
     * @throws LoadingHelpFailed if the help.properties file for locale failed to load
     */
    @Throws(LoadingHelpFailed::class)
    private fun loadProperties(locale: String): Properties {
        var prefixedLocale = locale
        if (locale.isNotEmpty()) {
            prefixedLocale = "_$locale"
        }

        val resource: Resource = ClassPathResource("/help$prefixedLocale.properties")

        try {
            return PropertiesLoaderUtils.loadProperties(resource)
        } catch (e: IOException) {
            throw LoadingHelpFailed("Loading help.properties file failed.")
        }
    }
}
