package org.appxi.javafx.control;

import com.sun.webkit.WebPage;
import com.sun.webkit.event.WCMouseWheelEvent;
import javafx.beans.Observable;
import javafx.concurrent.Worker;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import org.appxi.javafx.helper.FxHelper;
import org.appxi.util.StringHelper;

import java.lang.reflect.Field;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class WebViewer extends StackPane {
    private final EventHandler<MouseEvent> handleContextMenuVisible = this::handleContextMenuVisible;
    private final EventHandler<Event> handleContextMenuHidden = this::handleContextMenuHidden;
    private WebView viewer;
    private WebEngine engine;
    private WebPage page;

    private final MaskingPane masking = new MaskingPane();

    private Supplier<ContextMenu> contextMenuBuilder;
    private Consumer<WebEngine> onLoadSucceedAction, onLoadFailedAction;

    public WebViewer() {
        this.setAlignment(Pos.TOP_LEFT);
        this.getStyleClass().add("web-viewer");
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public final WebView getViewer() {
        if (null == this.viewer) {
            this.viewer = new WebView();
            this.viewer.addEventFilter(ScrollEvent.SCROLL, ev -> {
                if (getPage() == null)
                    return;
                getPage().dispatchMouseWheelEvent(new WCMouseWheelEvent(
                        (int) ev.getX(), (int) ev.getY(),
                        (int) ev.getScreenX(), (int) ev.getScreenY(),
                        System.currentTimeMillis(),
                        ev.isShiftDown(), ev.isControlDown(), ev.isAltDown(), ev.isMetaDown(),
                        (float) (-ev.getDeltaX() * viewer.getFontScale() * getScaleX() * 5),
                        (float) (-ev.getDeltaY() * viewer.getFontScale() * getScaleY() * 5)
                ));
                ev.consume();
            });
            this.getChildren().add(0, this.viewer);
        }
        return this.viewer;
    }

    public final WebEngine getEngine() {
        if (null != engine)
            return engine;
        this.engine = this.getViewer().getEngine();
        this.engine.getLoadWorker().stateProperty().addListener(this::handleWebEngineLoadStateChanged);
        this.engine.setOnAlert(this::handleWebEngineOnAlertEvent);
        return this.engine;
    }

    public final WebPage getPage() {
        if (null != page)
            return page;
        try {
            Field pageField = getEngine().getClass().getDeclaredField("page");
            pageField.setAccessible(true);
            return this.page = (WebPage) pageField.get(engine);
        } catch (Throwable throwable) {
            if (!FxHelper.productionMode)
                throwable.printStackTrace();
        }
        return null;
    }

    public void setContextMenuBuilder(Supplier<ContextMenu> contextMenuBuilder) {
        this.contextMenuBuilder = contextMenuBuilder;
        if (null == contextMenuBuilder) {
            this.viewer.setContextMenuEnabled(true);
            this.viewer.removeEventHandler(MouseEvent.MOUSE_RELEASED, handleContextMenuVisible);
            this.viewer.removeEventHandler(MouseEvent.MOUSE_PRESSED, handleContextMenuHidden);
            this.viewer.removeEventHandler(ScrollEvent.SCROLL, handleContextMenuHidden);
            return;
        }
        this.viewer.setContextMenuEnabled(false);
        this.viewer.addEventHandler(MouseEvent.MOUSE_RELEASED, handleContextMenuVisible);
        this.viewer.addEventHandler(MouseEvent.MOUSE_PRESSED, handleContextMenuHidden);
        this.viewer.addEventHandler(ScrollEvent.SCROLL, handleContextMenuHidden);
    }

    private ContextMenu contextMenu;

    private void handleContextMenuHidden(Event event) {
        if (null != contextMenu && this.contextMenu.isShowing()) {
            this.contextMenu.hide();
            this.contextMenu = null;
            event.consume();
        }
    }

    private void handleContextMenuVisible(Event event) {
        // only for right click
        if (event instanceof MouseEvent mEvent && mEvent.getButton() == MouseButton.SECONDARY) {
            contextMenu = null == contextMenuBuilder ? null : contextMenuBuilder.get();
            if (null != contextMenu)
                this.contextMenu.show(this.viewer, mEvent.getScreenX(), mEvent.getScreenY());
            event.consume();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public boolean findInPage(String query, boolean forwards) {
        return findInPage(query, forwards, true, false);
    }

    public boolean findInPage(String query, boolean forwards, boolean wrapAround, boolean caseSensitive) {
        try {
            WebPage page = getPage();
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
        WebEngine engine = getEngine();
        if (engine.getDocument() != null) {
            if (null != query && !query.isBlank()) {
                //window.find(aString, aCaseSensitive, aBackwards, aWrapAround, aWholeWord, aSearchInFrames, aShowDialog);
                return (Boolean) engine.executeScript(StringHelper.concat("window.find(\"", query, "\", ",
                        StringHelper.joinArray(",", caseSensitive, !forwards, wrapAround, false, false, false)
                        , ")"));
            }
        }
        return false;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void handleWebEngineLoadStateChanged(Observable observable, Worker.State old, Worker.State state) {
        if (state == Worker.State.SUCCEEDED) {
            if (null != this.onLoadSucceedAction)
                this.onLoadSucceedAction.accept(this.engine);
        } else if (state == Worker.State.FAILED) {
            if (null != this.onLoadFailedAction)
                this.onLoadFailedAction.accept(this.engine);
        }
    }

    public final WebViewer setOnLoadSucceedAction(Consumer<WebEngine> onLoadSucceedAction) {
        this.onLoadSucceedAction = onLoadSucceedAction;
        return this;
    }

    public final WebViewer setOnLoadFailedAction(Consumer<WebEngine> onLoadFailedAction) {
        this.onLoadFailedAction = onLoadFailedAction;
        return this;
    }

    private void handleWebEngineOnAlertEvent(WebEvent<String> event) {
        final DialogPane dialogPane = new DialogPane();
        StackPane.setAlignment(dialogPane, Pos.CENTER);
        dialogPane.setPrefSize(640, 480);
        dialogPane.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        dialogPane.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        dialogPane.setHeaderText("Alert");
        dialogPane.setContentText(event.getData());
        dialogPane.getButtonTypes().add(ButtonType.CLOSE);
        ((Button) dialogPane.lookupButton(ButtonType.CLOSE)).setOnAction(e -> getChildren().removeAll(masking, dialogPane));
        getChildren().addAll(masking, dialogPane);
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
        return (T) this.getEngine().executeScript(script);
    }

    /**
     * Alert, the given messages will join with '\n'
     *
     * @param messages multiple lines
     */
    public final void alert(Object... messages) {
        this.executeScript("alert(\"" + StringHelper.joinArray("\\n", messages) + "\");");
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
        final Set<Node> scrolls = getViewer().lookupAll(".scroll-bar");
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
        final Set<Node> scrolls = getViewer().lookupAll(".scroll-bar");
        for (Node scrollNode : scrolls) {
            if (scrollNode instanceof ScrollBar scroll && scroll.getOrientation() == Orientation.HORIZONTAL)
                return scroll;
        }
        return null;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}
