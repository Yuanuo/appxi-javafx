package org.appxi.javafx.web;

import com.sun.webkit.WebPage;
import com.sun.webkit.event.WCMouseWheelEvent;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ScrollBar;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.appxi.javafx.control.HBoxEx;
import org.appxi.javafx.control.ToolBarEx;
import org.appxi.util.StringHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Set;
import java.util.function.Supplier;

public class WebPane extends BorderPane {
    protected static final Logger logger = LoggerFactory.getLogger(WebPane.class);

    public static void preloadLibrary() {
        try {
            com.sun.webkit.WebPage.getWorkerThreadCount();
            com.sun.javafx.webkit.WebConsoleListener.setDefaultListener((webView, message, lineNumber, sourceId) ->
                    logger.warn(StringHelper.concat(message, "[at ", lineNumber, "]")));
        } catch (Throwable e) {
            logger.error("load library for WebView Failed", e);
        }
    }

    private WebView webView;
    private WebEngine webEngine;
    private WebPage webPage;

    private ContextMenu contextMenu;
    private Supplier<ContextMenu> contextMenuBuilder;

    private final EventHandler<MouseEvent> handleContextMenuShow = event -> {
        // only for right click
        if (event.getButton() == MouseButton.SECONDARY) {
            contextMenu = null == contextMenuBuilder ? null : contextMenuBuilder.get();
            if (null != contextMenu)
                this.contextMenu.show(this.webView(), event.getScreenX(), event.getScreenY());
            event.consume();
        }
    };

    private final EventHandler<Event> handleContextMenuHide = event -> {
        if (null != contextMenu && this.contextMenu.isShowing()) {
            this.contextMenu.hide();
            this.contextMenu = null;
            event.consume();
        }
    };

    public WebPane() {
        super();
        this.getStyleClass().add("web-pane");
    }

    public final ToolBarEx getTopBar() {
        HBoxEx box = this.getTopBox();
        Node node = box.getChildren().stream().filter(v -> v instanceof ToolBarEx).findFirst().orElse(null);
        if (node instanceof ToolBarEx topBar) return topBar;

        ToolBarEx topBar = new ToolBarEx();
        HBox.setHgrow(topBar, Priority.ALWAYS);
        topBar.getStyleClass().addAll("compact");
        box.addLeft(topBar);
        return topBar;
    }

