package org.appxi.javafx.visual;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.text.Font;
import org.appxi.javafx.event.EventBus;
import org.appxi.javafx.settings.DefaultOption;
import org.appxi.javafx.settings.Option;
import org.appxi.javafx.settings.OptionEditorBase;
import org.appxi.prefs.UserPrefs;
import org.appxi.util.FileHelper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

public final class VisualProvider {
    public final EventBus eventBus;
    private final Supplier<Scene> primarySceneSupplier;

    private Visual visual;
    private Theme theme;
    private Swatch swatch;

    public VisualProvider(EventBus eventBus, Supplier<Scene> primarySceneSupplier) {
        this.eventBus = eventBus;
        this.primarySceneSupplier = primarySceneSupplier;
    }

    public Visual visual() {
        return visual != null ? visual : Visual.getDefault();
    }

    public Theme theme() {
        return theme != null ? theme : Theme.getDefault();
    }

    public Swatch swatch() {
        return swatch != null ? swatch : Swatch.getDefault();
    }

    public double webFontSize() {
        double zoomLevel = UserPrefs.prefs.getDouble("web.font.size", -1);
        if (zoomLevel == -1) zoomLevel = UserPrefs.prefs.getDouble("display.zoom", -1);
        if (zoomLevel < 1.3 || zoomLevel > 3.0)
            zoomLevel = 1.6;
        return zoomLevel;
    }

    public String webFontName() {
        return UserPrefs.prefs.getString("web.font.name", "");
    }

    public void initialize() {
        this.applyFont();
        this.applyTheme(null);
        this.applySwatch(null);
        this.applyVisual(null);
    }

    @Override
    public String toString() {
        return "visual-%s theme-%s swatch-%s".formatted(visual().name(), theme().name(), swatch().name()).toLowerCase();
    }

    private void applyVisual(Visual visual) {
        if (null != this.visual && visual == this.visual) return;
        if (null == visual)
            try {
                visual = Visual.valueOf(UserPrefs.prefs.getString("ui.visual", "DESKTOP"));
            } catch (Throwable t) {
                visual = Visual.getDefault();
            }
        UserPrefs.prefs.setProperty("ui.visual", visual.name());
        this.visual = visual;
        final Scene scene = primarySceneSupplier.get();
        visual.assignTo(scene);
        final ObservableList<String> styleClass = scene.getRoot().getStyleClass();
        styleClass.removeIf(s -> s.startsWith("visual-"));
        styleClass.add("visual-".concat(visual.name().toLowerCase(Locale.ROOT)));
    }

    private void applyTheme(Theme theme) {
        if (null != this.theme && theme == this.theme) return;
        if (null == theme)
            try {
                theme = Theme.valueOf(UserPrefs.prefs.getString("ui.theme", "LIGHT"));
            } catch (Throwable t) {
                theme = Theme.getDefault();
            }
        UserPrefs.prefs.setProperty("ui.theme", theme.name());
        this.theme = theme;
        final Scene scene = primarySceneSupplier.get();
        theme.assignTo(scene);
        final ObservableList<String> styleClass = scene.getRoot().getStyleClass();
        styleClass.removeIf(s -> s.startsWith("theme-"));
        styleClass.add("theme-".concat(theme.name().toLowerCase(Locale.ROOT)));
    }

    private void applySwatch(Swatch swatch) {
        if (null != this.swatch && swatch == this.swatch) return;
        if (null == swatch)
            try {
                swatch = Swatch.valueOf(UserPrefs.prefs.getString("ui.swatch", "BLUE"));
            } catch (Throwable t) {
                swatch = Swatch.getDefault();
            }
        UserPrefs.prefs.setProperty("ui.swatch", swatch.name());
        this.swatch = swatch;
        final Scene scene = primarySceneSupplier.get();
        swatch.assignTo(scene);
        final ObservableList<String> styleClass = scene.getRoot().getStyleClass();
        styleClass.removeIf(s -> s.startsWith("swatch-"));
        styleClass.add("swatch-".concat(swatch.name().toLowerCase(Locale.ROOT)));
    }

