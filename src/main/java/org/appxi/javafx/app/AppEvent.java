package org.appxi.javafx.app;

import org.appxi.event.Event;
import org.appxi.event.EventType;

public interface AppEvent {
    EventType<Event> STARTING = new EventType<>(Event.ANY);

    EventType<Event> STARTED = new EventType<>(Event.ANY);

    EventType<Event> STOPPING = new EventType<>(Event.ANY);
}
