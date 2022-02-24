package org.appxi.javafx.settings;

import javafx.scene.Node;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;

public class DefaultOptions<T> extends DefaultOption<T> {
    private Collection<T> values;

    public DefaultOptions(String caption) {
        super(caption);
    }

    public DefaultOptions(String caption, String description, String category) {
        super(caption, description, category);
    }

    public DefaultOptions(Node graphic, String caption, String description, String category) {
        super(graphic, caption, description, category);
    }

    public DefaultOptions(String caption, String description, String category, boolean isEditable) {
        super(caption, description, category, isEditable);
    }

    public DefaultOptions(String caption, String description, String category, boolean isEditable, Function<Option<T>, OptionEditor<T>> editorFactory) {
        super(caption, description, category, isEditable, editorFactory);
    }

    public DefaultOptions(Node graphic, String caption, String description, String category, boolean isEditable) {
        super(graphic, caption, description, category, isEditable);
    }

    public DefaultOptions(Node graphic, String caption, String description, String category, boolean isEditable, Function<Option<T>, OptionEditor<T>> editorFactory) {
        super(graphic, caption, description, category, isEditable, editorFactory);
    }

    public final Collection<T> getValues() {
        return values;
    }

    public final DefaultOptions<T> setValues(Collection<T> values) {
        this.values = values;
        return this;
    }

    public final DefaultOptions<T> setValues(T... values) {
        this.values = Arrays.asList(values);
        return this;
    }
}
