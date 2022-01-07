package org.appxi.javafx.visual;

import javafx.event.Event;
import javafx.event.EventType;

public final class VisualEvent extends Event {
    public static final EventType<VisualEvent> STYLE_CHANGED = new EventType<>(Event.ANY, "STYLE_CHANGED");
    public static final EventType<VisualEvent> VISUAL_CHANGED = new EventType<>(STYLE_CHANGED, "VISUAL_CHANGED");
    public static final EventType<VisualEvent> THEME_CHANGED = new EventType<>(STYLE_CHANGED, "THEME_CHANGED");
    public static final EventType<VisualEvent> SWATCH_CHANGED = new EventType<>(STYLE_CHANGED, "SWATCH_CHANGED");

    public static final EventType<VisualEvent> APP_FONT_CHANGED = new EventType<>(Event.ANY, "APP_FONT_CHANGED");

    public static final EventType<VisualEvent> WEB_ZOOM_CHANGED = new EventType<>(Event.ANY, "WEB_ZOOM_CHANGED");

    public final Object data;

    public VisualEvent(EventType<? extends Event> eventType, Object data) {
        super(eventType);
        this.data = data;
    }

    public <T> T data() {
        //noinspection unchecked
        return (T) data;
    }
}
