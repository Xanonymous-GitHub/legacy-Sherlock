package uk.ac.warwick.dcs.sherlock.module.web.exceptions

/**
 * Thrown when a user attempts to modify their own account
 * through the admin account settings page
 */
class AccountOwner(errorMessage: String) : RuntimeException(errorMessage)