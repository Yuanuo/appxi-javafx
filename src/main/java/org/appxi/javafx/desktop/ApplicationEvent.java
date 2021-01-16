package org.appxi.javafx.desktop;

import javafx.event.Event;
import javafx.event.EventType;

public class ApplicationEvent extends Event {
    private static final long serialVersionUID = 1L;

    public static final EventType<ApplicationEvent> STARTING = new EventType<>(Event.ANY, "APP_STARTING");

    public static final EventType<ApplicationEvent> STARTED = new EventType<>(Event.ANY, "APP_STARTED");

    public static final EventType<ApplicationEvent> STOPPING = new EventType<>(Event.ANY, "APP_STOPPING");

    public ApplicationEvent(EventType<? extends Event> eventType) {
        super(eventType);
    }
}
