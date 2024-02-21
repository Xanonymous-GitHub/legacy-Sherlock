package uk.ac.warwick.dcs.sherlock.module.web.exceptions

/**
 * Thrown if the help.properties file was not loaded successfully
 */
class LoadingHelpFailed(errorMessage: String) : RuntimeException(errorMessage)