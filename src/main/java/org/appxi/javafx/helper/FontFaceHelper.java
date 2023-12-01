package org.appxi.javafx.helper;

import com.sun.javafx.font.FontResource;
import com.sun.javafx.font.PrismFontFactory;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.appxi.util.ext.RawVal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public final class FontFaceHelper {
    private static final Logger logger = LoggerFactory.getLogger(FontFaceHelper.class);

    private static boolean fontFamiliesFixed;

    /**
     * @noinspection unchecked
     */
    public static void fixFontFamilies() {
        try {
            PrismFontFactory factory = PrismFontFactory.getFontFactory();
            //
            Field fileNameToFontResourceMapFld = PrismFontFactory.class.getDeclaredField("fileNameToFontResourceMap");
            fileNameToFontResourceMapFld.setAccessible(true);
            //
            Field fontResourceMapFld = PrismFontFactory.class.getDeclaredField("fontResourceMap");
            fontResourceMapFld.setAccessible(true);
            Map<String, Object> fontResourceMap = (Map<String, Object>) fontResourceMapFld.get(factory);
            //
            Field fontToFamilyNameMapFld = PrismFontFactory.class.getDeclaredField("fontToFamilyNameMap");
            fontToFamilyNameMapFld.setAccessible(true);
            HashMap<String, String> fontToFamilyNameMap = (HashMap<String, String>) fontToFamilyNameMapFld.get(factory);

            //
            Collection<?> values = ((Map<?, ?>) fileNameToFontResourceMapFld.get(factory)).values();
            for (Object obj : values) {
                if (!(obj instanceof FontResource fr)) {
                    continue;
                }
                String familyName = fr.getFamilyName();
                for (final String font : new String[]{
                        familyName, fr.getLocaleFamilyName(),
                        fr.getFullName(), fr.getLocaleFullName(),
                        fr.getPSName()
                }) {
                    String fontName = font.toLowerCase();
                    if (!fontResourceMap.containsKey(fontName)) {
                        if (logger.isWarnEnabled()) {
                            logger.warn("add alias font " + fontName + " to family " + familyName);
                        }
                        fontResourceMap.put(fontName, fr);
                    }
                    if (!fontToFamilyNameMap.containsKey(fontName)) {
                        fontToFamilyNameMap.put(fontName, font);
                    }
                }
            }
            fontFamiliesFixed = !values.isEmpty();
        } catch (Throwable e) {
            logger.error("unknown", e);
        }
    }

    public static List<RawVal<String>> getFontFamilies() {
        final List<RawVal<String>> result = new ArrayList<>(64);

        if (!fontFamiliesFixed) {
            Font.getFamilies().forEach(family -> result.add(new RawVal<>(family, family)));
            return result;
        }
        try {
            final HashSet<FontResource> addedFonts = new HashSet<>();
            PrismFontFactory factory = PrismFontFactory.getFontFactory();
            //
            for (String family : Font.getFamilies()) {
                FontResource fr = factory.getFontResource(family, null, false);
                if (null == fr || addedFonts.contains(fr)) {
                    continue;
                }
                addedFonts.add(fr);

                String engName = fr.getFullName();
                String locName = fr.getLocaleFamilyName();
                result.add(new RawVal<>(family, engName.equalsIgnoreCase(locName) ? engName : engName + " (" + locName + ")"));
            }
        } catch (Throwable e) {
            Font.getFamilies().forEach(family -> result.add(new RawVal<>(family, family)));
        }
        return result;
    }

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
