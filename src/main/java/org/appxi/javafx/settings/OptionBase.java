package org.appxi.javafx.settings;

import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.util.StringConverter;

import java.util.Optional;
import java.util.function.Function;

public abstract class OptionBase<T> implements Option<T> {
    protected String caption;
    protected String description;
    protected String category;
    protected Node graphic;
    protected Property<T> value;
    protected boolean isEditable;
    protected Function<Option<T>, OptionEditor<T>> editorFactory;
    protected String extendedDescription;
    protected StringConverter<T> stringConverter;
    protected Orientation layout;
    private final StringProperty id;

    protected OptionBase(String caption) {
        this(null, caption, null, null, null, false, null);
    }

    protected OptionBase(String caption, String description, String category) {
        this(null, caption, description, category, null, false, null);
    }

    protected OptionBase(Node graphic, String caption, String description, String category) {
        this(graphic, caption, description, category, null, false, null);
    }

    protected OptionBase(String caption, String description, String category, Property<T> value, boolean isEditable) {
        this(null, caption, description, category, value, isEditable, null);
    }

    protected OptionBase(String caption, String description, String category, Property<T> value, boolean isEditable, Function<Option<T>, OptionEditor<T>> editorFactory) {
        this(null, caption, description, category, value, isEditable, editorFactory);
    }

    protected OptionBase(Node graphic, String caption, String description, String category, Property<T> value, boolean isEditable) {
        this(graphic, caption, description, category, value, isEditable, null);
    }

    protected OptionBase(Node graphic, String caption, String description, String category, Property<T> value, boolean isEditable, Function<Option<T>, OptionEditor<T>> editorFactory) {
        this.id = new SimpleStringProperty();
        this.graphic = graphic;
        this.caption = caption;
        this.description = description;
        this.category = category;
        this.value = value;
        this.isEditable = isEditable;
        this.editorFactory = editorFactory;
        this.layout = Orientation.HORIZONTAL;
    }

    public Optional<Node> getGraphic() {
        return Optional.ofNullable(this.graphic);
    }

    public String getCaption() {
        return this.caption;
    }

    public String getDescription() {
        return this.description;
    }

    public Optional<String> getExtendedDescription() {
        return Optional.ofNullable(this.extendedDescription);
    }

    public void setExtendedDescription(String value) {
        this.extendedDescription = value;
    }

    public String getCategory() {
        return this.category;
    }

    public abstract Property<T> valueProperty();

    public Optional<Function<Option<T>, OptionEditor<T>>> editorFactoryProperty() {
        return Optional.ofNullable(this.editorFactory);
    }

    public boolean isEditable() {
        return this.isEditable;
    }

    public void setStringConverter(StringConverter<T> value) {
        this.stringConverter = value;
    }

    public Optional<StringConverter<T>> getStringConverter() {
        return Optional.ofNullable(this.stringConverter);
    }

    public static <T> Property<T> valueProperty(T value) {
        return new ReadOnlyObjectWrapper<>(value);
    }

    public Orientation getLayout() {
        return this.layout;
    }

    public void setLayout(Orientation layout) {
        this.layout = layout;
    }

    public final StringProperty idProperty() {
        return this.id;
    }

    public final void setId(String id) {
        this.id.setValue(id);
    }

    public final String getId() {
        return this.id.get();
    }
}
