package uk.ac.warwick.dcs.sherlock.api.annotation

import uk.ac.warwick.dcs.sherlock.api.util.Side

/**
 * Marks a method as an event handler. If the containing class object is registered on the event bus, the method will receive events of the type of its required single parameter.
 * <br></br><br></br>
 * Set the side parameter to only receive events when sherlock is running as a server or a client
 */
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
annotation class EventHandler(
    /**
     * Is SherlockEngine running locally (Side.CLIENT) or on a server (Side.SERVER)
     * @return side
     */
    val side: Side = Side.UNKNOWN
)
