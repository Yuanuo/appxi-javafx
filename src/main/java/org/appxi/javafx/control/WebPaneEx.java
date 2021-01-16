//package org.appxi.javafx.control;
//
//import com.sun.webkit.WebPage;
//import javafx.scene.web.WebEngine;
//
//import java.lang.reflect.Field;
//
//public class WebPaneEx extends WebPane {
//    public WebPaneEx() {
//        super();
//    }
//
//    public final WebPage getWebPage() {
//        try {
//            final WebEngine engine = getWebEngine();
//            Field pageField = engine.getClass().getDeclaredField("page");
//            pageField.setAccessible(true);
//            return (WebPage) pageField.get(engine);
//        } catch (Exception e) {
//            e.printStackTrace();
//            /* log error could not access page */
//        }
//        return null;
//    }
//
//    public boolean find(String stringToFind, boolean forward, boolean wrap, boolean matchCase) {
//        final WebPage webPage = getWebPage();
//        return null != webPage && webPage.find(stringToFind, forward, wrap, matchCase);
//    }
//}
