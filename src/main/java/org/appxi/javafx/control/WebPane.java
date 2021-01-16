package org.appxi.javafx.control;

import javafx.beans.Observable;
import javafx.concurrent.Worker;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.ScrollBar;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import org.appxi.javafx.helper.FxHelper;
import org.appxi.util.StringHelper;

import java.util.Set;
import java.util.function.Consumer;

public class WebPane extends StackPaneEx {
    private WebView webView;
    private WebEngine webEngine;

    private Consumer<WebEngine> onLoadSucceedAction, onLoadFailedAction;

    public WebPane() {
        this.setAlignment(Pos.TOP_LEFT);
        this.getStyleClass().add("web-pane");
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public final WebView getWebView() {
        if (null == this.webView) {
            this.webView = new WebView();
            this.getChildren().add(0, this.webView);
            FxHelper.setDisabledEffects(webView);
        }
        return this.webView;
    }

    public final WebEngine getWebEngine() {
        if (null != webEngine)
            return webEngine;
        this.webEngine = this.getWebView().getEngine();
        this.webEngine.getLoadWorker().stateProperty().addListener(this::handleWebEngineLoadStateChanged);
        this.webEngine.setOnAlert(this::handleWebEngineOnAlertEvent);
        return this.webEngine;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void handleWebEngineLoadStateChanged(Observable observable, Worker.State old, Worker.State state) {
        if (state == Worker.State.SUCCEEDED) {
            if (null != this.onLoadSucceedAction)
                this.onLoadSucceedAction.accept(this.webEngine);
        } else if (state == Worker.State.FAILED) {
            if (null != this.onLoadFailedAction)
                this.onLoadFailedAction.accept(this.webEngine);
        }
    }

    public final WebPane setOnLoadSucceedAction(Consumer<WebEngine> onLoadSucceedAction) {
        this.onLoadSucceedAction = onLoadSucceedAction;
        return this;
    }

    public final WebPane setOnLoadFailedAction(Consumer<WebEngine> onLoadFailedAction) {
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
            webView.setDisable(false);
            hide(dialogPane);
        });
        webView.setDisable(true);
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
        return (T) this.getWebEngine().executeScript(script);
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
        final Set<Node> scrolls = getWebView().lookupAll(".scroll-bar");
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
        final Set<Node> scrolls = getWebView().lookupAll(".scroll-bar");
        for (Node scrollNode : scrolls) {
            if (scrollNode instanceof ScrollBar scroll && scroll.getOrientation() == Orientation.HORIZONTAL)
                return scroll;
        }
        return null;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}
