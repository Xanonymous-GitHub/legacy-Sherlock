package uk.ac.warwick.dcs.sherlock.module.web.controllers.settings.account

import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import uk.ac.warwick.dcs.sherlock.module.web.data.models.forms.AccountEmailForm
import uk.ac.warwick.dcs.sherlock.module.web.data.models.forms.AccountNameForm
import uk.ac.warwick.dcs.sherlock.module.web.data.models.forms.AccountPasswordForm
import uk.ac.warwick.dcs.sherlock.module.web.data.repositories.AccountRepository
import uk.ac.warwick.dcs.sherlock.module.web.data.wrappers.AccountWrapper
import uk.ac.warwick.dcs.sherlock.module.web.exceptions.NotAjaxRequest

/**
 * The controller that deals with account settings pages
 */
@Controller
class AccountController {
    @Autowired
    private val accountRepository: AccountRepository? = null

    @Autowired
    private val bCryptPasswordEncoder: BCryptPasswordEncoder? = null

    /**
     * Handles requests to the account page
     *
     * @return the path to the account page
     */
    @GetMapping("/account")
    fun indexGet(): String {
        return "settings/account/index"
    }

    /**
     * Handles GET requests to the name fragment on the account page
     *
     * @param model holder for model attributes
     * @param isAjax whether or not the request was ajax or not
     * @param account the account wrapper for the logged in user
     *
     * @return the path to the name fragment
     *
     * @throws NotAjaxRequest if the request was not an ajax one, the message is where to redirect the user to
     */
    @GetMapping("/account/name")
    @Throws(NotAjaxRequest::class)
    fun nameGetFragment(
        model: Model,
        @ModelAttribute("isAjax") isAjax: Boolean,
        @ModelAttribute("account") account: AccountWrapper
    ): String {
        if (!isAjax) throw NotAjaxRequest("/account")

        model.addAttribute("accountNameForm", AccountNameForm(account.account))
        return "settings/account/fragments/name"
    }

    /**
     * Handles POST requests to the name fragment on the account page
     *
     * @param isAjax whether or not the request was ajax or not
     * @param account the account wrapper for the logged in user
     * @param accountNameForm the form that should be submitted in the request
     * @param result the results of the validation on the form above
     * @param model holder for model attributes
     *
     * @return the path to the name fragment
     *
     * @throws NotAjaxRequest if the request was not an ajax one, the message is where to redirect the user to
     */
    @PostMapping("/account/name")
    @Throws(NotAjaxRequest::class)
    fun namePostFragment(
        @ModelAttribute("isAjax") isAjax: Boolean,
        @ModelAttribute("account") account: AccountWrapper,
        @ModelAttribute accountNameForm: @Valid AccountNameForm?,
        result: BindingResult,
        model: Model
    ): String {
        if (!isAjax) throw NotAjaxRequest("/account")

        if (!result.hasErrors()) {
            account.account.username = accountNameForm!!.getUsername()
            accountRepository!!.save(account.account)

            model.addAttribute("success_msg", "account.name.updated")
        }

        return "settings/account/fragments/name"
    }

    /**
     * Handles GET requests to the email fragment of the account page
     *
     * @param model holder for model attributes
     * @param isAjax whether or not the request was ajax or not
     * @param account the account wrapper for the logged in user
     *
     * @return the path to the email fragment
     *
     * @throws NotAjaxRequest if the request was not an ajax one, the message is where to redirect the user to
     */
    @GetMapping("/account/email")
    @Throws(NotAjaxRequest::class)
    fun emailGetFragment(
        model: Model,
        @ModelAttribute("isAjax") isAjax: Boolean,
        @ModelAttribute("account") account: AccountWrapper
    ): String {
        if (!isAjax) throw NotAjaxRequest("/account")

        model.addAttribute("accountEmailForm", AccountEmailForm(account.account))
        return "settings/account/fragments/email"
    }

