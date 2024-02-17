package uk.ac.warwick.dcs.sherlock.module.web.exceptions

/**
 * Thrown if the file failed to upload when adding a submission
 */
class FileUploadFailed(errorMessage: String) : RuntimeException(errorMessage)