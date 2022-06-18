package org.appxi.javafx.app.web;

import javafx.concurrent.Worker;
import javafx.scene.layout.StackPane;
import netscape.javascript.JSObject;
import org.appxi.event.EventHandler;
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

import java.net.URI;
import java.nio.file.Path;

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

    protected final String getWebStyleSheetLocationScript() {
        return """
                       window.setUserStyleSheetLocation = function(src) {
                           let ele = document.querySelector('html > head > link#CSS');
                           if (ele) {
                               ele.setAttribute('href', src);
                           } else {
                               ele = document.createElement('link');
                               ele.setAttribute('id', 'CSS');
                               ele.setAttribute('rel', 'stylesheet');
                               ele.setAttribute('type', 'text/css');
                               ele.setAttribute('href', src);
                               document.head.appendChild(ele);
                           }
                       };
                       """
               + "setUserStyleSheetLocation('" + app.visualProvider.getWebStyleSheetLocationURI() + "');";
    }

    /**
     * 响应App关闭事件，在App关闭过程中可在此处做一些数据保存等操作
     */
    protected void onAppEventStopping(AppEvent event) {
    }

    /**
     * 响应App样式修改事件
     */
    protected void onAppStyleSetting(VisualEvent event) {
        // APP样式只涉及 明/暗 和 颜色，此时只需直接更改即可
        webPane.executeScript("document.body.setAttribute('class','" + app.visualProvider + "');"
                              + getWebStyleSheetLocationScript());
    }

    /**
     * 响应Web样式修改事件，字体、字号、字色、底色等改变时触发
     */
    protected void onWebStyleSetting(VisualEvent event) {
        // 以不重载页面的方式动态更新
        webPane.executeScript("window._selector = typeof(getScrollTop1Selector) === 'function' && getScrollTop1Selector() || -1;"
                              + getWebStyleSheetLocationScript()
                              + "_selector && _selector !== -1 && setScrollTop1BySelectors(_selector);"
        );
    }

    public void deinitialize() {
        app.eventBus.removeEventHandler(AppEvent.STOPPING, _onAppEventStopping);
        app.eventBus.removeEventHandler(VisualEvent.SET_STYLE, _onAppStyleSetting);
        app.eventBus.removeEventHandler(VisualEvent.SET_WEB_STYLE, _onWebStyleSetting);
        this.webPane.reset();
    }

    public void initialize() {
        // 标记此函数已被显式调用
        viewport.getProperties().put(AK_INITIALIZED, true);
        viewport.getChildren().setAll(this.webPane);
        //
        app.eventBus.addEventHandler(AppEvent.STOPPING, _onAppEventStopping);
        app.eventBus.addEventHandler(VisualEvent.SET_STYLE, _onAppStyleSetting);
        app.eventBus.addEventHandler(VisualEvent.SET_WEB_STYLE, _onWebStyleSetting);
    }

    public final void navigate(final Object location) {
        // 检查是否已调用函数initialize
        if (!viewport.getProperties().containsKey(AK_INITIALIZED)) {
            initialize();
        }
        // 在首次使用时需要触发一些特殊操作
        if (!webPane.getProperties().containsKey(AK_INITIALIZED)) {
            webPane.getProperties().put(AK_INITIALIZED, true);
            progressLayerRemover = ProgressLayer.show(viewport, progressLayer -> FxHelper.runThread(60, () -> {
                webPane.webEngine().setUserDataDirectory(UserPrefs.cacheDir().toFile());
                //
                webPane.webEngine().getLoadWorker().stateProperty().addListener((o, ov, state) -> {
                    if (state == Worker.State.SUCCEEDED) {
                        // set an interface object named 'javaApp' in the web engine's page
                        final JSObject window = webPane.executeScript("window");
                        window.setMember("javaApp", _javaApp);
                        // apply theme; 尝试执行onDocumentReady此函数以通知网页端javaApp已准备就绪
                        webPane.executeScript("document.body.setAttribute('class','" + app.visualProvider + "');" +
                                              "typeof(onDocumentReady) === 'function' && onDocumentReady(window.javaApp)");
                        //
                        webPane.widthProperty().addListener(observable -> {
                            try {
                                webPane.executeScript("typeof(onBodyResizeBefore) === 'function' && onBodyResizeBefore()");
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
        Object webContent = createWebContent();
        if (webContent instanceof Path file) {
            webContent = file.toUri();
        }
        if (webContent instanceof URI uri) {
            try {
                String uriStr = uri.toString();
                String uriParams = "theme=" + app.visualProvider.toString().replace(' ', '+');
                //
                uriStr = uriStr + (uriStr.contains("?") ? "&" : "?") + uriParams;
                uri = new URI(uriStr);
            } catch (Exception ignore) {
            }
            final String uriStr = uri.toString();
            logger.warn("load URI: " + uriStr);
            FxHelper.runLater(() -> webPane.webEngine().load(uriStr));
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

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * for communication from the Javascript engine.
     * <p>
     * 此处必须存在一个实例属性，否则Webkit中会回收掉window.javaApp
     */
    private final WebCallback _javaApp = createWebCallback();

    protected WebCallback createWebCallback() {
        return new WebCallback();
    }

    public static class WebCallback {
        public void log(String msg) {
            logger.warn(msg);
        }
    }
}
