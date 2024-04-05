package uk.ac.warwick.dcs.sherlock.module.web.controlleradvice

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.multipart.MaxUploadSizeExceededException
import uk.ac.warwick.dcs.sherlock.module.web.data.repositories.AccountRepository
import uk.ac.warwick.dcs.sherlock.module.web.data.wrappers.AccountWrapper
import uk.ac.warwick.dcs.sherlock.module.web.exceptions.*

/**
 * Handles exceptions thrown by all the controllers
 */
@ControllerAdvice
class ExceptionControllerAdvice {
    @Autowired
    private val accountRepository: AccountRepository? = null

    @Autowired
    private val environment: Environment? = null

    /**
     * Handles requests which occur before Spring has finished starting up
     *
     * @param model holder for model attributes
     * @param e the exception object
     *
     * @return the path to the error page
     */
    @ExceptionHandler(SpringNotInitialised::class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    fun notInitialised(model: Model, e: Exception): String {
        model.addAttribute("msg", e.javaClass.name)
        return "error"
    }

    /**
     * Handles requests which are only allowed to be ajax requests. Redirects
     * the user to the url supplied in the error message, typically this is
     * the page that contained the form/page element
     *
     * @param e the exception object
     *
     * @return the page to redirect to
     */
    @ExceptionHandler(NotAjaxRequest::class)
    fun notAjaxRequest(e: NotAjaxRequest): String {
        return "redirect:" + e.message + "?msg=ajax"
    }

    /**
     * Handles all generic/unknown errors
     *
     * @param model holder for model attributes
     * @param e the exception object
     *
     * @return the path to the error page
     */
    @ExceptionHandler(
        Throwable::class, LoadingHelpFailed::class
    )
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun runtimeError(model: Model?, e: Exception): String {
        //If running in dev mode, print the error
        if (listOf(*environment!!.activeProfiles).contains("dev")) {
            e.printStackTrace()
        }

        return "error"
    }

    /**
     * Handles all "not found" errors
     *
     * @param model holder for model attributes
     * @param authentication the authentication class
     * @param e the exception object
     *
     * @return the path to the error page
     */
    @ExceptionHandler(
        IWorkspaceNotFound::class,
        WorkspaceNotFound::class,
        TemplateNotFound::class,
        SubmissionNotFound::class,
        DetectorNotFound::class,
        ResultsNotFound::class,
        SourceFileNotFound::class,
        AccountNotFound::class,
        CompareSameSubmission::class
    )
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun notFoundError(model: Model, authentication: Authentication?, e: Exception): String {
        addAccountToModel(model, authentication).also {
            it.addAttribute("msg", e.javaClass.name)
        }
        return "error-default"
    }

    /**
     * Handles all requests which are not authorised
     *
     * @param model holder for model attributes
     * @param authentication the authentication class
     * @param e the exception object
     *
     * @return the path to the error page
     */
    @ExceptionHandler(
        NotTemplateOwner::class, AccountOwner::class
    )
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun notAuthorisedError(model: Model, authentication: Authentication?, e: Exception): String {
        addAccountToModel(model, authentication).also {
            it.addAttribute("msg", e.javaClass.name)
        }
        return "error-default"
    }

    /**
     * Handles all requests where the user tried to upload files that are larger than allowed
     *
     * @param model holder for model attributes
     * @param authentication the authentication class
     * @param e the exception object
     *
     * @return the path to the error page
     */
    @ExceptionHandler(
        MaxUploadSizeExceededException::class
    )
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    fun uploadSizeExceededError(model: Model, authentication: Authentication?, e: Exception): String {
        addAccountToModel(model, authentication).also {
            it.addAttribute("msg", e.javaClass.name)
        }
        return "error-default"
    }

    /**
     * Adds the account object of the current user to the display model
     *
     * @param model holder for model attributes
     * @param authentication the authentication class
     *
     * @return the model updated with the account object
     */
    private fun addAccountToModel(model: Model, authentication: Authentication?): Model {
        if (authentication != null) {
            model.addAttribute(
                "account",
                AccountWrapper(accountRepository!!.findByEmail(authentication.name))
            )
        }

        return model
    }
}
