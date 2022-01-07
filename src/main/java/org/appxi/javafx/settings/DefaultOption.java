package org.appxi.javafx.settings;

import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.Node;

import java.util.function.Function;

public class DefaultOption<T> extends OptionBase<T> {
    public DefaultOption(String caption) {
        this(null, caption, null, null, null, false, null);
    }

    public DefaultOption(String caption, String description, String category) {
        this(null, caption, description, category, null, false, null);
    }

    public DefaultOption(Node graphic, String caption, String description, String category) {
        this(graphic, caption, description, category, null, false, null);
    }

    public DefaultOption(String caption, String description, String category, T value, boolean isEditable) {
        this(null, caption, description, category, value, isEditable, null);
    }

    public DefaultOption(String caption, String description, String category, T value, boolean isEditable, Function<Option<T>, OptionEditor<T>> editorFactory) {
        this(null, caption, description, category, value, isEditable, editorFactory);
    }

    public DefaultOption(Node graphic, String caption, String description, String category, T value, boolean isEditable) {
        this(graphic, caption, description, category, value, isEditable, null);
    }

    public DefaultOption(Node graphic, String caption, String description, String category, T value, boolean isEditable, Function<Option<T>, OptionEditor<T>> editorFactory) {
        super(graphic, caption, description, category);

        try {
            if (value instanceof Property) {
                this.value = (Property) value;
            } else {
                this.value = new ReadOnlyObjectWrapper(value);
            }
        } catch (Exception var9) {
            System.out.println("Error " + var9);
        }

        this.isEditable = isEditable;
        this.editorFactory = editorFactory;
    }

    public Property<T> valueProperty() {
        return this.value;
    }
}
