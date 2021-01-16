package org.appxi.javafx.event;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.List;
import java.util.function.Consumer;

public interface EventHelper {
    /**
     * static methods for easy access
     */
    EventBus ONE = new EventBus();

    static <T extends Event> EventSubscriber addEventHandler(EventType<T> eventType, EventHandler<? super T> eventHandler) {
        return ONE.addEventHandler(eventType, eventHandler);
    }

    static <T extends Event> EventSubscriber addEventHandler(EventType<T> eventType, EventHandler<? super T> eventHandler,
                                                             List<EventSubscriber> manager) {
        final EventSubscriber subscriber = ONE.addEventHandler(eventType, eventHandler);
        if (!manager.contains(subscriber))
            manager.add(subscriber);
        return subscriber;
    }

    static <T extends Event> void removeEventHandler(EventType<T> eventType, EventHandler<? super T> eventHandler) {
        ONE.removeEventHandler(eventType, eventHandler);
    }

    static void fireEvent(Event event) {
        ONE.fireEvent(event);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    static void onKeyPressed(Node owner, KeyCode key, Consumer<KeyEvent> callback) {
        owner.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == key) callback.accept(event);
        });
    }

    static void onKeyReleased(Node owner, KeyCode key, Consumer<KeyEvent> callback) {
        owner.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            if (event.getCode() == key) callback.accept(event);
        });
    }
}