    private void applyFont() {
        String fontName = UserPrefs.prefs.getString("ui.font.name", null);
        if (fontName == null || fontName.isBlank()) {
            final String osName = System.getProperty("os.name").toLowerCase(Locale.ROOT);
            if (osName.contains("windows")) {
                fontName = "Microsoft YaHei";
            } else if (osName.contains("mac") || osName.contains("osx")) {
                fontName = "System";
            } else if (osName.contains("linux") || osName.contains("ubuntu")) {
                fontName = "System";
            } else {
                fontName = "System";
            }
            UserPrefs.prefs.setProperty("ui.font.name", fontName);
        }
        //
        String fontSize = UserPrefs.prefs.getString("ui.font.size", null);
        if (null == fontSize || fontSize.isBlank()) {
            fontSize = "14";
            UserPrefs.prefs.setProperty("ui.font.size", fontSize);
        }

        fontName = " -fx-font-family: \"".concat(fontName).concat("\";");
        fontSize = " -fx-font-size: ".concat(fontSize).concat("px;");

        Path file = UserPrefs.cacheDir().resolve("ui.temp.css");
        FileHelper.writeString(".root, .root * { ".concat(fontName).concat(" }\n")
                .concat(".root { ").concat(fontSize).concat(" }\n")
                .concat(".icon-toggle .text, .icon-text .text { -fx-font-family: \"Material Icons\"; }\n"), file);
        final String css = file.toUri().toString().replace("///", "/");
        Scene primaryScene = primarySceneSupplier.get();
        int idx = primaryScene.getStylesheets().indexOf(css);
        primaryScene.getStylesheets().remove(css);
        if (idx != -1)
            primaryScene.getStylesheets().add(idx, css);
        else primaryScene.getStylesheets().add(css);
    }

    public Option<String> optionForFontName() {
        return new DefaultOption<>("主界面字体", "", "UI",
                UserPrefs.prefs.getString("ui.font.name", ""), true,
                option -> new OptionEditorBase<String, ChoiceBox<String>>(option, new ChoiceBox<>()) {
                    private StringProperty valueProperty;

                    @Override
                    public Property<String> valueProperty() {
                        if (this.valueProperty != null) return this.valueProperty;
                        this.valueProperty = new SimpleStringProperty();
                        getEditor().getItems().setAll(Font.getFontNames());
                        this.getEditor().getSelectionModel().selectedItemProperty().addListener((obs, ov, nv) -> {
                            if (ov == null || Objects.equals(ov, nv)) return;
                            this.valueProperty.set(nv);
                            //
                            UserPrefs.prefs.setProperty("ui.font.name", nv);
                            applyFont();
                            eventBus.fireEvent(new VisualEvent(VisualEvent.SET_FONT_NAME, nv));
                        });
                        this.valueProperty.addListener((obs, ov, nv) -> this.setValue(nv));
                        return this.valueProperty;
                    }

                    @Override
                    public void setValue(String value) {
                        if (getEditor().getItems().isEmpty()) return;
                        if (!getEditor().getItems().contains(value))
                            value = "";
                        getEditor().getSelectionModel().select(value);
                    }
                });
    }

    public Option<Number> optionForFontSize() {
        return new DefaultOption<>("主界面字号", "", "UI",
                UserPrefs.prefs.getInt("ui.font.size", 14), true,
                option -> new OptionEditorBase<Number, ChoiceBox<Number>>(option, new ChoiceBox<>()) {
                    private IntegerProperty valueProperty;

                    @Override
                    public Property<Number> valueProperty() {
                        if (this.valueProperty == null) {
                            this.valueProperty = new SimpleIntegerProperty();
                            this.getEditor().getItems().setAll(
                                    IntStream.iterate(12, v -> v <= 30, v -> v + 2).boxed().collect(Collectors.toList())
                            );
                            this.getEditor().getSelectionModel().selectedItemProperty().addListener((obs, ov, nv) -> {
                                if (ov == null || Objects.equals(ov, nv)) return;
                                this.valueProperty.set(nv.intValue());
                                //
                                UserPrefs.prefs.setProperty("ui.font.size", nv.intValue());
                                applyFont();
                                eventBus.fireEvent(new VisualEvent(VisualEvent.SET_FONT_SIZE, nv.intValue()));
                            });
                            this.valueProperty.addListener((obs, ov, nv) -> this.setValue(nv));
                        }
                        return this.valueProperty;
                    }

                    @Override
                    public void setValue(Number value) {
                        if (getEditor().getItems().isEmpty()) return;
                        if (!getEditor().getItems().contains(value.intValue()))
                            value = 14;
                        getEditor().getSelectionModel().select(value);
                    }
                });
    }

    public Option<Theme> optionForTheme() {
        return new DefaultOption<>("颜色模式", "", "UI", theme(), true,
                option -> new OptionEditorBase<Theme, ChoiceBox<Theme>>(option, new ChoiceBox<>()) {
                    private ObjectProperty<Theme> valueProperty;

                    @Override
                    public Property<Theme> valueProperty() {
                        if (this.valueProperty == null) {
                            this.valueProperty = new SimpleObjectProperty<>();
                            this.getEditor().getItems().setAll(Theme.values());
                            this.getEditor().getSelectionModel().selectedItemProperty().addListener((obs, ov, nv) -> {
                                if (ov == null || Objects.equals(ov, nv)) return;
                                this.valueProperty.set(nv);
                                //
                                applyTheme(nv);
                                eventBus.fireEvent(new VisualEvent(VisualEvent.SET_THEME, theme));
                            });
                            this.valueProperty.addListener((obs, ov, nv) -> this.setValue(nv));
                        }
                        return this.valueProperty;
                    }

                    @Override
                    public void setValue(Theme value) {
                        if (getEditor().getItems().isEmpty()) return;
                        getEditor().setValue(value);
                    }
                });
    }

