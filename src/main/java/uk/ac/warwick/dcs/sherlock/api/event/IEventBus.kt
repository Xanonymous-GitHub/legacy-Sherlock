package uk.ac.warwick.dcs.sherlock.api.event

/**
 * Event bus interface, used to publish and subscrib to events
 */
interface IEventBus {
    /**
     * Publish an event to the bus
     *
     * @param event to publish
     */
    fun publishEvent(event: IEvent)

    /**
     * Attempts to register an object as an event subscriber, all methods with @EventHandler annotation will be registered
     *
     * @param subscriber instance of a class to register
     */
    fun registerEventSubscriber(subscriber: Any)
}
