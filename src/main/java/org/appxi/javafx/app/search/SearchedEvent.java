package org.appxi.javafx.app.search;

import org.appxi.event.Event;
import org.appxi.event.EventType;

public class SearchedEvent extends Event {
    public static final EventType<SearchedEvent> OPEN = new EventType<>(Event.ANY);

    public final String highlightTerm, highlightSnippet;

    public SearchedEvent(Object piece) {
        this(piece, null, null);
    }

    public SearchedEvent(Object piece, String highlightTerm, String highlightSnippet) {
        super(OPEN, piece);
        this.highlightTerm = highlightTerm;
        this.highlightSnippet = highlightSnippet;
    }
}
