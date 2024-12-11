package org.appxi.javafx.visual;

import org.appxi.event.Event;
import org.appxi.event.EventType;

public interface VisualEvent {
    EventType<Event> SET_STYLE = new EventType<>(Event.ANY);
    EventType<Event> SET_VISUAL = new EventType<>(SET_STYLE);
    EventType<Event> SET_THEME = new EventType<>(SET_STYLE);
    EventType<Event> SET_SWATCH = new EventType<>(SET_STYLE);

    //
    EventType<Event> SET_WEB_STYLE = new EventType<>(Event.ANY);

    EventType<Event> SET_WEB_FONT = new EventType<>(SET_WEB_STYLE);
    EventType<Event> SET_WEB_FONT_NAME = new EventType<>(SET_WEB_FONT);
    EventType<Event> SET_WEB_FONT_SIZE = new EventType<>(SET_WEB_FONT);

    EventType<Event> SET_WEB_PAGE_COLOR = new EventType<>(SET_WEB_STYLE);
    EventType<Event> SET_WEB_TEXT_COLOR = new EventType<>(SET_WEB_STYLE);
}
