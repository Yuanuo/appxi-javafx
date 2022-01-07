package org.appxi.javafx.app;

import javafx.event.Event;
import javafx.event.EventType;

public class AppEvent extends Event {
    private static final long serialVersionUID = 603517436980812659L;

    public static final EventType<AppEvent> STARTING = new EventType<>(Event.ANY, "APP_STARTING");

    public static final EventType<AppEvent> STARTED = new EventType<>(Event.ANY, "APP_STARTED");

    public static final EventType<AppEvent> STOPPING = new EventType<>(Event.ANY, "APP_STOPPING");

    public AppEvent(EventType<? extends Event> eventType) {
        super(eventType);
    }
}
