package uk.ac.warwick.dcs.sherlock.module.web.exceptions

/**
 * Thrown when the "addMatch" method in LineMapper is called after
 * the "Fill" method has already ran
 */
class MapperException(errorMessage: String) : RuntimeException(errorMessage)