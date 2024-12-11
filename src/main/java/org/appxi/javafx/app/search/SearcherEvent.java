package org.appxi.javafx.app.search;

import org.appxi.event.Event;
import org.appxi.event.EventType;

public class SearcherEvent extends Event {
    public static final EventType<SearcherEvent> LOOKUP = new EventType<>(Event.ANY);

    public static final EventType<SearcherEvent> SEARCH = new EventType<>(Event.ANY);

    public final String text;

    public SearcherEvent(EventType<SearcherEvent> eventType, String text, Object scope) {
        super(eventType, scope);
        this.text = text;
    }

    public static SearcherEvent ofLookup(String text) {
        return new SearcherEvent(LOOKUP, text, null);
    }

    public static SearcherEvent ofSearch(String text) {
        return new SearcherEvent(SEARCH, text, null);
    }

    public static SearcherEvent ofSearch(String text, Object scope) {
        return new SearcherEvent(SEARCH, text, scope);
    }
}
