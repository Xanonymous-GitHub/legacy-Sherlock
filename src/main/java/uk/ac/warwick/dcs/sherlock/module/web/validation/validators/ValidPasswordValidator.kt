package uk.ac.warwick.dcs.sherlock.module.web.validation.validators

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import uk.ac.warwick.dcs.sherlock.module.web.data.repositories.AccountRepository
import uk.ac.warwick.dcs.sherlock.module.web.validation.annotations.ValidPassword

/**
 * Form validator that checks if the password supplied matches that
 * of the current user
 */
class ValidPasswordValidator : ConstraintValidator<ValidPassword, String> {
    //All @Autowired variables are automatically loaded by Spring
    @Autowired
    private val accountRepository: AccountRepository? = null

    @Autowired
    private val passwordEncoder: BCryptPasswordEncoder? = null

    override fun initialize(constraint: ValidPassword) {}

    /**
     * Performs the validation step by fetching the account from the
     * repository, encoding the supplied password and checking that
     * against the encoded password stored in the database
     *
     * @param password the plaintext password from the form
     * @param context (not used here)
     *
     * @return whether the validation passed
     */
    override fun isValid(password: String, context: ConstraintValidatorContext): Boolean {
        val authentication = SecurityContextHolder.getContext().authentication ?: return false

        val account = accountRepository!!.findByEmail(authentication.name)

        return passwordEncoder!!.matches(password, account!!.password)
    }
}
