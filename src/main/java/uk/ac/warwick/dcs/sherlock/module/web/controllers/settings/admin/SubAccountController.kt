package uk.ac.warwick.dcs.sherlock.module.web.controllers.settings.admin

import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import uk.ac.warwick.dcs.sherlock.module.web.configuration.SecurityConfig.Companion.generateRandomPassword
import uk.ac.warwick.dcs.sherlock.module.web.data.models.db.Role
import uk.ac.warwick.dcs.sherlock.module.web.data.models.forms.AccountForm
import uk.ac.warwick.dcs.sherlock.module.web.data.models.forms.PasswordForm
import uk.ac.warwick.dcs.sherlock.module.web.data.repositories.AccountRepository
import uk.ac.warwick.dcs.sherlock.module.web.data.repositories.RoleRepository
import uk.ac.warwick.dcs.sherlock.module.web.data.wrappers.AccountWrapper
import uk.ac.warwick.dcs.sherlock.module.web.exceptions.AccountNotFound
import uk.ac.warwick.dcs.sherlock.module.web.exceptions.AccountOwner

/**
 * The controller that deals with all the admin subaccount pages
 */
@Controller
class SubAccountController {
    @Autowired
    private val accountRepository: AccountRepository? = null

    @Autowired
    private val passwordEncoder: BCryptPasswordEncoder? = null

    @Autowired
    private val roleRepository: RoleRepository? = null

    /**
     * Handles GET requests to the manage account page
     *
     * @param subAccount the account being managed
     * @param model holder for model attributes
     *
     * @return the path to the manage account page
     */
    @GetMapping("/admin/manage/{pathid}")
    fun manageGet(
        @ModelAttribute("subAccount") subAccount: AccountWrapper,
        model: Model
    ): String {
        model.addAttribute("accountForm", AccountForm(subAccount.account))
        return "settings/admin/manage"
    }

    /**
     * Handles POST requests to the manage account page, updates the
     * username, email and roles based on the form contents
     *
     * @param accountForm the form that should be submitted in the request
     * @param result the results of the validation on the form above
     * @param subAccount the account being managed
     * @param account the account wrapper for the logged in user
     * @param model holder for model attributes
     *
     * @return the path to the manage account page
     */
    @PostMapping("/admin/manage/{pathid}")
    fun managePost(
        @ModelAttribute accountForm: @Valid AccountForm?,
        result: BindingResult,
        @ModelAttribute("subAccount") subAccount: AccountWrapper,
        @ModelAttribute("account") account: AccountWrapper?,
        model: Model
    ): String {
        if (!result.hasErrors()) {
            //Check that they are attempting to change the email
            if (accountForm!!.getEmail() != subAccount.email) {
                //if attempting to change, check that the email doesn't already exist
                if (accountRepository!!.findByEmail(accountForm.getEmail()) != null) {
                    result.reject("error.email.exists")
                    return "settings/admin/manage"
                }
            }

            subAccount.account.email = accountForm.getEmail()
            subAccount.account.username = accountForm.getName()
            accountRepository!!.save(subAccount.account)

            var isAdmin = false //whether the user is already an admin
            var adminRole: Role? = null //the admin role object for that account

            for (role in subAccount.roles) {
                if (role.name != null && role.name == "ADMIN") {
                    isAdmin = true
                    adminRole = role
                }
            }

            if (accountForm.isAdmin() && !isAdmin) { //Add admin role
                roleRepository!!.save(Role("ADMIN", subAccount.account))
            }

            if (!accountForm.isAdmin() && isAdmin) { //Remove admin role
                roleRepository!!.delete(adminRole!!)
            }

            model.addAttribute("success_msg", "admin.accounts.manage.updated")
        }

        accountForm!!.setOldPassword("")
        return "settings/admin/manage"
    }

    /**
     * Handles GET requests to the reset password page
     *
     * @param model holder for model attributes
     *
     * @return the path to the reset page
     */
    @GetMapping("/admin/password/{pathid}")
    fun passwordGet(model: Model): String {
        model.addAttribute("passwordForm", PasswordForm())
        return "settings/admin/password"
    }

    /**
     * Handles POST requests to the reset password page
     *
     * @param passwordForm the form that should be submitted in the request
     * @param result the results of the validation on the form above
     * @param subAccount the account being managed
     * @param account the account wrapper for the logged in user
     * @param model holder for model attributes
     *
     * @return the path to the reset password page
     */
    @PostMapping("/admin/password/{pathid}")
    fun passwordPost(
        @ModelAttribute passwordForm: @Valid PasswordForm?,
        result: BindingResult,
        @ModelAttribute("subAccount") subAccount: AccountWrapper,
        @ModelAttribute("account") account: AccountWrapper?,
        model: Model
    ): String {
        if (!result.hasErrors()) {
            //Generate a random password
            val newPassword = generateRandomPassword()

            subAccount.account.password = passwordEncoder!!.encode(newPassword)
            accountRepository!!.save(subAccount.account)

            model.addAttribute("success_msg", "admin.accounts.change_password.updated")
            model.addAttribute("newPassword", newPassword)
            return "settings/admin/passwordSuccess"
        }

        return "settings/admin/password"
    }

    /**
     * Handles GET requests to the delete account page
     *
     * @param model holder for model attributes
     *
     * @return the path to the delete page
     */
    @GetMapping("/admin/delete/{pathid}")
    fun deleteGet(model: Model): String {
        model.addAttribute("passwordForm", PasswordForm())
        return "settings/admin/delete"
    }

    /**
     * Handles POST requests to the delete account page
     *
     * @param passwordForm the form that should be submitted in the request
     * @param result the results of the validation on the form above
     * @param subAccount the account being managed
     * @param account the account wrapper for the logged in user
     * @param model holder for model attributes
     *
     * @return the path to the delete page
     */
    @PostMapping("/admin/delete/{pathid}")
    fun deletePost(
        @ModelAttribute passwordForm: @Valid PasswordForm?,
        result: BindingResult,
        @ModelAttribute("subAccount") subAccount: AccountWrapper,
        @ModelAttribute("account") account: AccountWrapper?,
        model: Model?
    ): String {
        if (!result.hasErrors()) {
            accountRepository!!.delete(subAccount.account)
            return "redirect:/admin?msg=deleted_account"
        }

        return "settings/admin/delete"
    }

    /**
     * Gets the account wrapper for the account where the id equals the
     * "pathid" attribute in the URL
     *
     * @param account the account object of the current user
     * @param pathid the account id from the path variable
     * @param model holder for model attributes
     *
     * @return the subaccount wrapper
     *
     * @throws AccountOwner if the subaccount is actually the account of the current user
     * @throws AccountNotFound if the account was not found
     */
    @ModelAttribute("subAccount")
    @Throws(AccountOwner::class, AccountNotFound::class)
    private fun getAccount(
        @ModelAttribute("account") account: AccountWrapper,
        @PathVariable(value = "pathid") pathid: Long,
        model: Model
    ): AccountWrapper {
        val optional = accountRepository!!.findById(pathid)

        if (optional.isEmpty) {
            throw AccountNotFound("Account not found")
        }

        val subAccount = AccountWrapper(optional.get())

        if (subAccount.id == account.id) {
            throw AccountOwner("You are not allowed to modify your own account.")
        }

        model.addAttribute("subAccount", subAccount)
        return subAccount
    }
}