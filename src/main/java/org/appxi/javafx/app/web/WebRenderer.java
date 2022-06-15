package org.appxi.javafx.app.web;

import javafx.concurrent.Worker;
import javafx.scene.layout.StackPane;
import netscape.javascript.JSObject;
import org.appxi.event.EventHandler;
import org.appxi.holder.RawHolder;
import org.appxi.javafx.app.AppEvent;
import org.appxi.javafx.control.ProgressLayer;
import org.appxi.javafx.helper.FxHelper;
import org.appxi.javafx.visual.VisualEvent;
import org.appxi.javafx.web.WebPane;
import org.appxi.javafx.workbench.WorkbenchApp;
import org.appxi.javafx.workbench.WorkbenchPane;
import org.appxi.prefs.UserPrefs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public abstract class WebRenderer {
    protected static final Logger logger = LoggerFactory.getLogger(WebRenderer.class);

    private static final Object AK_INITIALIZED = new Object();

    private final EventHandler<AppEvent> _onAppEventStopping = this::onAppEventStopping;
    private final EventHandler<VisualEvent> _onAppStyleSetting = this::onAppStyleSetting;
    private final EventHandler<VisualEvent> _onWebStyleSetting = this::onWebStyleSetting;

    public final WorkbenchApp app;
    public final WorkbenchPane workbench;
    public final StackPane viewport;
    public final WebPane webPane;

    private Runnable progressLayerRemover;

    public WebRenderer(WorkbenchPane workbench, StackPane viewport) {
        this.app = workbench.application;
        this.workbench = workbench;
        this.viewport = null != viewport ? viewport : new StackPane();
        this.webPane = new WebPane();
    }

    protected void onAppEventStopping(AppEvent event) {
    }

    protected void onAppStyleSetting(VisualEvent event) {
        this.applyStyleSheet();
    }

    protected void onWebStyleSetting(VisualEvent event) {
        final String selector = webPane.executeScript("getScrollTop1Selector()");
        this.applyStyleSheet();
        webPane.executeScript("setScrollTop1BySelectors(\"" + selector + "\")");
    }

    private void applyStyleSheet() {
        final RawHolder<byte[]> cssBytes = new RawHolder<>();
        cssBytes.value = """
                :root {
                    --font-family: tibetan, "%s", AUTO !important;
                    --zoom: %.2f !important;
                    --text-color: %s;
                }
                body {
                    background-color: %s;
                }
                """.formatted(
                        app.visualProvider.webFontName(),
                        app.visualProvider.webFontSize(),
                        app.visualProvider.webTextColor(),
                        app.visualProvider.webPageColor()
                )
                .getBytes(StandardCharsets.UTF_8);

        Consumer<InputStream> combiner = stream -> {
            try (BufferedInputStream in = new BufferedInputStream(stream)) {
                int pos = cssBytes.value.length;
                byte[] tmpBytes = new byte[pos + in.available()];
                System.arraycopy(cssBytes.value, 0, tmpBytes, 0, pos);
                cssBytes.value = tmpBytes;
                //noinspection ResultOfMethodCallIgnored
                in.read(cssBytes.value, pos, in.available());
            } catch (Exception exception) {
                logger.warn("combine css", exception);
            }
        };
        // 加载主题相关的CSS
        Optional.ofNullable(VisualEvent.class.getResourceAsStream("web.css")).ifPresent(combiner);
        // 加载应用相关的CSS
        Optional.ofNullable(getAdditionalStyleSheets()).ifPresent(v -> v.forEach(combiner));
        // 编码成base64并应用
        String cssData = "data:text/css;charset=utf-8;base64," + Base64.getMimeEncoder().encodeToString(cssBytes.value);
        FxHelper.runLater(() -> {
            webPane.webEngine().setUserStyleSheetLocation(cssData);
            webPane.executeScript("document.body.setAttribute('class','" + app.visualProvider + "');");
        });
    }

    /**
     * 获取应用相关的CSS
     */
    protected abstract List<InputStream> getAdditionalStyleSheets();

    public void deinitialize() {
        app.eventBus.removeEventHandler(AppEvent.STOPPING, _onAppEventStopping);
        app.eventBus.removeEventHandler(VisualEvent.SET_STYLE, _onAppStyleSetting);
        app.eventBus.removeEventHandler(VisualEvent.SET_WEB_STYLE, _onWebStyleSetting);
        this.webPane.reset();
    }

    public void initialize() {
        viewport.getChildren().setAll(this.webPane);
        //
        app.eventBus.addEventHandler(AppEvent.STOPPING, _onAppEventStopping);
        app.eventBus.addEventHandler(VisualEvent.SET_STYLE, _onAppStyleSetting);
        app.eventBus.addEventHandler(VisualEvent.SET_WEB_STYLE, _onWebStyleSetting);
    }

    public final void navigate(final Object location) {
        if (!webPane.getProperties().containsKey(AK_INITIALIZED)) {
            webPane.getProperties().put(AK_INITIALIZED, true);
            progressLayerRemover = ProgressLayer.show(viewport, progressLayer -> FxHelper.runThread(60, () -> {
                webPane.webEngine().setUserDataDirectory(UserPrefs.cacheDir().toFile());
                // apply theme
                this.applyStyleSheet();
                //
                webPane.webEngine().getLoadWorker().stateProperty().addListener((o, ov, state) -> {
                    if (state == Worker.State.SUCCEEDED) {
                        // set an interface object named 'javaApp' in the web engine's page
                        final JSObject window = webPane.executeScript("window");
                        window.setMember("javaApp", createWebCallback());
                        // apply theme
                        webPane.executeScript("document.body.setAttribute('class','" + app.visualProvider + "');");
                        //
                        webPane.widthProperty().addListener(observable -> {
                            try {
                                webPane.executeScript("onBodyResizeBefore()");
                            } catch (Throwable ignore) {
                            }
                        });
                        //
                        onWebEngineLoadSucceeded();
                    } else if (state == Worker.State.FAILED) {
                        onWebEngineLoadFailed();
                    }
                    if ((state == Worker.State.SUCCEEDED || state == Worker.State.FAILED) && null != progressLayerRemover) {
                        progressLayerRemover.run();
                        progressLayerRemover = null;
                    }
                });
                //
                navigating(location, true);
            }));
        } else {
            navigating(location, false);
        }
    }

    protected void navigating(Object location, boolean firstTime) {
        final Object webContent = createWebContent();
        if (webContent instanceof Path file) {
            logger.warn("load FILE: " + file);
            FxHelper.runLater(() -> webPane.webEngine().load(file.toUri().toString()));
        } else if (webContent instanceof URI uri) {
            logger.warn("load URI: " + uri);
            FxHelper.runLater(() -> webPane.webEngine().load(uri.toString()));
        } else if (webContent instanceof String text) {
            logger.warn("load TEXT: " + text.length());
            FxHelper.runLater(() -> webPane.webEngine().loadContent(text));
        }
    }

    protected void onWebEngineLoadSucceeded() {
        webPane.patch();
    }

    protected void onWebEngineLoadFailed() {
    }

    protected abstract Object createWebContent();

    protected abstract WebCallback createWebCallback();

}
