package org.appxi.javafx.iconfont;

import javafx.beans.property.ObjectProperty;
import javafx.css.*;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FontIconView extends Text {
    private static final FontIcon defaultFontIcon = new FontIcon() {
        @Override
        public String name() {
            return "HOME";
        }

        @Override
        public char unicode() {
            return 'X';
        }

        @Override
        public String fontFamily() {
            return "System Regular";
        }
    };
    public final static Double DEFAULT_ICON_SIZE = 14.0;
    public final static String DEFAULT_FONT_SIZE = "1.2em"; // 1em==12px

    public final ObjectProperty<String> iconNameProperty = new SimpleStyleableObjectProperty<>(
            StyleableProperties.ICON_NAME, FontIconView.this, "iconName") {
        @Override
        protected void invalidated() {
            FontIcon newIcon;
            try {
                Class clazz = icon.getClass();
                newIcon = (FontIcon) Enum.valueOf(clazz, this.get());
            } catch (Throwable e) {
                newIcon = icon;
            }
            icon = newIcon;
            setText(String.valueOf(icon.unicode()));
        }
    };

    private FontIcon icon;
    private String iconSize;

    public FontIconView() {
        this(defaultFontIcon, null);
    }

    public FontIconView(FontIcon icon) {
        this(icon, null);
    }

    public FontIconView(FontIcon icon, String iconSize) {
        this.iconSize = iconSize;
        getStyleClass().addAll("font-icon");
        //
        setIcon(icon);
    }

    public final FontIcon getIcon() {
        return icon;
    }

    public final void setIcon(FontIcon icon) {
        icon = null == icon ? defaultFontIcon : icon;
        this.icon = icon;
        this.iconNameProperty.set(icon.name());
        //
        this.setIconSize(this.iconSize);
    }

    public String getIconSize() {
        return iconSize;
    }

    public FontIconView setIconSize(String iconSize) {
        this.iconSize = iconSize;

        final String style = "-fx-font-family:".concat(this.icon.fontFamily()).concat(";");
        setStyle(null == this.iconSize ? style : style.concat("-fx-font-size:").concat(iconSize).concat(";"));
        return this;
    }

    private static class StyleableProperties {
        private static final CssMetaData<FontIconView, String> ICON_NAME
                = new CssMetaData<>("-font-icon-name", StyleConverter.getStringConverter(), "HOME") {
            @Override
            public boolean isSettable(FontIconView styleable) {
                return !styleable.iconNameProperty.isBound();
            }

            @Override
            public StyleableProperty<String> getStyleableProperty(FontIconView styleable) {
                return (StyleableProperty) styleable.iconNameProperty;
            }
        };

        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            final List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Text.getClassCssMetaData());
            Collections.addAll(styleables, ICON_NAME);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        return getClassCssMetaData();
    }
}
