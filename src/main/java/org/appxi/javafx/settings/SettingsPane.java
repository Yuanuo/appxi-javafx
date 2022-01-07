package org.appxi.javafx.settings;

import javafx.beans.DefaultProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.util.Callback;
import org.appxi.javafx.settings.skin.DefaultOptionEditorFactory;
import org.appxi.javafx.settings.skin.SettingsSkin;

@DefaultProperty("options")
public class SettingsPane extends Control {
    private final ObservableList<Option> options;
    private final SimpleObjectProperty<Callback<Option, OptionEditor<?>>> optionEditorFactory;
    private final SimpleBooleanProperty searchBoxVisible;
    private final StringProperty titleFilterProperty;
    private static final String DEFAULT_STYLE_CLASS = "settings-pane";

    public SettingsPane() {
        this(null);
    }

    public SettingsPane(ObservableList<Option> options) {
        this.optionEditorFactory = new SimpleObjectProperty(this, "optionEditor", new DefaultOptionEditorFactory<>());
        this.searchBoxVisible = new SimpleBooleanProperty(this, "searchBoxVisible", true);
        this.titleFilterProperty = new SimpleStringProperty(this, "titleFilter", "");
        this.getStyleClass().setAll(DEFAULT_STYLE_CLASS);
        this.options = options == null ? FXCollections.observableArrayList() : options;
    }

    public final SimpleObjectProperty<Callback<Option, OptionEditor<?>>> optionEditorFactory() {
        return this.optionEditorFactory;
    }

    public final Callback<Option, OptionEditor<?>> getOptionEditorFactory() {
        return this.optionEditorFactory.get();
    }

    public final void setOptionEditorFactory(Callback<Option, OptionEditor<?>> factory) {
        this.optionEditorFactory.set(factory == null ? new DefaultOptionEditorFactory() : factory);
    }

    public final SimpleBooleanProperty searchBoxVisibleProperty() {
        return this.searchBoxVisible;
    }

    public final boolean isSearchBoxVisible() {
        return this.searchBoxVisible.get();
    }

    public final void setSearchBoxVisible(boolean visible) {
        this.searchBoxVisible.set(visible);
    }

    public final StringProperty titleFilter() {
        return this.titleFilterProperty;
    }

    public final String getTitleFilter() {
        return this.titleFilterProperty.get();
    }

    public final void setTitleFilter(String filter) {
        this.titleFilterProperty.set(filter);
    }

    public ObservableList<Option> getOptions() {
        return this.options;
    }

    protected Skin<?> createDefaultSkin() {
        return new SettingsSkin(this);
    }
}
