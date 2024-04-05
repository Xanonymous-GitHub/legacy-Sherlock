package uk.ac.warwick.dcs.sherlock.module.web.controllers.settings.admin

import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import uk.ac.warwick.dcs.sherlock.module.web.configuration.SecurityConfig.Companion.generateRandomPassword
import uk.ac.warwick.dcs.sherlock.module.web.data.models.db.Account
import uk.ac.warwick.dcs.sherlock.module.web.data.models.db.Role
import uk.ac.warwick.dcs.sherlock.module.web.data.models.forms.AccountForm
import uk.ac.warwick.dcs.sherlock.module.web.data.repositories.AccountRepository
import uk.ac.warwick.dcs.sherlock.module.web.data.repositories.RoleRepository
import uk.ac.warwick.dcs.sherlock.module.web.data.wrappers.AccountWrapper
import uk.ac.warwick.dcs.sherlock.module.web.exceptions.NotAjaxRequest

/**
 * The controller that deals with the admin settings pages
 */
@Controller
class AdminController {
    @Autowired
    private val accountRepository: AccountRepository? = null

    @Autowired
    private val passwordEncoder: BCryptPasswordEncoder? = null

    @Autowired
    private val roleRepository: RoleRepository? = null

    /**
     * Handles all requests to the admin page
     *
     * @return the path to the admin page
     */
    @GetMapping("/admin")
    fun indexGet(): String {
        return "settings/admin/index"
    }

    /**
     * Handles GET requests to the list fragment on the admin page
     *
     * @param isAjax whether the request was ajax or not
     * @param model holder for model attributes
     *
     * @return the path to the list fragment
     *
     * @throws NotAjaxRequest if the request was not an ajax one, the message is where to redirect the user to
     */
    @GetMapping("/admin/list")
    @Throws(NotAjaxRequest::class)
    fun listGetFragment(
        @ModelAttribute("isAjax") isAjax: Boolean,
        model: Model
    ): String {
        if (!isAjax) throw NotAjaxRequest("/admin")

        model.addAttribute("accounts", accountRepository!!.findAll())
        return "settings/admin/fragments/list"
    }

    /**
     * Handles GET requests to the add account page
     *
     * @param model holder for model attributes
     *
     * @return the path to the add account page
     */
    @GetMapping("/admin/add")
    fun addGet(model: Model): String {
        model.addAttribute("accountForm", AccountForm())
        return "settings/admin/add"
    }

    /**
     * Handles POST requests to the add account page, creating a new account
     * with a random password if validation succeeds
     *
     * @param account the account wrapper for the logged in user
     * @param accountForm the form that should be submitted in the request
     * @param result the results of the validation on the form above
     * @param model holder for model attributes
     *
     * @return the path to the add account page, or the password page if an
     * account is added
     */
    @PostMapping("/admin/add")
    fun addPost(
        @ModelAttribute("account") account: AccountWrapper?,
        @ModelAttribute accountForm: @Valid AccountForm?,
        result: BindingResult,
        model: Model
    ): String {
        if (!result.hasErrors()) {
            if (accountRepository!!.findByEmail(accountForm!!.getEmail()) == null) {
                //Generate a random password
                val newPassword = generateRandomPassword()

                val newAccount = Account(
                    accountForm.getEmail(),
                    passwordEncoder!!.encode(newPassword),
                    accountForm.getName()
                )

                accountRepository.save(newAccount)

                roleRepository!!.save(Role("USER", newAccount))
                if (accountForm.isAdmin()) {
                    roleRepository.save(Role("ADMIN", newAccount))
                }

                model.addAttribute("success_msg", "admin.accounts.password.start")
                model.addAttribute("newPassword", newPassword)
                return "settings/admin/passwordSuccess"
            } else {
                result.reject("error.email.exists")
            }
        }

        return "settings/admin/add"
    }
}
