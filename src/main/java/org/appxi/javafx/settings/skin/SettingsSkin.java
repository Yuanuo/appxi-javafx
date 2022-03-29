package org.appxi.javafx.settings.skin;

import javafx.beans.binding.Bindings;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SkinBase;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.appxi.javafx.settings.Option;
import org.appxi.javafx.settings.OptionBase;
import org.appxi.javafx.settings.OptionEditor;
import org.appxi.javafx.settings.OptionEditorBase;
import org.appxi.javafx.settings.SettingsPane;
import org.appxi.javafx.visual.MaterialIcon;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class SettingsSkin extends SkinBase<SettingsPane> {
    private final BorderPane content;
    private final ScrollPane scroller;
    private final ToolBar toolbar;

    public SettingsSkin(SettingsPane control) {
        super(control);
        TextField textField = new TextField();
        HBox.setHgrow(textField, Priority.ALWAYS);
        HBox searchField = new HBox(textField, MaterialIcon.SEARCH.iconButton((e) -> this.getSkinnable().setTitleFilter(textField.getText())));
        HBox.setHgrow(searchField, Priority.ALWAYS);
        this.toolbar = new ToolBar();
        this.toolbar.getItems().addAll(searchField);
        searchField.managedProperty().bind(searchField.visibleProperty());
        searchField.setVisible(this.getSkinnable().isSearchBoxVisible());
        this.getSkinnable().searchBoxVisibleProperty().addListener((o) -> {
            searchField.setVisible(this.getSkinnable().isSearchBoxVisible());
        });
        this.toolbar.visibleProperty().bind(searchField.visibleProperty());
        this.toolbar.managedProperty().bind(searchField.visibleProperty());
        this.scroller = new ScrollPane();
        this.scroller.setFitToWidth(true);
        this.content = new BorderPane();
        this.content.setTop(this.toolbar);
        this.content.setCenter(this.scroller);
        this.getChildren().add(this.content);
        this.refreshSettings();
        this.getSkinnable().optionEditorFactory().addListener((o) -> {
            this.refreshSettings();
        });
        this.getSkinnable().titleFilter().addListener((o) -> {
            this.refreshSettings();
        });
    }

    protected void layoutChildren(double x, double y, double w, double h) {
        this.content.resizeRelocate(x, y, w, h);
    }

    private void refreshSettings() {
        this.scroller.setContent(this.buildSettingsContainer(this.getSkinnable().getOptions()));
    }

    private VBox buildSettingsContainer(ObservableList<Option> options) {
        String search = this.getSkinnable().getTitleFilter().toLowerCase(Locale.ROOT);
        FilteredList<Option> filteredOptions = new FilteredList<>(options);
        filteredOptions.setPredicate(v -> search.isEmpty()
                || v.getCaption() != null && v.getCaption().toLowerCase(Locale.ROOT).contains(search)
                || v.getDescription() != null && v.getDescription().toLowerCase(Locale.ROOT).contains(search));
        boolean graphic = false;
        Map<String, List<OptionBase>> categoryMap = new TreeMap<>();

        for (Option o : filteredOptions) {
            if (o instanceof OptionBase optionBase) {
                String category = o.getCategory();
                if (category == null) {
                    category = "General";
                }

                categoryMap.computeIfAbsent(category, k -> new ArrayList<>()).add(optionBase);
                if (!graphic && o.getGraphic().isPresent()) {
                    graphic = true;
                }
            }
        }

        VBox grid = new VBox();
        grid.getStyleClass().add("options-grid");

        for (String category : categoryMap.keySet()) {
            HBox categoryBox = new HBox(new Label(category));
            categoryBox.getStyleClass().add("subheader");
            categoryBox.setAlignment(Pos.CENTER_LEFT);
            grid.getChildren().add(categoryBox);

            for (Option option : categoryMap.get(category)) {
                if (option.getCaption().equals("Separator")) {
                    HBox separatorBox = new HBox();
                    separatorBox.getStyleClass().add("separator");
                    grid.getChildren().add(separatorBox);
                    continue;
                }
                boolean hLayout = true;
                Node editor = this.getEditor(option);
                if (editor != null) {
                    if (!(editor instanceof Slider) && !Orientation.VERTICAL.equals(option.getLayout())) {
                        editor.getStyleClass().add("editor");
                    } else {
                        hLayout = false;
                        editor.getStyleClass().add("editor-large");
                    }
                }

                HBox rowOption = new HBox();
                rowOption.getStyleClass().add("option-row");
                if (option.getGraphic().isPresent()) {
                    HBox grLeft = new HBox();
                    grLeft.setAlignment(Pos.CENTER_LEFT);
                    grLeft.getStyleClass().add("primary-graphic");
                    grLeft.getChildren().add((Node) option.getGraphic().get());
                    rowOption.getChildren().add(grLeft);
                    HBox.setHgrow(grLeft, Priority.NEVER);
                }

                if (hLayout) {
                    Label title = new Label(option.getCaption());
                    title.getStyleClass().add("primary-text");
                    Label subtitle = new Label();
                    subtitle.getStyleClass().add("secondary-text");
                    subtitle.setAlignment(Pos.TOP_LEFT);
                    VBox.setVgrow(title, Priority.ALWAYS);
                    VBox textBox = new VBox(title, subtitle);
                    textBox.getStyleClass().setAll("text-box");
                    textBox.setAlignment(Pos.CENTER_LEFT);
                    HBox.setHgrow(textBox, Priority.ALWAYS);
                    rowOption.getChildren().add(textBox);
                    if (option.getExtendedDescription().isEmpty()) {
                        if (null != option.getDescription()) {
                            subtitle.setText(option.getDescription());
                            Tooltip.install(textBox, new Tooltip(option.getDescription()));
                        } else {
                            textBox.getChildren().remove(subtitle);
                        }
                    } else {
                        subtitle.textProperty().bind(Bindings.createStringBinding(() -> {
                            if (option.getStringConverter().isPresent()) {
                                StringConverter<Object> converter = (StringConverter) option.getStringConverter().get();
                                return converter.toString(option.valueProperty().getValue());
                            } else {
                                return option.valueProperty().getValue().toString();
                            }
                        }, option.valueProperty()));
                        Tooltip.install(textBox, new Tooltip("Click to find a more extensive description about " + option.getCaption()));
                    }

                    if (editor != null) {
                        if (option.getExtendedDescription().isEmpty()) {
                            HBox grRight = new HBox(editor);
                            grRight.setAlignment(Pos.CENTER);
                            HBox.setHgrow(grRight, Priority.NEVER);
                            grRight.getStyleClass().add("secondary-graphic");
                            rowOption.getChildren().add(grRight);
                        }
                    } else {
                        System.out.println("Error, no rendering options for: " + option.getCaption());
                    }
                } else {
                    Label title = new Label(option.getCaption());
                    title.getStyleClass().add("primary-text");
                    VBox grRight = new VBox(title, editor);
                    grRight.setAlignment(Pos.CENTER_LEFT);
                    HBox.setHgrow(grRight, Priority.ALWAYS);
                    grRight.getStyleClass().addAll("text-box", "secondary-graphic");
                    rowOption.getChildren().add(grRight);
                }

                grid.getChildren().add(rowOption);
            }
        }
        return grid;
    }

    private Node getEditor(Option option) {
        if (option.valueProperty() != null && option.valueProperty().getValue() != null) {
            OptionEditor editor = this.getSkinnable().getOptionEditorFactory().call(option);
            if (editor == null) {
                editor = new OptionEditorBase<String, TextField>(option, new TextField(), true) {
                    {
                        this.getEditor().setEditable(false);
                        this.getEditor().setDisable(true);
                    }

                    public StringProperty valueProperty() {
                        return this.getEditor().textProperty();
                    }

                    public void setValue(String value) {
                        this.getEditor().setText(value == null ? "" : value);
                    }
                };
            } else if (!option.isEditable()) {
                editor.getEditor().setDisable(true);
            }

            try {
                editor.setValue(option.valueProperty().getValue());
            } catch (ClassCastException var4) {
                editor.setValue(option.valueProperty().getValue().toString());
            }

            return editor.getEditor();
        } else {
            return null;
        }
    }
}
