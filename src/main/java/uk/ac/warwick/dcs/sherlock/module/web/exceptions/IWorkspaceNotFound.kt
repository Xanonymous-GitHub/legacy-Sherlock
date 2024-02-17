package uk.ac.warwick.dcs.sherlock.module.web.exceptions

/**
 * Thrown if the workspace is not found in the engine
 */
class IWorkspaceNotFound(errorMessage: String) : RuntimeException(errorMessage)