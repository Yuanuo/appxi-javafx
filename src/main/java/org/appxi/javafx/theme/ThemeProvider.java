package org.appxi.javafx.theme;

import javafx.scene.Scene;
import org.appxi.javafx.event.EventBus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
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
        if (!this.scenes.contains(scene))
            this.scenes.add(scene);
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
        if (null == theme)
            return;

        if (Objects.equals(this.currentTheme, theme))
            return;

        final Theme oldTheme = this.currentTheme;
        // remove old
        uninstallTheme(oldTheme);

        // apply new
        this.currentTheme = theme;
        installTheme(theme);

        // fire events
        if (null != this.eventBus)
            this.eventBus.fireEvent(new ThemeEvent(oldTheme, theme));
    }

    protected void installTheme(Theme theme) {
        if (null == theme)
            return;
        final List<String> styles = theme.stylesheets;
        if (null != styles && !styles.isEmpty())
            this.scenes.forEach(scene -> scene.getStylesheets().addAll(styles));
    }

    protected void uninstallTheme(Theme theme) {
        if (null == theme)
            return;
        final List<String> styles = theme.stylesheets;
        if (null != styles && !styles.isEmpty())
            this.scenes.forEach(scene -> scene.getStylesheets().removeAll(styles));
    }
}
