package org.appxi.javafx.settings;

import javafx.beans.property.Property;
import javafx.scene.Node;

public interface OptionEditor<T> {
    Node getEditor();

    Property<T> valueProperty();

    T getValue();

    void setValue(T var1);
}
