package org.appxi.javafx.settings;

import javafx.beans.property.Property;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.util.StringConverter;

import java.util.Optional;
import java.util.function.Function;

public interface Option<T> {
    String SEPARATOR = "Separator";

    Optional<Node> getGraphic();

    String getCaption();

    String getDescription();

    Optional<String> getExtendedDescription();

    String getCategory();

    Property<T> valueProperty();

    boolean isEditable();

    Optional<Function<Option<T>, OptionEditor<T>>> editorFactoryProperty();

    ObservableList<Option> getChildren();

    Optional<StringConverter<T>> getStringConverter();

    Orientation getLayout();
}
