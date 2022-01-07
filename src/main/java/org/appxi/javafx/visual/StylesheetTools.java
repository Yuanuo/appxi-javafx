package org.appxi.javafx.visual;

import javafx.collections.ObservableList;
import javafx.scene.Scene;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class StylesheetTools {
    private static final Class<?> BASE_CLASS = Theme.class;
    private static final int BUFFER_SIZE = 20480;
    private static final String RESOURCE_ABBR = "css";
    private static String REAL_RESOURCE_ABBR = "jar:";
    private static URLStreamHandlerFactory userURLStreamHandlerFactory = null;

    private StylesheetTools() {
    }

    public static void init(URLStreamHandlerFactory userURLStreamHandlerFactory) {
        StylesheetTools.userURLStreamHandlerFactory = userURLStreamHandlerFactory;
        URL.setURLStreamHandlerFactory(new StylesheetTools.GlsURLStreamHandlerFactory());
    }

    public static String buildResourceName(String stylesheetPrefix, String newStylesheetName) {
        return stylesheetPrefix + newStylesheetName.toLowerCase(Locale.ROOT) + "." + RESOURCE_ABBR;
    }

    public static void replaceStylesheet(Scene scene, String stylesheetPrefix, String newStylesheetName) {
        Objects.requireNonNull(scene, "Scene cannot be null");
        Objects.requireNonNull(stylesheetPrefix, "Stylesheet prefix cannot be null");
        Objects.requireNonNull(scene, "New stylesheet name cannot be null");
        String resourceName = buildResourceName(stylesheetPrefix, newStylesheetName);
        ObservableList<String> sceneStylesheets = scene.getStylesheets();
        List<String> invalidStylesheets = new ArrayList<>();

        for (String s : sceneStylesheets) {
            if (s.contains(stylesheetPrefix)) {
                invalidStylesheets.add(s);
            }
        }

        sceneStylesheets.removeAll(invalidStylesheets);
        sceneStylesheets.add(asResource(resourceName));
    }

    public static void addStylesheet(Scene scene, String stylesheetName) {
        Objects.requireNonNull(scene, "No scene found");
        scene.getStylesheets().add(asResource(stylesheetName));
    }

    public static String asResource(String stylesheetName) {
        String formed = BASE_CLASS.getResource(stylesheetName).toExternalForm();
        if (formed.indexOf("file:") == 0) {
            REAL_RESOURCE_ABBR = "file:";
        } else {
            REAL_RESOURCE_ABBR = "jar:";
        }

        return formed;//.replace(REAL_RESOURCE_ABBR, RESOURCE_ABBR + ":");
    }

    public static InputStream asResourceStream(String stylesheetName) {
        return BASE_CLASS.getResourceAsStream(stylesheetName);
    }

    public static byte[] process(byte[] src) {
        byte[] result = new byte[src.length];

        for (int i = 0; i < src.length; ++i) {
            result[i] = (byte) (result[i] + (src[i] ^ (i + 1) * 2));
        }

        return result;
    }

    private static InputStream process(InputStream stream) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[BUFFER_SIZE];

        try {
            int len;
            while ((len = stream.read(buffer)) > -1) {
                baos.write(buffer, 0, len);
            }

            baos.flush();
        } catch (IOException var5) {
            var5.printStackTrace();
        }

        return new ByteArrayInputStream(process(baos.toByteArray()));
    }

    private static class GlsURLStreamHandlerFactory implements URLStreamHandlerFactory {
        URLStreamHandler streamHandler = new URLStreamHandler() {
            protected URLConnection openConnection(URL url) throws IOException {
                return new StylesheetTools.GlsURLConnection(new URL(url.toString().replace(StylesheetTools.RESOURCE_ABBR + ":", StylesheetTools.REAL_RESOURCE_ABBR)));
            }
        };

        private GlsURLStreamHandlerFactory() {
        }

        public URLStreamHandler createURLStreamHandler(String protocol) {
            return StylesheetTools.RESOURCE_ABBR.equals(protocol) ? this.streamHandler : (StylesheetTools.userURLStreamHandlerFactory != null ? StylesheetTools.userURLStreamHandlerFactory.createURLStreamHandler(protocol) : null);
        }
    }

    private static class GlsURLConnection extends URLConnection {
        public GlsURLConnection(URL url) {
            super(url);
        }

        public void connect() throws IOException {
        }

        public InputStream getInputStream() throws IOException {
            InputStream stream = this.getURL().openStream();
            return this.url.toString().toLowerCase(Locale.ROOT).endsWith("." + StylesheetTools.RESOURCE_ABBR) ? StylesheetTools.process(stream) : stream;
        }
    }
}
