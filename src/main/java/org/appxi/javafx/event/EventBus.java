package org.appxi.javafx.event;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Group;

/**
 * An event dispatcher that can be used for subscribing to events and posting
 * the events.
 */
public final class EventBus {
    private final Group eventHandlers = new Group();

    /**
     * Register event handler for event type.
     *
     * @param eventType    type
     * @param eventHandler handler
     * @param <T>          event
     */
    @SuppressWarnings("unchecked")
    public <T extends Event> EventSubscriber addEventHandler(EventType<T> eventType, EventHandler<? super T> eventHandler) {
        eventHandlers.addEventHandler(eventType, eventHandler);
        return new EventSubscriber(this, eventType, (EventHandler<? super Event>) eventHandler);
    }

    /**
     * Remove event handler for event type.
     *
     * @param eventType    type
     * @param eventHandler handler
     * @param <T>          event
     */
    public <T extends Event> void removeEventHandler(EventType<T> eventType, EventHandler<? super T> eventHandler) {
        eventHandlers.removeEventHandler(eventType, eventHandler);
    }

    /**
     * Post (fire) given event. All listening parties will be notified. Events will
     * be handled on the same thread that fired the event, i.e. synchronous.
     *
     * <p>
     * Note: according to JavaFX doc this must be called on JavaFX DesktopApplication
     * Thread. In reality this doesn't seem to be true.
     * </p>
     *
     * @param event the event
     */
    public void fireEvent(Event event) {
        eventHandlers.fireEvent(event);
    }
}