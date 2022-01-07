package org.appxi.javafx.visual;

public enum SwatchElement {
    PRIMARY_50("-primary-swatch-50"),
    PRIMARY_100("-primary-swatch-100"),
    PRIMARY_200("-primary-swatch-200"),
    PRIMARY_300("-primary-swatch-300"),
    PRIMARY_400("-primary-swatch-400"),
    PRIMARY_500("-primary-swatch-500"),
    PRIMARY_600("-primary-swatch-600"),
    PRIMARY_700("-primary-swatch-700"),
    PRIMARY_800("-primary-swatch-800"),
    PRIMARY_900("-primary-swatch-900"),
    ALTERNATE_100("-alternate-swatch-100"),
    ALTERNATE_200("-alternate-swatch-200"),
    ALTERNATE_400("-alternate-swatch-400"),
    ALTERNATE_700("-alternate-swatch-700");

    private final String title;

    SwatchElement(String title) {
        this.title = title;
    }

    public String getTitle() {
        return this.title;
    }
}
