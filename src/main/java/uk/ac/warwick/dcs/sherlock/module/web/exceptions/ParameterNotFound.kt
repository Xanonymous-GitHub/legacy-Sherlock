package uk.ac.warwick.dcs.sherlock.module.web.exceptions

/**
 * Thrown if the parameter is not found for the current user
 */
class ParameterNotFound(errorMessage: String) : RuntimeException(errorMessage)