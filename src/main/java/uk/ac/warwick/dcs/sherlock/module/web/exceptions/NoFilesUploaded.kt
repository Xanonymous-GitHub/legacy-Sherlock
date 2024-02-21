package uk.ac.warwick.dcs.sherlock.module.web.exceptions

/**
 * Thrown if the user clicks submit on the "upload submissions"
 * page when they selected no files
 */
class NoFilesUploaded(errorMessage: String) : RuntimeException(errorMessage)