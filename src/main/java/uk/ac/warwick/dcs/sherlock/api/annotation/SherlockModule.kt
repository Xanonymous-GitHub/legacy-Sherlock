package uk.ac.warwick.dcs.sherlock.api.annotation

import uk.ac.warwick.dcs.sherlock.api.util.Side

/**
 * Annotation to mark a module which should be register to the Sherlock engine on startup
 */
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class SherlockModule(
    /**
     * The name of the module, for easy identification
     * @return name
     */
    val name: String = "",
    /**
     * Is SherlockEngine running locally (Side.CLIENT) or on a server (Side.SERVER)
     * @return side
     */
    val side: Side = Side.UNKNOWN,
    /**
     * Version string for the module, help users track whether they are up to date
     * @return version
     */
    val version: String = ""
) {
    /**
     * Annotation for an instance variable if required by the module code
     * <br></br><br></br>
     * Create a variable with the type of the module class, and use this annotation, it will be populated with created instance of the module.
     */
    @MustBeDocumented
    @Retention(AnnotationRetention.RUNTIME)
    @Target(AnnotationTarget.FIELD)
    annotation class Instance
}
