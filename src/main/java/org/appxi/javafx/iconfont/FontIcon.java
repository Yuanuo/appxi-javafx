package org.appxi.javafx.iconfont;

public interface FontIcon {
    String name();

    char unicode();

    String fontFamily();

    default FontIconView iconView() {
        return new FontIconView(this);
    }

    default FontIconView iconView(String iconSize) {
        return new FontIconView(this, iconSize);
    }
}
