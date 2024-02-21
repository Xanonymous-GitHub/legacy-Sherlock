package uk.ac.warwick.dcs.sherlock.module.web.validation.validators

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import uk.ac.warwick.dcs.sherlock.api.registry.SherlockRegistry
import uk.ac.warwick.dcs.sherlock.module.web.validation.annotations.ValidLanguage

/**
 * Form validator that checks if the language supplied is
 * valid (i.e. in the registry of supported languages)
 */
class ValidLanguageValidator : ConstraintValidator<ValidLanguage, String?> {
    override fun initialize(constraint: ValidLanguage) {}

    /**
     * Performs the validation step by checking that the language
     * is set and then that it is in the list of languages in the
     * Sherlock Registry
     *
     * @param language the language to check
     * @param context (not used here)
     *
     * @return whether the validation passed
     */
    override fun isValid(language: String?, context: ConstraintValidatorContext): Boolean {
        return language != null && SherlockRegistry.getLanguages().contains(language)
    }
}
