package org.appxi.javafx.visual;

import javafx.scene.Scene;

import java.util.Locale;

public enum Theme {
    LIGHT,
    DARK;

    private static final String PREFIX = "theme_";

    Theme() {
    }

    public void assignTo(Scene scene) {
        StylesheetTools.replaceStylesheet(scene, PREFIX, this.name());
    }

    public void unAssign(Scene scene) {
        scene.getStylesheets().removeIf(s -> s.endsWith(PREFIX.concat(this.name().toLowerCase(Locale.ROOT).concat(".css"))));
    }

    public static Theme getDefault() {
        return LIGHT;
    }
}