    public Option<Swatch> optionForSwatch() {
        return new DefaultOption<>("颜色", "", "UI", swatch(), true,
                option -> new OptionEditorBase<Swatch, ChoiceBox<Swatch>>(option, new ChoiceBox<>()) {
                    private ObjectProperty<Swatch> valueProperty;

                    @Override
                    public Property<Swatch> valueProperty() {
                        if (this.valueProperty == null) {
                            this.valueProperty = new SimpleObjectProperty<>();
                            this.getEditor().getItems().setAll(Swatch.values());
                            this.getEditor().getSelectionModel().selectedItemProperty().addListener((obs, ov, nv) -> {
                                if (ov == null || Objects.equals(ov, nv)) return;
                                this.valueProperty.set(nv);
                                //
                                applySwatch(nv);
                                eventBus.fireEvent(new VisualEvent(VisualEvent.SET_SWATCH, swatch));
                            });
                            this.valueProperty.addListener((obs, ov, nv) -> this.setValue(nv));
                        }
                        return this.valueProperty;
                    }

                    @Override
                    public void setValue(Swatch value) {
                        if (getEditor().getItems().isEmpty()) return;
                        getEditor().setValue(value);
                    }
                });
    }

    public Option<String> optionForWebFontName() {
        return new DefaultOption<>(
                "阅读器字体", "阅读视图默认字体", "VIEWER", webFontName(), true,
                option -> new OptionEditorBase<String, ChoiceBox<String>>(option, new ChoiceBox<>()) {
                    private StringProperty valueProperty;

                    @Override
                    public Property<String> valueProperty() {
                        if (this.valueProperty != null) return this.valueProperty;
                        this.valueProperty = new SimpleStringProperty();
                        getEditor().getItems().setAll(Font.getFontNames());
                        this.getEditor().getSelectionModel().selectedItemProperty().addListener((obs, ov, nv) -> {
                            if (ov == null || Objects.equals(ov, nv)) return;
                            this.valueProperty.set(nv);
                            //
                            UserPrefs.prefs.setProperty("web.font.name", nv);
                            eventBus.fireEvent(new VisualEvent(VisualEvent.SET_WEB_FONT_NAME, nv));
                        });
                        this.valueProperty.addListener((obs, ov, nv) -> this.setValue(nv));
                        return this.valueProperty;
                    }

                    @Override
                    public void setValue(String value) {
                        if (getEditor().getItems().isEmpty()) return;
                        if (!getEditor().getItems().contains(value))
                            value = "";
                        getEditor().getSelectionModel().select(value);
                    }
                });
    }

    public Option<Number> optionForWebFontSize() {
        return new DefaultOption<>(
                "阅读器字号", "阅读视图默认字号", "VIEWER", webFontSize(), true,
                option -> new OptionEditorBase<Number, ChoiceBox<Number>>(option, new ChoiceBox<>()) {
                    private DoubleProperty valueProperty;

                    @Override
                    public Property<Number> valueProperty() {
                        if (this.valueProperty == null) {
                            this.valueProperty = new SimpleDoubleProperty();
                            this.getEditor().getItems().setAll(
                                    DoubleStream.iterate(1.3, v -> v <= 3.0,
                                                    v -> new BigDecimal(v + .1).setScale(1, RoundingMode.HALF_UP).doubleValue())
                                            .boxed().collect(Collectors.toList())
                            );
                            this.getEditor().getSelectionModel().selectedItemProperty().addListener((obs, ov, nv) -> {
                                if (ov == null || Objects.equals(ov, nv)) return;
                                this.valueProperty.set(nv.doubleValue());
                                //
                                UserPrefs.prefs.setProperty("web.font.size", nv.doubleValue());
                                eventBus.fireEvent(new VisualEvent(VisualEvent.SET_WEB_FONT_SIZE, nv.doubleValue()));
                            });
                            this.valueProperty.addListener((obs, ov, nv) -> this.setValue(nv));
                        }
                        return this.valueProperty;
                    }

                    @Override
                    public void setValue(Number value) {
                        if (getEditor().getItems().isEmpty()) return;
                        if (!getEditor().getItems().contains(value))
                            value = 1.6;
                        getEditor().getSelectionModel().select(value);
                    }
                });
    }

}
