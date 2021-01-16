package org.appxi.javafx.theme;

import javafx.event.Event;
import javafx.event.EventType;

public final class ThemeEvent extends Event {
    public static final EventType<ThemeEvent> CHANGED = new EventType<>(Event.ANY, "THEME_CHANGED");

    public final Theme oldTheme, newTheme;

    public ThemeEvent(Theme oldTheme, Theme newTheme) {
        this(CHANGED, oldTheme, newTheme);
    }

    public ThemeEvent(EventType<? extends Event> eventType, Theme oldTheme, Theme newTheme) {
        super(eventType);
        this.oldTheme = oldTheme;
        this.newTheme = newTheme;
    }
}
