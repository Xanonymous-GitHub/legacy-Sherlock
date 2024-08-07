package uk.ac.warwick.dcs.sherlock.api.annotation

/**
 * Annotation to define a parameter as adjustable by the UI. Currently must be a float or int.
 * <br></br><br></br>
 * Can be used in classes which implement [uk.ac.warwick.dcs.sherlock.api.model.detection.IDetector] or [uk.ac.warwick.dcs.sherlock.api.model.postprocessing.IPostProcessor]
 * <br></br><br></br>
 * If another type is required please request it on https://github.com/DCS-Sherlock/Sherlock/issues
 * <br></br><br></br>
 * Set the parameter declaration to the desired default value
 * <br></br><br></br>
 * The engine will set this parameter to it's adjusted value when creating an instance of a supported object
 */
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class AdjustableParameter( // float and int supported currently
    // To add more supported types look in the engine.core.registry
    /**
     * default value the parameter takes
     *
     * @return the default value
     */
    val defaultValue: Float,
    /**
     * Optional, detailed description of what the parameter does
     *
     * @return the description string
     */
    val description: String = "",
    /**
     * The maximum bound for the field
     *
     * @return the max bound
     */
    val maximumBound: Float,
    /**
     * Minimum bound for field
     *
     * @return the min bound
     */
    val minimumBound: Float,
    /**
     * Name for the parameter to be displayed in the UI
     *
     * @return the parameter name
     */
    val name: String,
    /**
     * The step to increment or decrement the parameter by in the UI
     *
     * @return the parameter step
     */
    val step: Float
)