    public final HBoxEx getTopBox() {
        Node node = this.getTop();
        if (node instanceof HBoxEx topBox) return topBox;

        HBoxEx topBox = new HBoxEx();
        topBox.setAlignment(Pos.CENTER_LEFT);
        topBox.setSpacing(8);
        topBox.setStyle("-fx-padding: 0;");
        topBox.getStyleClass().addAll("bob-line");
        this.setTop(topBox);
        return topBox;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public final WebView webView() {
        if (null != this.webView) return this.webView;
        this.webView = new WebView();
        this.webView.addEventFilter(ScrollEvent.SCROLL, ev -> {
            final WebPage webPage1 = webPage();
            if (webPage1 == null)
                return;
            webPage1.dispatchMouseWheelEvent(new WCMouseWheelEvent(
                    (int) ev.getX(), (int) ev.getY(),
                    (int) ev.getScreenX(), (int) ev.getScreenY(),
                    System.currentTimeMillis(),
                    ev.isShiftDown(), ev.isControlDown(), ev.isAltDown(), ev.isMetaDown(),
                    (float) (-ev.getDeltaX() * webView.getFontScale() * getScaleX() * 5),
                    (float) (-ev.getDeltaY() * webView.getFontScale() * getScaleY() * 5)
            ));
            ev.consume();
        });
        this.setCenter(this.webView);
        return this.webView;
    }

    public final WebEngine webEngine() {
        if (null != webEngine) return webEngine;
        this.webEngine = this.webView().getEngine();
//        this.webEngine.getLoadWorker().stateProperty().addListener(this::handleWebEngineLoadStateChanged);
        this.webEngine.setOnAlert(event -> {
            final Alert alert = new Alert(Alert.AlertType.NONE);
            alert.setTitle("Web Alert");
            alert.setContentText(event.getData());
            alert.getButtonTypes().add(ButtonType.CLOSE);
            alert.initOwner(this.getScene().getWindow());
            alert.getDialogPane().setPrefSize(640, 480);
            alert.show();
        });
        this.webEngine.setOnError(event -> {
            final Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Web Error");
            alert.setHeaderText(event.getMessage());
            alert.setContentText(StringHelper.getThrowableAsString(event.getException()));
            alert.getButtonTypes().add(ButtonType.CLOSE);
            alert.initOwner(this.getScene().getWindow());
            alert.getDialogPane().setPrefSize(640, 480);
            alert.show();
        });
        return this.webEngine;
    }

    public final WebPage webPage() {
        if (null != webPage) return webPage;
        try {
            Field pageField = webEngine().getClass().getDeclaredField("page");
            pageField.setAccessible(true);
            return this.webPage = (WebPage) pageField.get(webEngine);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return null;
    }

    public final void setContextMenuBuilder(Supplier<ContextMenu> contextMenuBuilder) {
        this.contextMenuBuilder = contextMenuBuilder;
        if (null == contextMenuBuilder) {
            this.webView().setContextMenuEnabled(true);
            this.webView().removeEventHandler(MouseEvent.MOUSE_RELEASED, handleContextMenuShow);
            this.webView().removeEventHandler(MouseEvent.MOUSE_PRESSED, handleContextMenuHide);
            this.webView().removeEventHandler(ScrollEvent.SCROLL, handleContextMenuHide);
            return;
        }
        this.webView().setContextMenuEnabled(false);
        this.webView().addEventHandler(MouseEvent.MOUSE_RELEASED, handleContextMenuShow);
        this.webView().addEventHandler(MouseEvent.MOUSE_PRESSED, handleContextMenuHide);
        this.webView().addEventHandler(ScrollEvent.SCROLL, handleContextMenuHide);
    }

    public final void patch() {
        this.setCenter(null);
        Platform.runLater(() -> this.setCenter(webView()));
    }

    public final void reset() {
        webView = null;
        webEngine = null;
        webPage = null;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public boolean findInPage(String query, boolean forwards) {
        return findInPage(query, forwards, true, false);
    }

    public boolean findInPage(String query, boolean forwards, boolean wrapAround, boolean caseSensitive) {
        try {
            WebPage page = webPage();
            return null != page
                    ? page.find(query, forwards, wrapAround, caseSensitive)
                    : findInWindow(query, forwards, wrapAround, caseSensitive);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean findInWindow(String query, boolean forwards) {
        return findInWindow(query, forwards, true, false);
    }

    public boolean findInWindow(String query, boolean forwards, boolean wrapAround, boolean caseSensitive) {
        WebEngine engine = webEngine();
        if (engine.getDocument() != null) {
            if (null != query && !query.isBlank()) {
                //window.find(aString, aCaseSensitive, aBackwards, aWrapAround, aWholeWord, aSearchInFrames, aShowDialog);
                return (Boolean) engine.executeScript(StringHelper.concat("window.find(\"", query, "\", ",
                        StringHelper.join(",", caseSensitive, !forwards, wrapAround, false, false, false)
                        , ")"));
            }
        }
        return false;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /*  util METHODS  */

    /**
     * Execute script in webEngine
     *
     * @param script
     * @return
     */
    public final <T> T executeScript(String script) {
        return (T) this.webEngine().executeScript(script);
    }

    /**
     * Alert, the given messages will join with '\n'
     *
     * @param messages multiple lines
     */
    public final void alert(Object... messages) {
        this.executeScript("alert(\"" + StringHelper.join("\\n", messages) + "\");");
    }

    /**
     * Scrolls to the specified element by selector.
     *
     * @param selector
     */
    public final void scrollTo(String selector) {
        this.executeScript("document.querySelector(\"" + selector + "\").scrollIntoView();");
    }

    /**
     * Scrolls to the specified position.
     *
     * @param x horizontal scroll value
     * @param y vertical scroll value
     */
    public final void scrollTo(int x, int y) {
        this.executeScript("window.scrollTo(" + x + ", " + y + ")");
    }

    /**
     * Scrolls to the specified position.
     *
     * @param x horizontal scroll value
     * @param y vertical scroll value
     */
    public final void scrollBy(int x, int y) {
        this.executeScript("window.scrollBy(" + x + ", " + y + ")");
    }

    /**
     * Scrolls to the specified position of vertical scroll.
     *
     * @param y vertical scroll value
     */
    public final void scrollTop(int y) {
        this.executeScript("document.body.scrollTop=" + y);
    }

    public final void scrollTop(double y) {
        this.executeScript("document.body.scrollTop=" + y);
    }

    /**
     * Position to scrollTop by percent, it dynamic calculate scrollTop value by vScrollMax.
     *
     * @param percent percent value of scrollTop
     */
    public final void scrollTopPercentage(double percent) {
        scrollTop(percent * getVScrollMax());
    }

    public final double getScrollTopPercentage() {
        return getVScrollValue() / (double) getVScrollMax();
    }

    /**
     * Returns the vertical scroll value, i.e. thumb position.
     * This is equivalent to {@link javafx.scene.control.ScrollBar#getValue().
     *
     * @return vertical scroll value
     */
    public final int getVScrollValue() {
        return this.executeScript("document.body.scrollTop");
    }

    /**
     * Returns the maximum vertical scroll value.
     * This is equivalent to {@link javafx.scene.control.ScrollBar#getMax()}.
     *
     * @return vertical scroll max
     */
    public final int getVScrollMax() {
        return this.executeScript("document.body.scrollHeight");
    }

    /**
     * Returns the horizontal scroll value, i.e. thumb position.
     * This is equivalent to {@link javafx.scene.control.ScrollBar#getValue()}.
     *
     * @return horizontal scroll value
     */
    public final int getHScrollValue() {
        return this.executeScript("document.body.scrollLeft");
    }

    /**
     * Returns the maximum horizontal scroll value.
     * This is equivalent to {@link javafx.scene.control.ScrollBar#getMax()}.
     *
     * @return horizontal scroll max
     */
    public final int getHScrollMax() {
        return this.executeScript("document.body.scrollWidth");
    }

    /**
     * Returns the vertical scrollbar of the webview.
     *
     * @return vertical scrollbar of the webview or {@code null} if no vertical
     * scrollbar exists
     */
    public final ScrollBar getVScrollBar() {
        final Set<Node> scrolls = webView().lookupAll(".scroll-bar");
        for (Node scrollNode : scrolls) {
            if (scrollNode instanceof ScrollBar scroll && scroll.getOrientation() == Orientation.VERTICAL)
                return scroll;
        }
        return null;
    }

    /**
     * Returns the horizontal scrollbar of the webview.
     *
     * @return horizontal scrollbar of the webview or {@code null} if no horizontal
     * scrollbar exists
     */
    public final ScrollBar getHScrollBar() {
        final Set<Node> scrolls = webView().lookupAll(".scroll-bar");
        for (Node scrollNode : scrolls) {
            if (scrollNode instanceof ScrollBar scroll && scroll.getOrientation() == Orientation.HORIZONTAL)
                return scroll;
        }
        return null;
    }
}
