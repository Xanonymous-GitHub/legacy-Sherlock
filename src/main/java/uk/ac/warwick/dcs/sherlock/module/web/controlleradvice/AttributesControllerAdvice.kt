package uk.ac.warwick.dcs.sherlock.module.web.controlleradvice

import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.security.core.Authentication
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ModelAttribute
import uk.ac.warwick.dcs.sherlock.launch.SherlockServer
import uk.ac.warwick.dcs.sherlock.module.web.data.repositories.AccountRepository
import uk.ac.warwick.dcs.sherlock.module.web.data.wrappers.AccountWrapper
import uk.ac.warwick.dcs.sherlock.module.web.exceptions.SpringNotInitialised
import java.util.*

/**
 * Declares ModelAttributes for all controllers
 */
@ControllerAdvice
class AttributesControllerAdvice {
    @Autowired
    private val accountRepository: AccountRepository? = null

    @Autowired
    private val environment: Environment? = null

    /**
     * Checks that the Spring server has finished initialising and throws an error
     * if a user attempts to load a page before it has finished
     *
     * @throws SpringNotInitialised if the server is still starting up
     */
    @ModelAttribute
    @Throws(SpringNotInitialised::class)
    fun checkLoaded() {
        assert(SherlockServer.engine != null)
        if (!SherlockServer.engine!!.isInitialised) {
            throw SpringNotInitialised("Not loaded")
        }
    }

    /**
     * Gets the account of the currently logged-in user using the authentication
     * details and adds it to the model attributes
     *
     * @param model          holder for model attributes
     * @param authentication the authentication class
     * @return the account wrapper if logged in, or an empty wrapper if not
     */
    @ModelAttribute("account")
    fun getAccount(model: Model, authentication: Authentication?): AccountWrapper {
        if (authentication == null) return AccountWrapper()

        val accountWrapper = AccountWrapper(accountRepository!!.findByEmail(authentication.name))

        model.addAttribute("account", accountWrapper)
        return accountWrapper
    }

    /**
     * Adds the msg parameter to the attributes of all requests
     *
     * @param model   holder for model attributes
     * @param request the http request information
     */
    @ModelAttribute
    fun addMessage(model: Model, request: HttpServletRequest) {
        if (request.parameterMap.containsKey("msg")) {
            model.addAttribute("top_message", request.parameterMap["msg"])
        } else {
            model.addAttribute("top_message", "")
        }
    }

    /**
     * Adds an "is ajax" boolean to the attributes of all requests
     *
     * @param model   holder for model attributes
     * @param request the http request information
     */
    @ModelAttribute
    fun addIsAjax(model: Model, request: HttpServletRequest) {
        model.addAttribute("ajax", request.parameterMap.containsKey("ajax"))
    }

    /**
     * Adds an "is printing" boolean to the attribute of all requests
     *
     * @param model   holder for model attributes
     * @param request the http request information
     */
    @ModelAttribute
    fun addIsPrinting(model: Model, request: HttpServletRequest) {
        model.addAttribute("printing", request.parameterMap.containsKey("print"))
    }

    /**
     * Checks whether the request is attempting to print
     *
     * @param model   holder for model attributes
     * @param request the http request information
     * @return whether the request is attempting to print
     */
    @ModelAttribute("isPrinting")
    fun isPrinting(model: Model?, request: HttpServletRequest): Boolean {
        return request.parameterMap.containsKey("print")
    }


    /**
     * Checks whether the current request is ajax or not
     *
     * @param request the http request information
     * @return whether the request is an ajax one
     */
    @ModelAttribute("isAjax")
    fun isAjax(request: HttpServletRequest): Boolean {
        return request.parameterMap.containsKey("ajax")
    }

    /**
     * Sets the javascript url to the standard version when running the
     * webdev profile and the minified in all other cases
     *
     * @param model holder for model attributes
     */
    @ModelAttribute
    fun addJsUrl(model: Model) {
        model.addAttribute("javascript", "default.min.js")
    }
}
