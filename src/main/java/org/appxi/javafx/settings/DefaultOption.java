package org.appxi.javafx.settings;

import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.Node;

import java.util.function.Function;

public class DefaultOption<T> extends OptionBase<T> {
    public DefaultOption(String caption) {
        this(null, caption, null, null, false, null);
    }

    public DefaultOption(String caption, String description, String category) {
        this(null, caption, description, category, false, null);
    }

    public DefaultOption(Node graphic, String caption, String description, String category) {
        this(graphic, caption, description, category, false, null);
    }

    public DefaultOption(String caption, String description, String category, boolean isEditable) {
        this(null, caption, description, category, isEditable, null);
    }

    public DefaultOption(String caption, String description, String category, boolean isEditable, Function<Option<T>, OptionEditor<T>> editorFactory) {
        this(null, caption, description, category, isEditable, editorFactory);
    }

    public DefaultOption(Node graphic, String caption, String description, String category, boolean isEditable) {
        this(graphic, caption, description, category, isEditable, null);
    }

    public DefaultOption(Node graphic, String caption, String description, String category, boolean isEditable, Function<Option<T>, OptionEditor<T>> editorFactory) {
        super(graphic, caption, description, category);

        this.isEditable = isEditable;
        this.editorFactory = editorFactory;
    }

    public Property<T> valueProperty() {
        return this.value;
    }

    public final DefaultOption<T> setValue(T value) {
        this.value = new ReadOnlyObjectWrapper<>(value);
        return this;
    }

    public final DefaultOption<T> setValueProperty(Property<T> value) {
        this.value = value;
        return this;
    }
}
