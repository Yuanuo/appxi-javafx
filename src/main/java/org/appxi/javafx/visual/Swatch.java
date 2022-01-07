package org.appxi.javafx.visual;

import javafx.scene.Scene;
import javafx.scene.paint.Color;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public enum Swatch {
    BLUE,
    CYAN,
    DEEP_ORANGE,
    DEEP_PURPLE,
    GREEN,
    INDIGO,
    LIGHT_BLUE,
    PINK,
    PURPLE,
    RED,
    TEAL,
    LIGHT_GREEN,
    LIME,
    YELLOW,
    AMBER,
    ORANGE,
    BROWN,
    GREY,
    BLUE_GREY;

    private static final Random RANDOM = new Random();
    private static final String PREFIX = "swatch_";
    private final String stylesheetName = StylesheetTools.buildResourceName(PREFIX, this.name());
    private Map<String, Color> colors = null;

    Swatch() {
    }

    public void assignTo(Scene scene) {
        StylesheetTools.replaceStylesheet(scene, PREFIX, this.name());
    }

    public void unAssign(Scene scene) {
        scene.getStylesheets().removeIf(s -> s.endsWith(PREFIX.concat(this.name().toLowerCase(Locale.ROOT).concat(".css"))));
    }

    private Map<String, Color> parseCss() {
        Map<String, Color> hashMap = new HashMap<>();

        try (InputStream is = StylesheetTools.asResourceStream(this.stylesheetName);
             BufferedReader lineReader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            String line;
            while ((line = lineReader.readLine()) != null) {
                line = line.strip();
                if (line.contains("-swatch-") && line.endsWith(";")) {
                    String[] pair = line.replace(";", "").split(":");
                    pair[0] = pair[0].trim().toLowerCase();
                    pair[1] = pair[1].trim().toLowerCase();
                    if (pair[1].startsWith("#")) {
                        hashMap.put(pair[0], Color.web(pair[1]));
                    }
                }
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        return hashMap;
    }

    public Color getColor(SwatchElement element) {
        if (this.colors == null) this.colors = this.parseCss();
        return this.colors.get(element.getTitle());
    }

    public static Swatch getDefault() {
        return BLUE;
    }

    public static Swatch getRandom() {
        return values()[RANDOM.nextInt(values().length)];
    }
}
