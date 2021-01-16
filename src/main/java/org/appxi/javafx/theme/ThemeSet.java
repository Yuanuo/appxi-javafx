package org.appxi.javafx.theme;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ThemeSet extends Theme {
    public final List<Theme> themes = new ArrayList<>();

    public ThemeSet(String name, String title, String accentColor, ThemeStyle style) {
        super(name, title, accentColor, style);
    }

    @Override
    public ThemeSet addStylesheet(String... stylesheets) {
        super.addStylesheet(stylesheets);
        return this;
    }

    @Override
    public ThemeSet addStylesheet(URL... stylesheets) {
        super.addStylesheet(stylesheets);
        return this;
    }

    public final ThemeSet addTheme(Theme... themes) {
        for (Theme theme : themes)
            if (!this.themes.contains(theme))
                this.themes.add(theme);
        return this;
    }

    public Theme getTheme(String name) {
        for (Theme theme : this.themes) {
            if (Objects.equals(theme.name, name))
                return theme;
        }

        if (!this.themes.isEmpty())
            return this.themes.get(0);
        return this;
    }

    public static ThemeSet light(String name, String title, String accentColor) {
        return new ThemeSet(name, title, accentColor, ThemeStyle.LIGHT);
    }

    public static ThemeSet dark(String name, String title, String accentColor) {
        return new ThemeSet(name, title, accentColor, ThemeStyle.DARK);
    }
}
