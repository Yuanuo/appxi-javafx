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
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class SettingsSkin extends SkinBase<SettingsPane> {
    private final BorderPane content;
    private final ScrollPane scroller;
    private final ToolBar toolbar;
    private int level = 0;
//    private static final Function<View, MobileTransition> VIEW_TRANSITION = (view) -> {
//        return new FadeInDownBigTransition(view, false);
//    };

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
        filteredOptions.setPredicate((optionx) -> {
            return search.isEmpty() || optionx.getCaption() != null && optionx.getCaption().toLowerCase(Locale.ROOT).contains(search) || optionx.getDescription() != null && optionx.getDescription().toLowerCase(Locale.ROOT).contains(search);
        });
        boolean graphic = false;
        Map<String, List<OptionBase>> categoryMap = new TreeMap<>();

        for (Option o : filteredOptions) {
            if (o instanceof OptionBase) {
                String category = o.getCategory();
                if (category == null) {
                    category = "";
                }

                List<OptionBase> list = categoryMap.get(category);
                if (list == null) {
                    list = new ArrayList();
                    categoryMap.put(category, list);
                }

                list.add((OptionBase) o);
                if (!graphic && o.getGraphic().isPresent()) {
                    graphic = true;
                }
            }
        }

        VBox grid = new VBox();
        grid.getStyleClass().add("options-grid");
        Iterator var19 = filteredOptions.iterator();

        while (true) {
            while (var19.hasNext()) {
                Option option = (Option) var19.next();
                String category = option.getCategory() == null ? "" : option.getCategory();
                List<OptionBase> list = categoryMap.get(category);
                HBox hBox;
                if (list.size() > 0 && list.get(0).equals(option) && !category.isEmpty()) {
                    hBox = new HBox(new Label(category));
                    hBox.getStyleClass().add("subheader");
                    hBox.setAlignment(Pos.CENTER_LEFT);
                    grid.getChildren().add(hBox);
                }

                if (option.getCaption().equals("Separator")) {
                    hBox = new HBox();
                    hBox.getStyleClass().add("separator");
                    grid.getChildren().add(hBox);
                } else {
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

                    Label labelTitle;
                    if (hLayout) {
                        labelTitle = new Label(option.getCaption());
                        labelTitle.getStyleClass().add("primary-text");
                        Label labelSubtitle = new Label();
                        labelSubtitle.getStyleClass().add("secondary-text");
                        labelSubtitle.setAlignment(Pos.TOP_LEFT);
                        VBox.setVgrow(labelTitle, Priority.ALWAYS);
                        VBox textBox = new VBox(labelTitle, labelSubtitle);
                        textBox.getStyleClass().setAll("text-box");
                        textBox.setAlignment(Pos.CENTER_LEFT);
                        HBox.setHgrow(textBox, Priority.ALWAYS);
                        rowOption.getChildren().add(textBox);
                        if (!option.getExtendedDescription().isPresent()) {
                            labelSubtitle.setText(option.getDescription());
                            Tooltip.install(textBox, new Tooltip(option.getDescription()));
                        } else {
                            labelSubtitle.textProperty().bind(Bindings.createStringBinding(() -> {
                                if (option.getStringConverter().isPresent()) {
                                    StringConverter<Object> converter = (StringConverter) option.getStringConverter().get();
                                    return converter.toString(option.valueProperty().getValue());
                                } else {
                                    return option.valueProperty().getValue().toString();
                                }
                            }, option.valueProperty()));
                            Tooltip.install(textBox, new Tooltip("Click to find a more extensive description about " + option.getCaption()));
                        }

                        String viewName;
                        if (editor != null) {
                            if (!option.getExtendedDescription().isPresent()) {
                                HBox grRight = new HBox(editor);
                                grRight.setAlignment(Pos.CENTER);
                                HBox.setHgrow(grRight, Priority.NEVER);
                                grRight.getStyleClass().add("secondary-graphic");
                                rowOption.getChildren().add(grRight);
                            } else {
                                viewName = "Extended_View_" + option.getCaption() + "_" + this.level + "_" + rowOption.getChildren().size();
//                                AppManager.getInstance().addViewFactory(viewName, () -> {
//                                    View view = this.getExtendedView(option);
////                                    view.setShowTransitionFactory(VIEW_TRANSITION);
//                                    return view;
//                                });
                                rowOption.setOnMouseClicked((e) -> {
//                                    AppManager.getInstance().switchView(viewName);
                                });
                            }
                        } else if (!option.getChildren().isEmpty()) {
                            viewName = "Group_View_" + option.getCaption() + "_" + this.level + "_" + rowOption.getChildren().size();
//                            AppManager.getInstance().addViewFactory(viewName, () -> {
//                                View view = this.getGroupView(option.getCaption(), this.buildSettingsContainer(option.getChildren()));
////                                view.setShowTransitionFactory(VIEW_TRANSITION);
//                                return view;
//                            });
                            rowOption.setOnMouseClicked((e) -> {
//                                AppManager.getInstance().switchView(viewName);
                            });
                        } else {
                            System.out.println("Error, no rendering options for: " + option.getCaption());
                        }
                    } else {
                        labelTitle = new Label(option.getCaption());
                        labelTitle.getStyleClass().add("primary-text");
                        VBox grRight = new VBox(labelTitle, editor);
                        grRight.setAlignment(Pos.CENTER_LEFT);
                        HBox.setHgrow(grRight, Priority.ALWAYS);
                        grRight.getStyleClass().addAll("text-box", "secondary-graphic");
                        rowOption.getChildren().add(grRight);
                    }

                    grid.getChildren().add(rowOption);
                }
            }

            ++this.level;
            return grid;
        }
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

//    private View getGroupView(final String caption, VBox vBox) {
//        BorderPane pane = new BorderPane();
//        pane.getStyleClass().add("settings-pane");
//        final Button btnBack = MaterialIcon.ARROW_BACK.button((e) -> {
//            AppManager.getInstance().switchToPreviousView();
//        });
//        final Button btnSearch = MaterialIcon.SEARCH.button();
//        pane.setCenter(vBox);
//        View view = new View(pane) {
//            protected void updateAppBar(AppBar appBar) {
//                appBar.setNavIcon(btnBack);
//                appBar.setTitleText(caption);
//                appBar.getActionItems().add(btnSearch);
//            }
//        };
//        return view;
//    }

//    private View getExtendedView(final Option option) {
//        BorderPane pane = new BorderPane();
//        pane.getStyleClass().add("settings-pane");
//        final Button btnBack = MaterialIcon.ARROW_BACK.button((e) -> {
//            AppManager.getInstance().switchToPreviousView();
//        });
//        final Button btnSearch = MaterialIcon.SEARCH.button();
//        HBox top = new HBox();
//        top.setAlignment(Pos.CENTER);
//        top.getStyleClass().add("extended-top");
//        Node editor = this.getEditor(option);
//        editor.getStyleClass().add("editor");
//        Label label = new Label();
//        label.textProperty().bind(Bindings.createStringBinding(() -> {
//            if (option.getStringConverter().isPresent()) {
//                StringConverter<Object> converter = (StringConverter) option.getStringConverter().get();
//                return converter.toString(option.valueProperty().getValue());
//            } else {
//                return option.valueProperty().getValue().toString();
//            }
//        }, option.valueProperty()));
//        Region spacer2 = new Region();
//        HBox.setHgrow(spacer2, Priority.ALWAYS);
//        top.getChildren().addAll(label, spacer2, editor);
//        BorderPane paneContent = new BorderPane();
//        paneContent.getStyleClass().add("extended-pane");
//        paneContent.setTop(top);
//        Text text = new Text();
//        if (option.getExtendedDescription().isPresent()) {
//            text.setText((String) option.getExtendedDescription().get());
//        }
//
//        text.getStyleClass().add("extended-text");
//        TextFlow textFlow = new TextFlow(text) {
//            protected void layoutChildren() {
//                super.layoutChildren();
//                double maxChildHeight = 0.0D;
//
//                Node child;
//                for (Iterator var3 = this.getManagedChildren().iterator(); var3.hasNext(); maxChildHeight = Math.max(maxChildHeight, child.getLayoutBounds().getHeight())) {
//                    child = (Node) var3.next();
//                }
//
//                this.setMaxHeight(maxChildHeight + this.getInsets().getTop() + this.getInsets().getBottom());
//            }
//        };
//        HBox hBox = new HBox(textFlow);
//        hBox.setAlignment(Pos.CENTER);
//        hBox.getStyleClass().add("extended-center");
//        paneContent.setCenter(hBox);
//        pane.setCenter(paneContent);
//        View view = new View(pane) {
//            protected void updateAppBar(AppBar appBar) {
//                appBar.setNavIcon(btnBack);
//                appBar.setTitleText(option.getCaption());
//                appBar.getActionItems().add(btnSearch);
//            }
//        };
//        return view;
//    }
}
