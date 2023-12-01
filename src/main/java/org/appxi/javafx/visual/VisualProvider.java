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
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.appxi.event.EventBus;
import org.appxi.javafx.app.BaseApp;
import org.appxi.javafx.settings.DefaultOption;
import org.appxi.javafx.settings.DefaultOptions;
import org.appxi.javafx.settings.Option;
import org.appxi.javafx.settings.OptionEditorBase;
import org.appxi.util.FileHelper;
import org.appxi.util.ext.RawVal;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

public final class VisualProvider {
    private static final double WEB_FONT_MIN = 1.3;
    private static final double WEB_FONT_MAX = 4.0;
    private static final int APP_FONT_MIN = 12;
    private static final int APP_FONT_MAX = 40;

    public final EventBus eventBus;
    private final BaseApp app;

    private Visual visual;
    private Theme theme;
    private Swatch swatch;

    public VisualProvider(BaseApp app) {
        this.app = app;
        this.eventBus = app.eventBus;
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
        double zoomLevel = app.config.getDouble("web.font.size", -1);
        if (zoomLevel == -1) zoomLevel = app.config.getDouble("display.zoom", -1);
        if (zoomLevel < WEB_FONT_MIN || zoomLevel > WEB_FONT_MAX)
            zoomLevel = 1.6;
        return zoomLevel;
    }

    public String webFontName() {
        return app.config.getString("web.font.name", "");
    }

    public String webPageColor() {
        return switch (theme()) {
            case LIGHT -> app.config.getString("web.page.color.light", "#f1e5c9");
            case DARK -> app.config.getString("web.page.color.dark", "#3b3b3b");
        };
    }

    public String webTextColor() {
        return switch (theme()) {
            case LIGHT -> app.config.getString("web.text.color.light", "#0e1a36");
            case DARK -> app.config.getString("web.text.color.dark", "#bbb");
        };
    }

    final WeakHashMap<Stage, Boolean> stages = new WeakHashMap<>();

    public void apply(Stage stage) {
        stages.put(stage, true);

        this.applyFont(stage);
        this.applyTheme(stage, null);
        this.applySwatch(stage, null);
        this.applyVisual(stage, null);

        if (null != eventBus) {
            eventBus.addEventHandler(VisualEvent.SET_STYLE, event -> _cachedWebStyleSheetLocationURI = null);
            eventBus.addEventHandler(VisualEvent.SET_WEB_STYLE, event -> _cachedWebStyleSheetLocationURI = null);
        }
    }

    @Override
    public String toString() {
        return "visual-%s theme-%s swatch-%s".formatted(visual().name(), theme().name(), swatch().name()).toLowerCase(Locale.ROOT);
    }

    private void applyVisual(Stage stage, Visual visual) {
        if (null != this.visual && visual == this.visual) return;
        if (null == visual)
            try {
                visual = Visual.valueOf(app.config.getString("ui.visual", "DESKTOP"));
            } catch (Throwable t) {
                visual = Visual.getDefault();
            }
        app.config.setProperty("ui.visual", visual.name());
        this.visual = visual;

        final List<Scene> sceneList = new ArrayList<>();
        if (null != stage) {
            sceneList.add(stage.getScene());
        } else {
            stages.keySet().forEach(v -> sceneList.add(v.getScene()));
        }
        for (Scene scene : sceneList) {
            visual.assignTo(scene);
            final ObservableList<String> styleClass = scene.getRoot().getStyleClass();
            styleClass.removeIf(s -> s.startsWith("visual-"));
            styleClass.add("visual-".concat(visual.name().toLowerCase(Locale.ROOT)));
        }
    }

    private void applyTheme(Stage stage, Theme theme) {
        if (null != this.theme && theme == this.theme) return;
        if (null == theme)
            try {
                theme = Theme.valueOf(app.config.getString("ui.theme", "LIGHT"));
            } catch (Throwable t) {
                theme = Theme.getDefault();
            }
        app.config.setProperty("ui.theme", theme.name());
        this.theme = theme;

        final List<Scene> sceneList = new ArrayList<>();
        if (null != stage) {
            sceneList.add(stage.getScene());
        } else {
            stages.keySet().forEach(v -> sceneList.add(v.getScene()));
        }
        for (Scene scene : sceneList) {
            theme.assignTo(scene);
            final ObservableList<String> styleClass = scene.getRoot().getStyleClass();
            styleClass.removeIf(s -> s.startsWith("theme-"));
            styleClass.add("theme-".concat(theme.name().toLowerCase(Locale.ROOT)));
        }
    }