    /**
     * Handles the POST requests to the email fragment on the accounts page
     *
     * @param isAjax whether the request was ajax or not
     * @param account the account wrapper for the logged in user
     * @param accountEmailForm the form that should be submitted in the request
     * @param result the results of the validation on the form above
     * @param model holder for model attributes
     *
     * @return the path to the email fragment
     *
     * @throws NotAjaxRequest if the request was not an ajax one, the message is where to redirect the user to
     */
    @PostMapping("/account/email")
    @Throws(NotAjaxRequest::class)
    fun emailPostFragment(
        @ModelAttribute("isAjax") isAjax: Boolean,
        @ModelAttribute("account") account: AccountWrapper,
        @ModelAttribute accountEmailForm: @Valid AccountEmailForm?,
        result: BindingResult,
        model: Model
    ): String {
        if (!isAjax) throw NotAjaxRequest("/account")

        if (!result.hasErrors()) {
            //Check that they are attempting to change the email
            if (accountEmailForm!!.getEmail() != account.email) {
                //if attempting to change, check that the email doesn't already exist
                if (accountRepository!!.findByEmail(accountEmailForm.getEmail()) != null) {
                    result.reject("error.email.exists")
                    return "settings/account/fragments/email"
                }
            }

            //TODO: Email old + new addresses
            //TODO: Perform email verification

            //Update the email in the database
            account.account.email = accountEmailForm.getEmail()
            accountRepository!!.save(account.account)

            //Update the email in the security session information to prevent the user being logged out.
            val authorities =
                SecurityContextHolder.getContext().authentication.authorities
            val authentication =
                UsernamePasswordAuthenticationToken(account.email, account.password, authorities)
            SecurityContextHolder.getContext().authentication = authentication

            model.addAttribute("success_msg", "account.email.updated")
        }

        accountEmailForm!!.setOldPassword("")

        return "settings/account/fragments/email"
    }

    /**
     * Handles GET requests to the password fragment on the account page
     *
     * @param model holder for model attributes
     * @param isAjax whether the request was ajax or not
     *
     * @return the path to the password fragment
     *
     * @throws NotAjaxRequest if the request was not an ajax one, the message is where to redirect the user to
     */
    @GetMapping("/account/password")
    @Throws(NotAjaxRequest::class)
    fun passwordGetFragment(
        model: Model,
        @ModelAttribute("isAjax") isAjax: Boolean
    ): String {
        if (!isAjax) throw NotAjaxRequest("/account")

        model.addAttribute("accountPasswordForm", AccountPasswordForm())
        return "settings/account/fragments/password"
    }

    /**
     * Handles POST requests to the password fragment on the accounts page
     *
     * @param isAjax whether the request was ajax or not
     * @param account the account wrapper for the logged-in user
     * @param accountPasswordForm the form that should be submitted in the request
     * @param result the results of the validation on the form above
     * @param model holder for model attributes
     *
     * @return the path to the password fragment
     *
     * @throws NotAjaxRequest if the request was not an ajax one, the message is where to redirect the user to
     */
    @PostMapping("/account/password")
    @Throws(NotAjaxRequest::class)
    fun passwordPostFragment(
        @ModelAttribute("isAjax") isAjax: Boolean,
        @ModelAttribute("account") account: AccountWrapper,
        @ModelAttribute accountPasswordForm: @Valid AccountPasswordForm?,
        result: BindingResult,
        model: Model
    ): String {
        if (!isAjax) throw NotAjaxRequest("/account")

        if (!result.hasErrors()) {
            account.account.password = bCryptPasswordEncoder!!.encode(accountPasswordForm!!.getNewPassword())
            accountRepository!!.save(account.account)

            model.addAttribute("accountPasswordForm", AccountPasswordForm())
            model.addAttribute("success_msg", "account.password.updated")
        }

        return "settings/account/fragments/password"
    }
}