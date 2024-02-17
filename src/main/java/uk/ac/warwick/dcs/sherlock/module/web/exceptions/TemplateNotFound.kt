package uk.ac.warwick.dcs.sherlock.module.web.exceptions

/**
 * Thrown if the template is not found for the current user
 */
class TemplateNotFound(errorMessage: String) : RuntimeException(errorMessage)