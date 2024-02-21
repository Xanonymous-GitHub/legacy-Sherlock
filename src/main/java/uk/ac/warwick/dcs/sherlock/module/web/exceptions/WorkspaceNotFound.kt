package uk.ac.warwick.dcs.sherlock.module.web.exceptions

/**
 * Thrown if the workspace is not found for the current user
 */
class WorkspaceNotFound(errorMessage: String) : RuntimeException(errorMessage)