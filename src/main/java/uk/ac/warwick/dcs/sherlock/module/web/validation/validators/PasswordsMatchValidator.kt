package uk.ac.warwick.dcs.sherlock.module.web.validation.validators

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import uk.ac.warwick.dcs.sherlock.module.web.data.models.forms.AccountPasswordForm
import uk.ac.warwick.dcs.sherlock.module.web.validation.annotations.PasswordsMatch

/**
 * Form validator that checks if the new password and confirm password
 * fields match
 */
class PasswordsMatchValidator : ConstraintValidator<PasswordsMatch, AccountPasswordForm> {
    override fun initialize(constraint: PasswordsMatch) {}

    /**
     * Performs the validation step to check if the two inputs are set
     * and then that they equal
     *
     * @param passwordForm the password form
     * @param context (not used here)
     *
     * @return whether the validation passed
     */
    override fun isValid(passwordForm: AccountPasswordForm, context: ConstraintValidatorContext): Boolean {
        if (passwordForm.getNewPassword() == null || passwordForm.getConfirmPassword() == null) {
            return false
        }
        return passwordForm.getNewPassword() == passwordForm.getConfirmPassword()
    }
}
