/**
 * Copyright (c) 2013-2016 Jens Deters http://www.jensd.de
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.appxi.javafx.glyphfont;

import javafx.scene.text.Font;

/**
 *
 * @author Jens Deters (mail@jensd.de)
 */
public class MaterialIconView extends GlyphIcon<MaterialIcon> {

    static {
        try {
            Font.loadFont(MaterialIcon.class.getResourceAsStream("/appxi/javafx/glyphfont/materialIcons.ttf"), 14);
        } catch (Exception ignored) {
        }
    }

    public MaterialIconView(MaterialIcon icon, String iconSize) {
        super(MaterialIcon.class);
        setIcon(icon);
        setStyle(String.format("-fx-font-family: %s; -fx-font-size: %s;", icon.fontFamily(), iconSize));
    }

    public MaterialIconView(MaterialIcon icon) {
        this(icon, "1em");
    }

    public MaterialIconView() {
        this(MaterialIcon.ANDROID);
    }

    @Override
    public MaterialIcon getDefaultGlyph() {
        return MaterialIcon.ANDROID;
    }

}
