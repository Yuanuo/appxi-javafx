package org.appxi.javafx.control;

import com.sun.webkit.WebPage;
import com.sun.webkit.event.WCMouseWheelEvent;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.appxi.javafx.helper.FxHelper;
import org.appxi.javafx.visual.MaterialIcon;
import org.appxi.util.StringHelper;
import org.appxi.util.ext.LookupExpression;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class WebPane extends BorderPane {
    public static void preloadLibrary() {
        try {
            com.sun.webkit.WebPage.getWorkerThreadCount();
        } catch (Throwable ignore) {
        }
    }

    private final EventHandler<MouseEvent> handleContextMenuVisible = this::handleContextMenuVisible;
    private final EventHandler<Event> handleContextMenuHidden = this::handleContextMenuHidden;

    public final ToolBarEx toolbar;

    private WebView webView;
    private WebEngine webEngine;
    private WebPage webPage;

    private Supplier<ContextMenu> contextMenuBuilder;

    public WebPane() {
        super();
        this.getStyleClass().add("web-pane");

        this.toolbar = new ToolBarEx();
        this.toolbar.getStyleClass().addAll("compact", "bob-line");
        this.setTop(this.toolbar);
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

    public void setContextMenuBuilder(Supplier<ContextMenu> contextMenuBuilder) {
        this.contextMenuBuilder = contextMenuBuilder;
        if (null == contextMenuBuilder) {
            this.webView().setContextMenuEnabled(true);
            this.webView().removeEventHandler(MouseEvent.MOUSE_RELEASED, handleContextMenuVisible);
            this.webView().removeEventHandler(MouseEvent.MOUSE_PRESSED, handleContextMenuHidden);
            this.webView().removeEventHandler(ScrollEvent.SCROLL, handleContextMenuHidden);
            return;
        }
        this.webView().setContextMenuEnabled(false);
        this.webView().addEventHandler(MouseEvent.MOUSE_RELEASED, handleContextMenuVisible);
        this.webView().addEventHandler(MouseEvent.MOUSE_PRESSED, handleContextMenuHidden);
        this.webView().addEventHandler(ScrollEvent.SCROLL, handleContextMenuHidden);
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
                this.contextMenu.show(this.webView(), mEvent.getScreenX(), mEvent.getScreenY());
            event.consume();
        }
    }

    public final void patch() {
        this.setCenter(null);
        Platform.runLater(() -> this.setCenter(webView()));
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

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static class WebFinder extends HBox {
        public final TextField input;
        public final Label info;
        public final Button prev, next, clear;

        public WebFinder() {
            super();
            setAlignment(Pos.CENTER);
            getStyleClass().add("web-finder");

            info = new Label();
            info.getStyleClass().add("info");

            prev = MaterialIcon.ARROW_UPWARD.flatButton();
            prev.setTooltip(new Tooltip("上一个"));
            prev.setDisable(true);

            next = MaterialIcon.ARROW_DOWNWARD.flatButton();
            next.setTooltip(new Tooltip("下一个"));
            next.setDisable(true);

            input = new TextField();
            input.setStyle("-fx-alignment: center; -fx-pref-width: 10em;");
            input.setPromptText("页内查找");
            input.setTooltip(new Tooltip("输入即查找，Enter 查找下一个，Shift + Enter 查找上一个"));
            input.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    if (event.isShiftDown()) prev.fire();
                    else next.fire();
                }
            });

            clear = MaterialIcon.CLEAR.flatButton();
            clear.setTooltip(new Tooltip("清除"));
            clear.setDisable(true);

            this.getChildren().setAll(input, info, prev, next, clear);
        }

        public void find(String text) {
            input.requestFocus();
            input.setText(text);
        }

        public void state(int index, int count) {
            prev.setDisable(index <= 0);
            next.setDisable(index <= 0);
            clear.setDisable(index == 0);

            if (index == 0) info.setText(null);
            else if (index < 0) info.setText("0");
            else info.setText(" %d/%d ".formatted(index, count));
        }
    }

    public static class WebMarkFinder extends WebFinder {
        private boolean scrollToVisible = true;
        private Function<String, String> inputConvertor;
        protected String searchedText;

        public WebMarkFinder(WebPane webPane) {
            super();

            prev.setOnAction(event -> webPane.executeScript("markFinder.findPrev()"));
            next.setOnAction(event -> webPane.executeScript("markFinder.findNext()"));
            clear.setOnAction(event -> input.setText(""));
            input.textProperty().addListener((o, ov, nv) -> {
                searchedText = null == nv ? "" : nv.strip();
                if (null != inputConvertor) searchedText = inputConvertor.apply(searchedText);
                if (null == searchedText || searchedText.isBlank()) {
                    webPane.executeScript("markFinder.clear()");
                    return;
                }

                webPane.executeScript("markFinder.find('".concat(searchedText).concat("', " + scrollToVisible + ")"));
            });
        }

        public final WebMarkFinder setInputConvertor(Function<String, String> inputConvertor) {
            this.inputConvertor = inputConvertor;
            return this;
        }

        public void mark(String text) {
            scrollToVisible = false;
            input.setText(text);
            scrollToVisible = true;
        }
    }

    public static class WebMarksFinder extends WebMarkFinder {
        public final Button more;

        private LookupLayer<String> lookupLayer;
        private Function<String, String> asciiConvertor;

        public WebMarksFinder(WebPane webPane, StackPane glassPane) {
            super(webPane);

            more = MaterialIcon.MORE_VERT.flatButton();
            more.setTooltip(new Tooltip("在列表中显示"));
            more.setDisable(true);
            more.setOnAction(event -> {
                if (null == lookupLayer) {
                    lookupLayer = new LookupLayer<>(glassPane) {
                        @Override
                        protected String getHeaderText() {
                            return "页内查找结果列表";
                        }

                        @Override
                        protected String getUsagesText() {
                            return """
                                    >> 快捷键：ESC 或 点击透明区 退出此界面；上下方向键选择列表项；回车键打开；
                                    """;
                        }

                        private Set<String> usedKeywords;

                        @Override
                        protected void updateItemLabel(Labeled labeled, String data) {
                            labeled.setText(data.split("#", 2)[1]);
                            //
                            FxHelper.highlight(labeled, usedKeywords);
                        }

                        @Override
                        protected Collection<String> lookupByKeywords(String lookupText, int resultLimit) {
                            final List<String> result = new ArrayList<>();
                            usedKeywords = new LinkedHashSet<>();
                            //
                            final boolean isInputEmpty = lookupText.isBlank();
                            Optional<LookupExpression> optional = isInputEmpty ? Optional.empty() : LookupExpression.of(lookupText,
                                    (parent, text) -> new LookupExpression.Keyword(parent, text) {
                                        @Override
                                        public double score(Object data) {
                                            final String text = null == data ? "" : data.toString();
                                            if (this.isAsciiKeyword() && null != asciiConvertor) {
                                                String dataInAscii = asciiConvertor.apply(text);
                                                if (null != dataInAscii && dataInAscii.contains(this.keyword()))
                                                    return 1;
                                            }
                                            return super.score(data);
                                        }
                                    });
                            if (!isInputEmpty && optional.isEmpty()) {
                                // not a valid expression
                                return result;
                            }
                            final LookupExpression lookupExpression = optional.orElse(null);
                            //
                            String highlights = webPane.executeScript("markFinder.getHighlights()");
                            if (null != highlights && highlights.length() > 0) {
                                highlights.lines().forEach(str -> {
                                    String[] arr = str.split("#", 2);
                                    if (arr.length != 2 || arr[1].isBlank()) return;

                                    final String hTxt = arr[1].strip();

                                    double score = isInputEmpty ? 1 : lookupExpression.score(hTxt);
                                    if (score > 0)
                                        result.add(arr[0].concat("#").concat(hTxt));
                                });
                            }
                            //
                            if (null != lookupExpression)
                                lookupExpression.keywords().forEach(k -> usedKeywords.add(k.keyword()));
                            if (usedKeywords.isEmpty())
                                usedKeywords.add(searchedText);
                            return result;
                        }

                        @Override
                        protected void lookupByCommands(String searchTerm, Collection<String> result) {
                        }

                        @Override
                        protected void handleEnterOrDoubleClickActionOnSearchResultList(InputEvent event, String data) {
                            String[] arr = data.split("#", 2);
                            if (arr[0].isEmpty()) return;
                            webPane.executeScript("markFinder.active(%s)".formatted(arr[0]));
                            hide();
                        }
                    };
                }
                lookupLayer.show(null);
            });

            getChildren().add(more);
        }

        public final WebMarksFinder setAsciiConvertor(Function<String, String> asciiConvertor) {
            this.asciiConvertor = asciiConvertor;
            return this;
        }

        @Override
        public void find(String text) {
            super.find(text);
            if (null != lookupLayer) lookupLayer.reset();
        }

        @Override
        public void mark(String text) {
            super.mark(text);
            if (null != lookupLayer) lookupLayer.reset();
        }

        @Override
        public void state(int index, int count) {
            super.state(index, count);

            more.setDisable(index <= 0);
        }
    }

    public static class WebPageFinder extends WebFinder {
        private Function<String, String> inputConvertor;

        public WebPageFinder(WebPane webPane) {
            super();

            final Consumer<Boolean> finder = forwardsOrElseBackwards -> {
                String inputText = input.getText();
                if (null != inputText && null != inputConvertor)
                    inputText = inputConvertor.apply(inputText);
                if (null != inputText && !inputText.isBlank() && webPane.findInPage(inputText, forwardsOrElseBackwards)) {
                    prev.setDisable(false);
                    next.setDisable(false);
                    clear.setDisable(false);
                } else {
                    prev.setDisable(true);
                    next.setDisable(true);
                    clear.setDisable(null == inputText || inputText.isEmpty());
                }
            };

            input.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
                if (event.getCode() == KeyCode.ENTER) finder.accept(!event.isShiftDown());
            });
            input.textProperty().addListener((o, ov, nv) -> finder.accept(true));
            prev.setOnAction(event -> finder.accept(false));
            next.setOnAction(event -> finder.accept(true));
        }

        public final WebPageFinder setInputConvertor(Function<String, String> inputConvertor) {
            this.inputConvertor = inputConvertor;
            return this;
        }
    }
}
