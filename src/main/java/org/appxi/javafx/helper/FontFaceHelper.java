package org.appxi.javafx.helper;

import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class FontFaceHelper {
    private static final Map<Integer, FontFace> CACHE = new HashMap<>(4096);

    static {
        register(new FontFace("SimSun", 0x0000, 0x00FF, "5em"));
        register(new FontFace("'Microsoft YaHei'", 0x4e00, 0x9fa5, "3em"));
    }

    public static void register(FontFace fontFace) {
        for (int i = fontFace.unicodeStart; i <= fontFace.unicodeEnd; i++) {
            CACHE.put(i, fontFace);
        }
    }

    public static List<Text> wrap(String text) {
        FontFaceHelper.FontFace curr, last = null;
        StringBuilder buff = null;
        final List<Text> texts = new ArrayList<>();
        for (int i = 0; i < text.length(); i++) {
            int codePoint = text.codePointAt(i);
            curr = FontFaceHelper.CACHE.get(codePoint);
            if (curr != last) {
                if (null != buff)
                    texts.add(FontFaceHelper.createText(last, buff.toString()));
                buff = new StringBuilder();
            }
            buff.appendCodePoint(codePoint);
            //
            last = curr;
        }
        if (null != buff && !buff.isEmpty())
            texts.add(FontFaceHelper.createText(last, buff.toString()));
        return texts;
    }

    private static Text createText(FontFace fontFace, String text) {
        Text text1 = new Text(text);
        if (null != fontFace) {
            StringBuilder buff = new StringBuilder();
            buff.append("-fx-font-family: ".concat(fontFace.fontFamily).concat(";"));
            if (null != fontFace.fontSize) {
                buff.append("-fx-font-size: ".concat(fontFace.fontSize).concat(";"));
            }
            text1.setStyle(buff.toString());
        }
        return text1;
    }

    public record FontFace(String fontFamily, int unicodeStart, int unicodeEnd, String fontSize) {
    }
}
