package uk.ac.warwick.dcs.sherlock.module.web.exceptions

import java.io.FileNotFoundException

/**
 * Thrown if the source file is not found in the current submission
 */
class SourceFileNotFound(errorMessage: String) : FileNotFoundException(errorMessage)