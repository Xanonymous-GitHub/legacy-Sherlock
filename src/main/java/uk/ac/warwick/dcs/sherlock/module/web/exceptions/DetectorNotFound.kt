package uk.ac.warwick.dcs.sherlock.module.web.exceptions

/**
 * Thrown when the detector was not found
 */
class DetectorNotFound(errorMessage: String) : RuntimeException(errorMessage)