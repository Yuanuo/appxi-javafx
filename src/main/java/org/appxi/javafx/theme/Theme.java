package org.appxi.javafx.theme;

import java.net.URL;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

public class Theme {
    public enum ThemeStyle {
        LIGHT, DARK
    }

    public final String name, title;
    public final String accentColor;
    public final ThemeStyle style;
    public final Collection<String> stylesheets = new LinkedHashSet<>();

    public Theme(String name, String title, String accentColor, ThemeStyle style) {
        this.name = name;
        this.title = title;
        this.accentColor = accentColor;
        this.style = style;
    }

    public Theme addStylesheet(String... stylesheets) {
        this.stylesheets.addAll(List.of(stylesheets));
        return this;
    }

    public Theme addStylesheet(URL... stylesheets) {
        for (URL stylesheet : stylesheets)
            this.stylesheets.add(stylesheet.toExternalForm());
        return this;
    }

    public static Theme light(String name, String title, String accentColor) {
        return new Theme(name, title, accentColor, ThemeStyle.LIGHT);
    }

    public static Theme dark(String name, String title, String accentColor) {
        return new Theme(name, title, accentColor, ThemeStyle.DARK);
    }
}
