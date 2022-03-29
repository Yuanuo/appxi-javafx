package org.appxi.javafx.visual;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.appxi.javafx.event.EventBus;
import org.appxi.javafx.settings.DefaultOption;
import org.appxi.javafx.settings.DefaultOptions;
import org.appxi.javafx.settings.Option;
import org.appxi.javafx.settings.SettingsList;
import org.appxi.prefs.UserPrefs;
import org.appxi.util.FileHelper;
import org.appxi.util.ext.RawVal;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
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
        //
        SettingsList.add(this::optionForFontSmooth);
        SettingsList.add(this::optionForFontName);
        SettingsList.add(this::optionForFontSize);
        SettingsList.add(this::optionForTheme);
        SettingsList.add(this::optionForSwatch);
        SettingsList.add(this::optionForWebFontName);
        SettingsList.add(this::optionForWebFontSize);
        SettingsList.add(this::optionForWebPageColor);
        SettingsList.add(this::optionForWebTextColor);
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

    public String webPageColor() {
        return switch (theme()) {
            case LIGHT -> UserPrefs.prefs.getString("web.page.color.light", "#f1e5c9");
            case DARK -> UserPrefs.prefs.getString("web.page.color.dark", "#3b3b3b");
        };
    }

    public String webTextColor() {
        return switch (theme()) {
            case LIGHT -> UserPrefs.prefs.getString("web.text.color.light", "#0e1a36");
            case DARK -> UserPrefs.prefs.getString("web.text.color.dark", "#bbb");
        };
    }

    public void initialize() {
        this.applyFont();
        this.applyTheme(null);
        this.applySwatch(null);
        this.applyVisual(null);
    }

    @Override
    public String toString() {
        return "visual-%s theme-%s swatch-%s".formatted(visual().name(), theme().name(), swatch().name()).toLowerCase(Locale.ROOT);
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
        String fontSmooth = UserPrefs.prefs.getString("ui.font.smooth", "gray");
        if (!"gray".equals(fontSmooth) && !"lcd".equals(fontSmooth)) {
            fontSmooth = "gray";
        }
        //
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

        fontSmooth = "-fx-font-smoothing-type: ".concat(fontSmooth).concat(";");
        fontName = " -fx-font-family: \"".concat(fontName).concat("\";");
        fontSize = " -fx-font-size: ".concat(fontSize).concat("px;");

        Path file = UserPrefs.cacheDir().resolve("ui.temp.css");
        FileHelper.writeString(".root, .root * { ".concat(fontSmooth).concat(fontName).concat(" }\n")
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

    private Option<String> optionForFontSmooth() {
        final StringProperty valueProperty = new SimpleStringProperty(UserPrefs.prefs.getString("ui.font.smooth", "gray"));
        valueProperty.addListener((o, ov, nv) -> {
            if (null == ov || Objects.equals(ov, nv)) return;
            UserPrefs.prefs.setProperty("ui.font.smooth", nv);
            applyFont();
        });
        return new DefaultOptions<String>("主界面字体平滑", null, "UI", true)
                .setValues("gray", "lcd")
                .setValueProperty(valueProperty);
    }

    private Option<RawVal<String>> optionForFontName() {
        final List<RawVal<String>> fontFamilies = getFontFamilies();

        final ObjectProperty<RawVal<String>> valueProperty = new SimpleObjectProperty<>();
        final String usedVal = UserPrefs.prefs.getString("ui.font.name", "");
        fontFamilies.stream().filter(v -> usedVal.equalsIgnoreCase(v.value())).findFirst().ifPresent(valueProperty::set);
        if (null == valueProperty.get()) valueProperty.set(new RawVal<>(usedVal, usedVal));

        valueProperty.addListener((o, ov, nv) -> {
            if (null == ov || Objects.equals(ov, nv)) return;
            UserPrefs.prefs.setProperty("ui.font.name", nv.value());
            applyFont();
        });
        return new DefaultOptions<RawVal<String>>("主界面字体", null, "UI", true)
                .setValues(fontFamilies)
                .setValueProperty(valueProperty);
    }

    private Option<Number> optionForFontSize() {
        final IntegerProperty valueProperty = new SimpleIntegerProperty(UserPrefs.prefs.getInt("ui.font.size", 14));
        valueProperty.addListener((o, ov, nv) -> {
            if (null == ov || Objects.equals(ov, nv)) return;
            UserPrefs.prefs.setProperty("ui.font.size", nv.intValue());
            applyFont();
        });
        return new DefaultOptions<Number>("主界面字号", null, "UI", true)
                .setValues(IntStream.iterate(12, v -> v <= 30, v -> v + 2).boxed().collect(Collectors.toList()))
                .setValueProperty(valueProperty);
    }

    private Option<Theme> optionForTheme() {
        final ObjectProperty<Theme> valueProperty = new SimpleObjectProperty<>(theme());
        valueProperty.addListener((o, ov, nv) -> {
            if (null == ov || Objects.equals(ov, nv)) return;
            applyTheme(nv);
            eventBus.fireEvent(new VisualEvent(VisualEvent.SET_THEME, theme));
        });
        return new DefaultOption<Theme>("明暗模式", null, "UI", true)
                .setValueProperty(valueProperty);
    }

    private Option<Swatch> optionForSwatch() {
        final ObjectProperty<Swatch> valueProperty = new SimpleObjectProperty<>(swatch());
        valueProperty.addListener((o, ov, nv) -> {
            if (null == ov || Objects.equals(ov, nv)) return;
            applySwatch(nv);
            eventBus.fireEvent(new VisualEvent(VisualEvent.SET_SWATCH, swatch));
        });
        return new DefaultOption<Swatch>("颜色", null, "UI", true)
                .setValueProperty(valueProperty);
    }

    private Option<RawVal<String>> optionForWebFontName() {
        final List<RawVal<String>> fontFamilies = getFontFamilies();

        final ObjectProperty<RawVal<String>> valueProperty = new SimpleObjectProperty<>();
        final String usedVal = webFontName();
        fontFamilies.stream().filter(v -> usedVal.equalsIgnoreCase(v.value())).findFirst().ifPresent(valueProperty::set);
        if (null == valueProperty.get()) valueProperty.set(new RawVal<>(usedVal, usedVal));

        valueProperty.addListener((o, ov, nv) -> {
            if (null == ov || Objects.equals(ov, nv)) return;
            UserPrefs.prefs.setProperty("web.font.name", nv.value());
            eventBus.fireEvent(new VisualEvent(VisualEvent.SET_WEB_FONT_NAME, nv.value()));
        });
        return new DefaultOptions<RawVal<String>>("阅读器字体", null, "VIEWER", true)
                .setValues(fontFamilies)
                .setValueProperty(valueProperty);
    }

    private Option<Number> optionForWebFontSize() {
        final DoubleProperty valueProperty = new SimpleDoubleProperty(webFontSize());
        valueProperty.addListener((o, ov, nv) -> {
            if (null == ov || Objects.equals(ov, nv)) return;
            UserPrefs.prefs.setProperty("web.font.size", nv.doubleValue());
            eventBus.fireEvent(new VisualEvent(VisualEvent.SET_WEB_FONT_SIZE, nv.doubleValue()));
        });
        return new DefaultOptions<Number>("阅读器字号", null, "VIEWER", true)
                .setValues(DoubleStream.iterate(1.3, v -> v <= 3.0,
                                v -> new BigDecimal(v + .1).setScale(1, RoundingMode.HALF_UP).doubleValue())
                        .boxed().collect(Collectors.toList()))
                .setValueProperty(valueProperty);
    }

    private Option<Color> optionForWebPageColor() {
        final ObjectProperty<Color> valueProperty = new SimpleObjectProperty<>(Color.web(webPageColor()));
        valueProperty.addListener((o, ov, nv) -> {
            if (null == ov || Objects.equals(ov, nv)) return;
            UserPrefs.prefs.setProperty("web.page.color.".concat(theme().name().toLowerCase(Locale.ROOT)), "#".concat(nv.toString().substring(2)));
            eventBus.fireEvent(new VisualEvent(VisualEvent.SET_WEB_PAGE_COLOR, nv));
        });
        return new DefaultOption<Color>("阅读器背景颜色", null, "VIEWER", true)
                .setValueProperty(valueProperty);
    }

    private Option<Color> optionForWebTextColor() {
        final ObjectProperty<Color> valueProperty = new SimpleObjectProperty<>(Color.web(webTextColor()));
        valueProperty.addListener((o, ov, nv) -> {
            if (null == ov || Objects.equals(ov, nv)) return;
            UserPrefs.prefs.setProperty("web.text.color.".concat(theme().name().toLowerCase(Locale.ROOT)), "#".concat(nv.toString().substring(2)));
            eventBus.fireEvent(new VisualEvent(VisualEvent.SET_WEB_TEXT_COLOR, nv));
        });
        return new DefaultOption<Color>("阅读器文字颜色", null, "VIEWER", true)
                .setValueProperty(valueProperty);
    }

    private static List<RawVal<String>> getFontFamilies() {
        final List<RawVal<String>> result = new ArrayList<>(64);

        try {
            Font.getFamilies().forEach(family ->
                    Optional.ofNullable(com.sun.javafx.font.PrismFontFactory.getFontFactory().getFontResource(family, null, false))
                            .map(com.sun.javafx.font.FontResource::getLocaleFamilyName)
                            .ifPresent(name -> result.add(new RawVal<>(family, family.equals(name) ? family : family + " (" + name + ")"))));
        } catch (Throwable e) {
            Font.getFamilies().forEach(family -> result.add(new RawVal<>(family, family)));
        }

        return result;
    }
}
