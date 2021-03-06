package org.appxi.javafx.theme;

import javafx.scene.Scene;
import org.appxi.javafx.event.EventBus;

import java.util.*;
import java.util.function.Predicate;

public class ThemeProvider {
    private final List<Theme> themes = new ArrayList<>();
    private final List<Scene> scenes = new ArrayList<>();
    private final EventBus eventBus;

    private Theme currentTheme;

    public ThemeProvider(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public Theme getTheme() {
        return currentTheme;
    }

    public List<Theme> getThemes() {
        return Collections.unmodifiableList(this.themes);
    }

    public List<Theme> getThemes(Theme.ThemeStyle style) {
        return getThemes((v) -> v.style == style);
    }

    public List<Theme> getThemes(Predicate<Theme> predicate) {
        final List<Theme> result = new ArrayList<>();
        this.themes.forEach(t -> {
            if (predicate.test(t))
                result.add(t);
        });
        return result;
    }

    public ThemeProvider addTheme(Theme theme) {
        if (!this.themes.contains(theme))
            this.themes.add(theme);
        return this;
    }

    public Theme removeTheme(String name) {
        final Theme theme = findTheme(name);
        this.themes.remove(theme);
        return theme;
    }

    public Theme findTheme(String name) {
        for (Theme theme : this.themes)
            if (Objects.equals(theme.name, name))
                return theme;
        return null;
    }

    public boolean containsTheme(String name) {
        return null != findTheme(name);
    }

    public boolean containsTheme(Theme theme) {
        return this.themes.contains(theme);
    }

    public boolean hasThemes() {
        return !this.themes.isEmpty();
    }

    public ThemeProvider addScene(Scene scene) {
        if (!this.scenes.contains(scene)) {
            this.scenes.add(scene);
            if (null != this.currentTheme)
                this.applyThemeFor(this.currentTheme, scene);
        }
        return this;
    }

    public ThemeProvider removeScene(Scene scene) {
        this.scenes.remove(scene);
        return this;
    }

    public final void applyTheme(String name) {
        applyTheme(findTheme(name));
    }

    public void applyTheme(Theme theme) {
        if (null == theme && !this.themes.isEmpty())
            theme = this.themes.get(0);
        if (null == theme)
            return;

        if (Objects.equals(this.currentTheme, theme))
            return;

        final Theme oldTheme = this.currentTheme;
        // remove old
        removeThemeFor(oldTheme, this.scenes.toArray(new Scene[0]));

        // apply new
        this.currentTheme = theme;
        applyThemeFor(theme, this.scenes.toArray(new Scene[0]));

        // fire events
        if (null != this.eventBus)
            this.eventBus.fireEvent(new ThemeEvent(oldTheme, theme));
    }

    public void applyThemeFor(Scene... scenes) {
        this.applyThemeFor(getTheme(), scenes);
    }

    public void applyThemeFor(Theme theme, Scene... scenes) {
        if (null == theme || null == scenes || scenes.length == 0)
            return;
        final Collection<String> styles = theme.stylesheets;
        if (!styles.isEmpty()) {
            for (Scene scene : scenes)
                scene.getStylesheets().addAll(styles);
        }
    }

    public void removeThemeFor(Theme theme, Scene... scenes) {
        if (null == theme || null == scenes || scenes.length == 0)
            return;
        final Collection<String> styles = theme.stylesheets;
        if (!styles.isEmpty()) {
            for (Scene scene : scenes)
                scene.getStylesheets().removeAll(styles);
        }
    }
}
