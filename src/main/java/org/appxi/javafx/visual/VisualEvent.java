package org.appxi.javafx.visual;

import org.appxi.event.Event;
import org.appxi.event.EventType;

public final class VisualEvent extends Event {
    public static final EventType<VisualEvent> SET_STYLE = new EventType<>(Event.ANY, "SET_STYLE");
    public static final EventType<VisualEvent> SET_VISUAL = new EventType<>(SET_STYLE, "SET_VISUAL");
    public static final EventType<VisualEvent> SET_THEME = new EventType<>(SET_STYLE, "SET_THEME");
    public static final EventType<VisualEvent> SET_SWATCH = new EventType<>(SET_STYLE, "SET_SWATCH");

    //
    public static final EventType<VisualEvent> SET_WEB_STYLE = new EventType<>(Event.ANY, "SET_WEB_STYLE");

    public static final EventType<VisualEvent> SET_WEB_FONT = new EventType<>(SET_WEB_STYLE, "SET_WEB_FONT");
    public static final EventType<VisualEvent> SET_WEB_FONT_NAME = new EventType<>(SET_WEB_FONT, "SET_WEB_FONT_NAME");
    public static final EventType<VisualEvent> SET_WEB_FONT_SIZE = new EventType<>(SET_WEB_FONT, "SET_WEB_FONT_SIZE");

    public static final EventType<VisualEvent> SET_WEB_PAGE_COLOR = new EventType<>(SET_WEB_STYLE, "SET_WEB_PAGE_COLOR");
    public static final EventType<VisualEvent> SET_WEB_TEXT_COLOR = new EventType<>(SET_WEB_STYLE, "SET_WEB_TEXT_COLOR");

    public VisualEvent(EventType<? extends Event> eventType, Object data) {
        super(eventType, data);
    }
}