    private void applySwatch(Stage stage, Swatch swatch) {
        if (null != this.swatch && swatch == this.swatch) return;
        if (null == swatch)
            try {
                swatch = Swatch.valueOf(app.config.getString("ui.swatch", "BLUE"));
            } catch (Throwable t) {
                swatch = Swatch.getDefault();
            }
        app.config.setProperty("ui.swatch", swatch.name());
        this.swatch = swatch;

        final List<Scene> sceneList = new ArrayList<>();
        if (null != stage) {
            sceneList.add(stage.getScene());
        } else {
            stages.keySet().forEach(v -> sceneList.add(v.getScene()));
        }
        for (Scene scene : sceneList) {
            swatch.assignTo(scene);
            final ObservableList<String> styleClass = scene.getRoot().getStyleClass();
            styleClass.removeIf(s -> s.startsWith("swatch-"));
            styleClass.add("swatch-".concat(swatch.name().toLowerCase(Locale.ROOT)));
        }
    }

    private void applyFont(Stage stage) {
        String fontSmooth = app.config.getString("ui.font.smooth", "gray");
        if (!"gray".equals(fontSmooth) && !"lcd".equals(fontSmooth)) {
            fontSmooth = "gray";
        }
        //
        String fontName = app.config.getString("ui.font.name", null);
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
            app.config.setProperty("ui.font.name", fontName);
        }
        //
        int fontSize = app.config.getInt("ui.font.size", 14);
        if (fontSize < APP_FONT_MIN || fontSize > APP_FONT_MAX) {
            fontSize = 14;
            app.config.setProperty("ui.font.size", fontSize);
        }

        fontSmooth = "-fx-font-smoothing-type: ".concat(fontSmooth).concat(";");
        fontName = " -fx-font-family: \"".concat(fontName).concat("\";");

        Path file = app.workspace.resolve("ui.app.css");
        FileHelper.writeString(".root, .root * { ".concat(fontSmooth).concat(fontName).concat(" }\n")
                .concat(".root { ").concat(" -fx-font-size: " + fontSize + "px;").concat(" }\n")
                .concat(".icon-toggle .text, .icon-text .text { -fx-font-family: \"Material Icons\"; }\n"), file);
        final String css = file.toUri().toString().replace("///", "/");

