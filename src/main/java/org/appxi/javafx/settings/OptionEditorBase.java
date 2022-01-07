package org.appxi.javafx.settings;

import javafx.scene.Node;

public abstract class OptionEditorBase<T, C extends Node> implements OptionEditor<T> {
    private final Option<T> option;
    private final C control;

    public OptionEditorBase(Option<T> option, C control) {
        this(option, control, !option.isEditable());
    }

    public OptionEditorBase(Option<T> option, C control, boolean readonly) {
        this.control = control;
        this.option = option;
        if (!readonly) {
            this.valueProperty().bindBidirectional(option.valueProperty());
        }

    }

    public final Option<T> getOption() {
        return this.option;
    }

    public C getEditor() {
        return this.control;
    }

    public T getValue() {
        return this.valueProperty().getValue();
    }
}
