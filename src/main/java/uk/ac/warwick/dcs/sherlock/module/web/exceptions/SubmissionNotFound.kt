package uk.ac.warwick.dcs.sherlock.module.web.exceptions

/**
 * Thrown if the submission is not found in the current workspace
 */
class SubmissionNotFound(errorMessage: String) : RuntimeException(errorMessage)