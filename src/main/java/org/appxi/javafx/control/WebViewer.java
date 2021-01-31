package org.appxi.javafx.control;

import javafx.beans.Observable;
import javafx.concurrent.Worker;
import javafx.event.Event;
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

import java.util.Set;
import java.util.function.Consumer;

public class WebViewer extends StackPaneEx {
    private WebView viewer;
    private WebEngine engine;

    private ContextMenu contextMenu;
    private Consumer<WebEngine> onLoadSucceedAction, onLoadFailedAction;

    public WebViewer() {
        this.setAlignment(Pos.TOP_LEFT);
        this.getStyleClass().add("web-viewer");
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public final WebView getViewer() {
        if (null == this.viewer) {
            this.viewer = new WebView();
            this.getChildren().add(0, this.viewer);
            FxHelper.setDisabledEffects(viewer);
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

    public void setContextMenu(ContextMenu contextMenu) {
        this.contextMenu = contextMenu;
        if (null == contextMenu) {
            this.viewer.setContextMenuEnabled(true);
            this.viewer.removeEventHandler(MouseEvent.MOUSE_PRESSED, this::handleContextMenuVisible);
            this.viewer.removeEventHandler(ScrollEvent.SCROLL, this::handleContextMenuVisible);
            return;
        }
        this.viewer.setContextMenuEnabled(false);
        this.viewer.addEventHandler(MouseEvent.MOUSE_PRESSED, this::handleContextMenuVisible);
        this.viewer.addEventHandler(ScrollEvent.SCROLL, this::handleContextMenuVisible);
    }

    private void handleContextMenuVisible(Event event) {
        // only for right click
        if (event instanceof MouseEvent mEvent && mEvent.getButton() == MouseButton.SECONDARY) {
            if (this.contextMenu.getItems().isEmpty())
                return;
            this.contextMenu.show(this.viewer, mEvent.getScreenX(), mEvent.getScreenY());
            event.consume();
        } else if (this.contextMenu.isShowing()) {
            this.contextMenu.hide();
            event.consume();
        }
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
        ((Button) dialogPane.lookupButton(ButtonType.CLOSE)).setOnAction(event1 -> {
            viewer.setDisable(false);
            hide(dialogPane);
        });
        viewer.setDisable(true);
        show(dialogPane);
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
