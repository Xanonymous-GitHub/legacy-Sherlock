package uk.ac.warwick.dcs.sherlock.module.web.exceptions

/**
 * Thrown when the user tries to run a function that has not been finished
 */
class NotImplementedException(errorMessage: String) : NoSuchElementException(errorMessage)