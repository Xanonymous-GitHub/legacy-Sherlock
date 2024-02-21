package uk.ac.warwick.dcs.sherlock.module.web.exceptions

/**
 * Thrown if a user attempts to modify a template that they are not
 * the owner of
 */
class NotTemplateOwner(errorMessage: String) : RuntimeException(errorMessage)