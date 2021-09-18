package org.appxi.javafx.control;

import com.sun.webkit.WebPage;
import com.sun.webkit.event.WCMouseWheelEvent;
import javafx.scene.Node;
import javafx.scene.control.ToolBar;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.web.HTMLEditor;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.appxi.javafx.helper.FxHelper;

import java.lang.reflect.Field;

public class HTMLEditorEx extends HTMLEditor {
    private WebView webView;
    private WebEngine engine;
    private WebPage page;
    private ToolBar topToolbar, bottomToolbar;

    public HTMLEditorEx() {
    }

    public WebView webView() {
        if (null != webView)
            return this.webView;
        //
        final Node node = this.lookup(".web-view");
        if (node instanceof WebView webView) {
            GridPane.setHgrow(webView, Priority.ALWAYS);
            GridPane.setVgrow(webView, Priority.ALWAYS);
            this.webView = webView;
        }
        if (null != webView) {
            this.webView.addEventFilter(ScrollEvent.SCROLL, ev -> {
                final WebPage webPage = webPage();
                if (webPage == null)
                    return;
                webPage.dispatchMouseWheelEvent(new WCMouseWheelEvent(
                        (int) ev.getX(), (int) ev.getY(),
                        (int) ev.getScreenX(), (int) ev.getScreenY(),
                        System.currentTimeMillis(),
                        ev.isShiftDown(), ev.isControlDown(), ev.isAltDown(), ev.isMetaDown(),
                        (float) (-ev.getDeltaX() * webView.getFontScale() * getScaleX() * 5),
                        (float) (-ev.getDeltaY() * webView.getFontScale() * getScaleY() * 5)
                ));
                ev.consume();
            });
        }

        return this.webView;
    }

    public final WebEngine webEngine() {
        if (null != engine)
            return engine;
        this.engine = this.webView().getEngine();
        return this.engine;
    }

    public final WebPage webPage() {
        if (null != page)
            return page;
        try {
            Field pageField = webEngine().getClass().getDeclaredField("page");
            pageField.setAccessible(true);
            return this.page = (WebPage) pageField.get(engine);
        } catch (Throwable throwable) {
            if (!FxHelper.productionMode)
                throwable.printStackTrace();
        }
        return null;
    }

    public ToolBar topToolbar() {
        if (null != this.topToolbar)
            return this.topToolbar;

        Node node = this.lookup(".top-toolbar");
        if (node instanceof ToolBar toolBar) {
            this.topToolbar = toolBar;
        }
        return this.topToolbar;
    }

    public ToolBar bottomToolbar() {
        if (null != this.bottomToolbar)
            return this.bottomToolbar;

        Node node = this.lookup(".bottom-toolbar");
        if (node instanceof ToolBar toolBar) {
            this.bottomToolbar = toolBar;
        }
        return this.bottomToolbar;
    }
}
