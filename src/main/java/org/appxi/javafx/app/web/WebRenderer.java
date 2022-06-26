package org.appxi.javafx.app.web;

import javafx.concurrent.Worker;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import netscape.javascript.JSObject;
import org.appxi.event.EventHandler;
import org.appxi.javafx.app.AppEvent;
import org.appxi.javafx.app.DesktopApp;
import org.appxi.javafx.control.ProgressLayer;
import org.appxi.javafx.helper.FxHelper;
import org.appxi.javafx.visual.VisualEvent;
import org.appxi.javafx.web.WebPane;
import org.appxi.javafx.web.WebSelection;
import org.appxi.javafx.workbench.WorkbenchApp;
import org.appxi.javafx.workbench.WorkbenchPane;
import org.appxi.prefs.UserPrefs;
import org.appxi.util.StringHelper;
import org.appxi.util.ext.RawVal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    public WebRenderer(WorkbenchPane workbench) {
        this.app = workbench.application;
        this.workbench = workbench;
        this.viewport = new StackPane();
        this.webPane = new WebPane();
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
        webPane.executeScript("typeof(setWebStyleTheme) === 'function' && setWebStyleTheme('" + app.visualProvider + "');"
                              + "typeof(setWebStyleSheetLocation) === 'function' && setWebStyleSheetLocation('" + app.visualProvider.getWebStyleSheetURI() + "');"
        );
    }

    /**
     * 响应Web样式修改事件，字体、字号、字色、底色等改变时触发
     */
    protected void onWebStyleSetting(VisualEvent event) {
        // 以不重载页面的方式动态更新
        webPane.executeScript("window._selector = typeof(getScrollTop1Selector) === 'function' && getScrollTop1Selector() || -1;"
                              + "typeof(setWebStyleSheetLocation) === 'function' && setWebStyleSheetLocation('" + app.visualProvider.getWebStyleSheetURI() + "');"
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
        // 对WebView绑定右键菜单请求事件
        this.webPane.setWebViewContextMenuBuilder(this::onWebViewContextMenuRequest);
        // 对WebPane绑定快捷键Pressed事件
        this.webPane.addEventHandler(KeyEvent.KEY_PRESSED, this::onWebPaneShortcutsPressed);
        // 监听主要视图区“宽度”变化
        webPane.widthProperty().addListener(observable ->
                webPane.executeScript("typeof(onBodyResizeBefore) === 'function' && onBodyResizeBefore()"));
    }

    public final void navigate(final Object location) {
        // 检查是否已调用函数initialize
        if (!viewport.getProperties().containsKey(AK_INITIALIZED)) {
            initialize();
        }
        if (webPane.getProperties().containsKey(AK_INITIALIZED)) {
            navigating(location, false);
            return;
        }
        // 在首次使用时需要触发一些特殊操作
        webPane.getProperties().put(AK_INITIALIZED, true);
        progressLayerRemover = ProgressLayer.show(viewport, progressLayer -> FxHelper.runThread(60, () -> {
            // 指定在当前数据目录，避免在公共目录写入数据
            webPane.webEngine().setUserDataDirectory(UserPrefs.cacheDir().toFile());
            //
            webPane.webEngine().getLoadWorker().stateProperty().addListener((o, ov, state) -> {
                if (state == Worker.State.SUCCEEDED) {
                    // set an interface object named 'javaApp' in the web engine's page
                    final JSObject window = webPane.executeScript("window");
                    window.setMember("devMode", !DesktopApp.productionMode);
                    window.setMember("javaApp", webJavaBridge);
                    // 尝试执行onJavaReady函数以通知网页端javaApp已准备就绪
                    final String args = webJavaBridge.getJavaReadyArguments()
                            .stream()
                            .map(v -> "'" + v.key() + "': " + (v.value() instanceof String s ? "`" + s + "`" : v.value()))
                            .collect(Collectors.joining(", "));
                    webPane.executeScript(webJavaBridge.getJavaReadyPreScript()
                                          + ";typeof(onJavaReady) === 'function' && onJavaReady({" + args + "});"
                    );
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
    }

    protected void navigating(Object location, boolean firstTime) {
        Object webContent = createWebContent();
        if (webContent instanceof Path file) {
            webContent = file.toUri();
        }
        if (webContent instanceof URI uri) {
            try {
                String uriStr = uri.toString();
                uriStr = uriStr + (uriStr.contains("?") ? "&" : "?") + StringHelper.concat(
                        "devMode=", !DesktopApp.productionMode,
                        "&theme=" + app.visualProvider.toString().replace(' ', '+')
                );
                uri = new URI(uriStr);
            } catch (Exception ignore) {
            }
            final String uriStr = uri.toString();
            logger.info("load URI: " + uriStr);
            FxHelper.runLater(() -> webPane.webEngine().load(uriStr));
        } else if (webContent instanceof String text) {
            logger.info("load TEXT: " + text.length());
            FxHelper.runLater(() -> webPane.webEngine().loadContent(text));
        } else {
            throw new UnsupportedOperationException("Unsupported webContent: " + webContent);
        }
    }

    /**
     * 响应WebEngine加载成功事件
     */
    protected void onWebEngineLoadSucceeded() {
        webPane.patch();
    }

    /**
     * 响应WebEngine加载失败事件
     */
    protected void onWebEngineLoadFailed() {
    }

    /**
     * 创建当前需要展示的Web内容，
     *
     * @return Path | URI | String
     */
    protected abstract Object createWebContent();

    /**
     * 响应Web视图中右键菜单事件，子类应实现构建MenuItem
     *
     * @param model     菜单列表
     * @param selection 当前选区
     */
    protected void onWebViewContextMenuRequest(List<MenuItem> model, WebSelection selection) {
        if (!DesktopApp.productionMode) {
            MenuItem menuItem = new MenuItem("查看源码（复制到剪贴板）");
            menuItem.setOnAction(event -> FxHelper.copyText(webPane.executeScript("document.documentElement.outerHTML")));
            model.add(menuItem);
        }
    }

    /**
     * 响应WebPane视图中快捷键KeyEvent.KEY_PRESSED事件
     */
    protected void onWebPaneShortcutsPressed(KeyEvent event) {
        if (event.isConsumed()) {
            return;
        }
        // LEFT 避免切换TAB
        if (event.getCode() == KeyCode.LEFT || event.getCode() == KeyCode.KP_LEFT) {
            event.consume();
            return;
        }
        // RIGHT 避免切换TAB
        if (event.getCode() == KeyCode.RIGHT || event.getCode() == KeyCode.KP_RIGHT) {
            event.consume();
            return;
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * for communication from the Javascript engine.
     * <p>
     * 此处必须存在一个实例属性，否则Webkit中会回收掉window.javaApp
     */
    private final WebJavaBridge webJavaBridge = createWebJavaBridge();

    /**
     * 创建用于Javascript engine与Java通信的具体实例
     */
    protected WebJavaBridge createWebJavaBridge() {
        return new WebJavaBridge();
    }

    /**
     * 用于Javascript engine与Java通信的实例基类
     */
    public class WebJavaBridge {
        /**
         * 预定义的在onJavaReady触发之前可以执行的Javascript代码
         */
        protected String getJavaReadyPreScript() {
            // 注入内置函数用于内部功能
            return """
                    if (typeof(window.setWebStyleTheme) !== 'function') {
                        window.setWebStyleTheme = function(theme) {
                            const oldValue = document.body.getAttribute('class') || '';
                            const oldTheme = document.body.getAttribute('theme');
                            let newValue;
                            if (oldTheme && oldTheme.length > 0) {
                                newValue = oldValue.replace(oldTheme, theme);
                            } else {
                                newValue = oldValue.trim() + ' ' + theme;
                            }
                            document.body.setAttribute('theme', theme);
                            document.body.setAttribute('class', newValue);
                        };
                    }
                                                
                    if (typeof(window.setWebStyleSheetLocation) !== 'function') {
                        window.setWebStyleSheetLocation = function(src, _id = 'CSS') {
                           let ele = document.querySelector('html > head > link#' + _id);
                           if (ele) {
                               ele.setAttribute('href', src);
                           } else {
                               ele = document.createElement('link');
                               ele.setAttribute('id', _id);
                               ele.setAttribute('rel', 'stylesheet');
                               ele.setAttribute('type', 'text/css');
                               ele.setAttribute('href', src);
                               document.head.appendChild(ele);
                           }
                        }
                    }
                    """;
        }

        /**
         * 预定义的在onJavaReady触发之时可以传递的参数
         */
        protected List<RawVal<Object>> getJavaReadyArguments() {
            List<RawVal<Object>> args = new ArrayList<>();
            args.add(RawVal.kv("theme", app.visualProvider.toString()));
            return args;
        }

        /**
         * 用于在Javascript中复制文本到系统剪贴板
         *
         * @param text 需要复制的文本
         */
        public void copyText(String text) {
            FxHelper.copyText(text);
        }

        /**
         * 用于在Javascript中获得当前应用设置的Web字色等样式的CSS格式
         */
        public String getWebStyleSheetCSS() {
            return app.visualProvider.getWebStyleSheetCSS();
        }

        /**
         * 用于在Javascript中获得当前应用设置的Web字色等样式的CSS-DATA-URI格式，
         * 可直接应用在link的href属性中。
         */
        public String getWebStyleSheetURI() {
            return app.visualProvider.getWebStyleSheetURI();
        }
    }
}