        final List<Scene> sceneList = new ArrayList<>();
        if (null != stage) {
            sceneList.add(stage.getScene());
        } else {
            stages.keySet().forEach(v -> sceneList.add(v.getScene()));
        }
        for (Scene scene : sceneList) {
            int idx = scene.getStylesheets().indexOf(css);
            scene.getStylesheets().remove(css);
            if (idx != -1) {
                scene.getStylesheets().add(idx, css);
            } else {
                scene.getStylesheets().add(css);
            }

            // 将主界面字号加入CSS根样式中以控制组件跟随字号而缩放
            final ObservableList<String> styleClass = scene.getRoot().getStyleClass();
            styleClass.removeIf(s -> s.startsWith("font-size-"));
            styleClass.add("font-size-" + fontSize);
        }
    }

    public Option<String> optionForFontSmooth() {
        final StringProperty valueProperty = new SimpleStringProperty(app.config.getString("ui.font.smooth", "gray"));
        valueProperty.addListener((o, ov, nv) -> {
            if (null == ov || Objects.equals(ov, nv)) return;
            app.config.setProperty("ui.font.smooth", nv);
            applyFont(null);
        });
        return new DefaultOptions<String>("主界面字体平滑", null, "UI", true)
                .setValues("gray", "lcd")
                .setValueProperty(valueProperty);
    }

    public Option<RawVal<String>> optionForFontName() {
        final String usedVal = app.config.getString("ui.font.name", "");
        return new DefaultOption<RawVal<String>>("主界面字体", null, "UI", true,
                option -> new OptionEditorBase<>(option, new Button()) {
                    private ObjectProperty<RawVal<String>> valueProperty;

                    @Override
                    public Property<RawVal<String>> valueProperty() {
                        if (this.valueProperty == null) {
                            this.valueProperty = new SimpleObjectProperty<>();
                            getEditor().setOnAction(evt -> chooseFontFamilies(valueProperty));
                            valueProperty.addListener((o, ov, nv) -> {
                                if (null == ov || Objects.equals(ov, nv)) return;
                                setValue(nv);
                                app.config.setProperty("ui.font.name", nv.value());
                                applyFont(null);
                            });
                        }
                        return this.valueProperty;
                    }

                    @Override
                    public void setValue(RawVal<String> value) {
                        if (null != value) {
                            getEditor().setText(value.title());
                            getEditor().setTooltip(new Tooltip(value.title()));
                        }
                    }
                })
                .setValue(new RawVal<>(usedVal, usedVal));
    }

    public Option<Number> optionForFontSize() {
        final IntegerProperty valueProperty = new SimpleIntegerProperty(app.config.getInt("ui.font.size", 14));
        valueProperty.addListener((o, ov, nv) -> {
            if (null == ov || Objects.equals(ov, nv)) return;
            app.config.setProperty("ui.font.size", nv.intValue());
            applyFont(null);
        });
        return new DefaultOptions<Number>("主界面字号", null, "UI", true)
                .setValues(IntStream.iterate(APP_FONT_MIN, v -> v <= APP_FONT_MAX, v -> v + 2).boxed().collect(Collectors.toList()))
                .setValueProperty(valueProperty);
    }

    public Option<Theme> optionForTheme() {
        final ObjectProperty<Theme> valueProperty = new SimpleObjectProperty<>(theme());
        valueProperty.addListener((o, ov, nv) -> {
            if (null == ov || Objects.equals(ov, nv)) return;
            applyTheme(null, nv);
            eventBus.fireEvent(new VisualEvent(VisualEvent.SET_THEME, theme));
        });
        return new DefaultOption<Theme>("明暗模式", null, "UI", true)
                .setValueProperty(valueProperty);
    }

    public Option<Swatch> optionForSwatch() {
        final ObjectProperty<Swatch> valueProperty = new SimpleObjectProperty<>(swatch());
        valueProperty.addListener((o, ov, nv) -> {
            if (null == ov || Objects.equals(ov, nv)) return;
            applySwatch(null, nv);
            eventBus.fireEvent(new VisualEvent(VisualEvent.SET_SWATCH, swatch));
        });
        return new DefaultOption<Swatch>("颜色", null, "UI", true)
                .setValueProperty(valueProperty);
    }

    public Option<RawVal<String>> optionForWebFontName() {
        final String usedVal = webFontName();
        return new DefaultOption<RawVal<String>>("阅读器字体", null, "VIEWER", true,
                option -> new OptionEditorBase<>(option, new Button()) {
                    private ObjectProperty<RawVal<String>> valueProperty;

                    @Override
                    public Property<RawVal<String>> valueProperty() {
                        if (this.valueProperty == null) {
                            this.valueProperty = new SimpleObjectProperty<>();
                            getEditor().setOnAction(evt -> chooseFontFamilies(valueProperty));
                            valueProperty.addListener((o, ov, nv) -> {
                                if (null == ov || Objects.equals(ov, nv)) return;
                                setValue(nv);
                                app.config.setProperty("web.font.name", nv.value());
                                eventBus.fireEvent(new VisualEvent(VisualEvent.SET_WEB_FONT_NAME, nv.value()));
                            });
                        }
                        return this.valueProperty;
                    }

                    @Override
                    public void setValue(RawVal<String> value) {
                        if (null != value) {
                            getEditor().setText(value.title());
                            getEditor().setTooltip(new Tooltip(value.title()));
                        }
                    }
                })
                .setValue(new RawVal<>(usedVal, usedVal));
    }

    public Option<Number> optionForWebFontSize() {
        final DoubleProperty valueProperty = new SimpleDoubleProperty(webFontSize());
        valueProperty.addListener((o, ov, nv) -> {
            if (null == ov || Objects.equals(ov, nv)) return;
            app.config.setProperty("web.font.size", nv.doubleValue());
            eventBus.fireEvent(new VisualEvent(VisualEvent.SET_WEB_FONT_SIZE, nv.doubleValue()));
        });
        return new DefaultOptions<Number>("阅读器字号", null, "VIEWER", true)
                .setValues(DoubleStream.iterate(WEB_FONT_MIN, v -> v <= WEB_FONT_MAX,
                                v -> new BigDecimal(v + .1).setScale(1, RoundingMode.HALF_UP).doubleValue())
                        .boxed().collect(Collectors.toList()))
                .setValueProperty(valueProperty);
    }

    public Option<Color> optionForWebPageColor() {
        final ObjectProperty<Color> valueProperty = new SimpleObjectProperty<>(Color.web(webPageColor()));
        valueProperty.addListener((o, ov, nv) -> {
            if (null == ov || Objects.equals(ov, nv)) return;
            app.config.setProperty("web.page.color.".concat(theme().name().toLowerCase(Locale.ROOT)), "#".concat(nv.toString().substring(2)));
            eventBus.fireEvent(new VisualEvent(VisualEvent.SET_WEB_PAGE_COLOR, nv));
        });
        return new DefaultOption<Color>("阅读器背景颜色", null, "VIEWER", true)
                .setValueProperty(valueProperty);
    }

    public Option<Color> optionForWebTextColor() {
        final ObjectProperty<Color> valueProperty = new SimpleObjectProperty<>(Color.web(webTextColor()));
        valueProperty.addListener((o, ov, nv) -> {
            if (null == ov || Objects.equals(ov, nv)) return;
            app.config.setProperty("web.text.color.".concat(theme().name().toLowerCase(Locale.ROOT)), "#".concat(nv.toString().substring(2)));
            eventBus.fireEvent(new VisualEvent(VisualEvent.SET_WEB_TEXT_COLOR, nv));
        });
        return new DefaultOption<Color>("阅读器文字颜色", null, "VIEWER", true)
                .setValueProperty(valueProperty);
    }

    private void chooseFontFamilies(ObjectProperty<RawVal<String>> property) {
        final RawVal<String> usedVal = property.get();
//        CardChooser.of("选择字体")
//                .owner(primarySceneSupplier.get().getWindow())
//                .cards(FontFaceHelper.getFontFamilies().stream()
//                        .map(v -> CardChooser.ofCard(v.title())
//                                .focused(v.value().equalsIgnoreCase(usedVal.value()))
//                                .userData(v)
//                                .get())
//                        .toList())
//                .showAndWait()
//                .ifPresent(card -> property.set(card.userData()));
    }

    private String _cachedWebStyleSheetLocationURI;

    /**
     * 针对Web显示工具的用户定义样式，此函数构建经过Base64编码的内嵌URI，
     * 可直接用于css标签的href属性
     *
     * @return 以data:text/css;开始的内嵌URI
     */
    public String getWebStyleSheetURI() {
        if (null != _cachedWebStyleSheetLocationURI) {
            return _cachedWebStyleSheetLocationURI;
        }
        String cssData = Base64.getMimeEncoder().encodeToString(getWebStyleSheetCSS().getBytes(StandardCharsets.UTF_8));
        cssData = cssData.replaceAll("[\r\n]", "");
        return _cachedWebStyleSheetLocationURI = "data:text/css;charset=utf-8;base64," + cssData;
    }

    public String getWebStyleSheetCSS() {
        return """
                :root {
                    --font-family: tibetan, "%s", AUTO !important;
                    --zoom: %.2f !important;
                    --font-size: calc(var(--zoom) * 1rem);
                    --text-color: %s;
                }
                                
                body {
                    background-color: %s;
                    font-family: var(--font-family) !important;
                    font-size: var(--font-size);
                    color: var(--text-color);
                    line-height: calc(var(--font-size) * 1.85);
                }
                """.formatted(
                webFontName(),
                webFontSize(),
                webTextColor(),
                webPageColor()
        );
    }
}
