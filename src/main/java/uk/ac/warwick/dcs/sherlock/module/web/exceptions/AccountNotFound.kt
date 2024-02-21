package uk.ac.warwick.dcs.sherlock.module.web.exceptions

/**
 * Thrown when the account is not found on the admin settings page
 */
class AccountNotFound(errorMessage: String) : RuntimeException(errorMessage)