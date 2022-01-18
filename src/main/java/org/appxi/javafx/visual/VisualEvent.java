package org.appxi.javafx.visual;

import javafx.event.Event;
import javafx.event.EventType;

public final class VisualEvent extends Event {
    public static final EventType<VisualEvent> SET_STYLE = new EventType<>(Event.ANY, "SET_STYLE");
    public static final EventType<VisualEvent> SET_VISUAL = new EventType<>(SET_STYLE, "SET_VISUAL");
    public static final EventType<VisualEvent> SET_THEME = new EventType<>(SET_STYLE, "SET_THEME");
    public static final EventType<VisualEvent> SET_SWATCH = new EventType<>(SET_STYLE, "SET_SWATCH");

    public static final EventType<VisualEvent> SET_FONT = new EventType<>(Event.ANY, "SET_FONT");
    public static final EventType<VisualEvent> SET_FONT_NAME = new EventType<>(SET_FONT, "SET_FONT_NAME");
    public static final EventType<VisualEvent> SET_FONT_SIZE = new EventType<>(SET_FONT, "SET_FONT_SIZE");

    public static final EventType<VisualEvent> SET_WEB_FONT = new EventType<>(Event.ANY, "SET_WEB_FONT");
    public static final EventType<VisualEvent> SET_WEB_FONT_NAME = new EventType<>(SET_WEB_FONT, "SET_WEB_FONT_NAME");
    public static final EventType<VisualEvent> SET_WEB_FONT_SIZE = new EventType<>(SET_WEB_FONT, "SET_WEB_FONT_SIZE");

    private final Object data;

    public VisualEvent(EventType<? extends Event> eventType, Object data) {
        super(eventType);
        this.data = data;
    }

    public <T> T data() {
        //noinspection unchecked
        return (T) data;
    }
}
