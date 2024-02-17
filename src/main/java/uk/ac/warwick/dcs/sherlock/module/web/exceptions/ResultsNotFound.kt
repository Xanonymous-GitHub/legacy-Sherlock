package uk.ac.warwick.dcs.sherlock.module.web.exceptions

/**
 * Thrown if the job id is not found for the current workspace
 */
class ResultsNotFound(errorMessage: String) : RuntimeException(errorMessage)