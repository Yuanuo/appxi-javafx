package org.appxi.javafx.event;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;

public final class EventSubscriber {
    private final EventBus bus;

    private final EventType<? extends Event> eventType;
    private final EventHandler<? super Event> eventHandler;

    EventSubscriber(EventBus bus, EventType<? extends Event> eventType, EventHandler<? super Event> eventHandler) {
        this.bus = bus;
        this.eventType = eventType;
        this.eventHandler = eventHandler;
    }

    /**
     * Stop listening for events.
     */
    public void unsubscribe() {
        bus.removeEventHandler(eventType, eventHandler);
    }
}