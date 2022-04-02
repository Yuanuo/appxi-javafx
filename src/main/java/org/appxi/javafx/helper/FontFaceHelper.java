package org.appxi.javafx.helper;

import com.sun.javafx.font.FontResource;
import com.sun.javafx.font.PrismFontFactory;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.appxi.util.StringHelper;
import org.appxi.util.ext.RawVal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public final class FontFaceHelper {
    private static final Logger logger = LoggerFactory.getLogger(FontFaceHelper.class);

    /**
     * @noinspection unchecked
     */
    public static void fixing() {
        final HashSet<String> filterExists = new HashSet<>();
        try {
            for (String family : Font.getFamilies()) {
                FontResource fr = PrismFontFactory.getFontFactory().getFontResource(family, null, false);
                if (null != fr) {
                    filterExists.add(StringHelper.join("/", fr.getFamilyName(), fr.getLocaleFamilyName(), fr.getFullName(), fr.getLocaleFullName()));
                }
            }
        } catch (Throwable e) {
            logger.error("unknown", e);

            filterExists.addAll(Font.getFamilies());
        }

        try {
            PrismFontFactory factory = PrismFontFactory.getFontFactory();
            Field fileNameToFontResourceMapFld = PrismFontFactory.class.getDeclaredField("fileNameToFontResourceMap");
            fileNameToFontResourceMapFld.setAccessible(true);
            //
            Field allFamilyNamesFld = PrismFontFactory.class.getDeclaredField("allFamilyNames");
            allFamilyNamesFld.setAccessible(true);
            List<String> allFamilyNames = (List<String>) allFamilyNamesFld.get(factory);

            //
            for (Object obj : ((Map<?, ?>) fileNameToFontResourceMapFld.get(factory)).values()) {
                if (obj instanceof FontResource fr) {
                    String id = StringHelper.join("/", fr.getFamilyName(), fr.getLocaleFamilyName(), fr.getFullName(), fr.getLocaleFullName());
                    if (filterExists.contains(id)) continue;
                    filterExists.add(id);
                    //
                    logger.warn("fixing font " + id);
                    if (null != Font.loadFont(Path.of(fr.getFileName()).toUri().toString(), Font.getDefault().getSize()))
                        allFamilyNames.add(fr.getFamilyName());
                }
            }
            Collections.sort(allFamilyNames);
        } catch (Throwable e) {
            logger.error("unknown", e);
        }
    }

    public static List<RawVal<String>> getFontFamilies() {
        final List<RawVal<String>> result = new ArrayList<>(64);

        final HashSet<String> filterExists = new HashSet<>();
        try {
            for (String family : Font.getFamilies()) {
                FontResource fr = PrismFontFactory.getFontFactory().getFontResource(family, null, false);
                if (null != fr && family.equalsIgnoreCase(fr.getFamilyName())) {
                    String id = StringHelper.join("/", fr.getFamilyName(), fr.getLocaleFamilyName(), fr.getFullName(), fr.getLocaleFullName());
                    if (filterExists.contains(id)) continue;
                    filterExists.add(id);
                    //
                    String engName = fr.getFullName();
                    String locName = fr.getLocaleFullName();
                    result.add(new RawVal<>(family, engName.equals(locName) ? engName : engName + " (" + locName + ")"));
                } else {
                    if (filterExists.contains(family)) continue;
                    filterExists.add(family);
                    //
                    result.add(new RawVal<>(family, family));
                }
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
