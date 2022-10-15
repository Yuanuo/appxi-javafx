package org.appxi.javafx.app.web;

import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.appxi.javafx.app.AppEvent;
import org.appxi.javafx.app.DesktopApp;
import org.appxi.javafx.app.dict.DictionaryEvent;
import org.appxi.javafx.app.search.SearcherEvent;
import org.appxi.javafx.helper.FxHelper;
import org.appxi.javafx.web.WebFindByMarks;
import org.appxi.javafx.web.WebSelection;
import org.appxi.javafx.workbench.WorkbenchPane;
import org.appxi.prefs.UserPrefs;
import org.appxi.smartcn.pinyin.PinyinHelper;
import org.appxi.util.FileHelper;
import org.appxi.util.OSVersions;
import org.appxi.util.StringHelper;
import org.appxi.util.ext.Attributes;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class WebViewer extends WebRenderer {
    public static Path getWebIncludeDir() {
        if (DesktopApp.productionMode) {
            return DesktopApp.appDir().resolve("template/web-incl");
        } else {
            for (int i = 0; i < 3; i++) {
                Path javafxDir = Path.of("../".repeat(i) + "appxi-javafx");
                if (FileHelper.exists(javafxDir)) {
                    return javafxDir.resolve("repo/web-incl");
                }
            }
        }
        return null;
    }

    public static List<String> getWebIncludeURIs() {
        Path dir = getWebIncludeDir();
        if (null == dir) {
            throw new RuntimeException("web-incl not found");
        }
        return Stream.of("jquery.min.js", "jquery.ext.js",
                        "jquery.isinviewport.js", "jquery.scrollto.js",
                        "jquery.mark.js", "jquery.mark.finder.js",
                        "popper.min.js", "tippy-bundle.umd.min.js",
                        "rangy-core.js", "rangy-serializer.js",
                        "app-base.css", "app-base.js")
                .map(s -> dir.resolve(s).toUri().toString())
                .collect(Collectors.toList());
    }

    private WebFindByMarks webFinder;

    private Attributes position;

    public WebViewer(WorkbenchPane workbench) {
        super(workbench);
    }

    public final WebFindByMarks webFinder() {
        return webFinder;
    }

    public final void setPosition(Attributes position) {
        this.position = position;
    }

    public final Attributes popPosition() {
        Attributes result = this.position;
        this.position = null;
        return result;
    }

    protected abstract Object location();

    protected abstract String locationId();

    protected void saveUserData() {
        try {
            final double scrollTopPercentage = this.webPane.getScrollTopPercentage();
            UserPrefs.recents.setProperty(locationId() + ".percent", scrollTopPercentage);

            final String selector = this.webPane.executeScript("getScrollTop1Selector()");
            //            System.out.println(selector + " SET selector for " + path.get());
            UserPrefs.recents.setProperty(locationId() + ".selector", selector);
        } catch (Exception ignore) {
        }
    }

    @Override
    protected void onAppEventStopping(AppEvent event) {
        saveUserData();
        super.onAppEventStopping(event);
    }

    @Override
    public void deinitialize() {
        saveUserData();
        super.deinitialize();
    }

    @Override
    public void initialize() {
        super.initialize();
        //
        webFinder = new WebFindByMarks(this.webPane, viewport);
        webFinder().setAsciiConvertor(PinyinHelper::pinyin);
        this.webPane.getTopBox().addRight(webFinder());
    }

    @Override
    protected void onWebEngineLoadSucceeded() {
        super.onWebEngineLoadSucceeded();
        //
        position(position);
        //
        FxHelper.runThread(50, () -> this.webPane.webView().requestFocus());
    }

    protected final void position(Attributes pos) {
        try {
            if (null != pos && pos.hasAttr("position.term")) {
                String posTerm = pos.removeAttr("position.term");
                String posText = pos.removeAttr("position.text"), longText = null;
                if (null != posText) {
                    List<String> posParts = new ArrayList<>(List.of(posText.split("。")));
                    posParts.sort(Comparator.comparingInt(String::length));
                    longText = posParts.get(posParts.size() - 1);
                }
                if (null != longText && this.webPane.findInPage(longText, true)) {
                    webFinder().mark(posTerm);
                } else if (null != longText && this.webPane.findInWindow(longText, true)) {
                    webFinder().mark(posTerm);
                } else {
                    webFinder().find(posTerm);
                }
            } else if (null != pos && pos.hasAttr("position.selector")) {
                this.webPane.executeScript("setScrollTop1BySelectors(\"" + pos.removeAttr("position.selector") + "\")");
            } else if (null != pos && pos.hasAttr("anchor")) {
                this.webPane.executeScript("setScrollTop1BySelectors(\"" + pos.removeAttr("anchor") + "\")");
            } else {
                final String selector = UserPrefs.recents.getString(locationId() + ".selector", null);
//                final double percent = UserPrefs.recents.getDouble(selectorKey() + ".percent", 0);
                if (null != selector) {
//                    System.out.println(selector + " GET selector for " + path.get());
                    this.webPane.executeScript("setScrollTop1BySelectors(\"" + selector + "\")");
                } else if (location() instanceof Attributes attr && attr.hasAttr("anchor")) {
                    this.webPane.executeScript("setScrollTop1BySelectors(\"" + attr.attr("anchor") + "\")");
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onWebPaneShortcutsPressed(KeyEvent event) {
        if (event.isConsumed()) {
            return;
        }
        // HOME 到页首
        if (event.getCode() == KeyCode.HOME) {
            event.consume();
            this.webPane.executeScript("setScrollTop1BySelectors(null, 0)");
            return;
        }
        // END 到页尾
        if (event.getCode() == KeyCode.END) {
            event.consume();
            this.webPane.executeScript("setScrollTop1BySelectors(null, 1)");
            return;
        }
        // Ctrl + F
        if (event.isShortcutDown() && event.getCode() == KeyCode.F) {
            // 如果有选中文字，则按查找选中文字处理
            String selText1 = this.webPane.executeScript("getValidSelectionText()");
            selText1 = null == selText1 ? null : selText1.strip().replace('\n', ' ');
            webFinder().find(StringHelper.isBlank(selText1) ? null : selText1);
            event.consume();
            return;
        }
        // Ctrl + G, Ctrl + H (Win) / J (Mac) ,MacOS平台上Cmd+H与系统快捷键冲突
        if (event.isShortcutDown() && (event.getCode() == KeyCode.G || event.getCode() == (OSVersions.isMac ? KeyCode.J : KeyCode.H))) {
            // 如果有选中文字，则按查找选中文字处理
            final String selText = this.webPane.executeScript("getValidSelectionText()");
            app.eventBus.fireEvent(event.getCode() == KeyCode.G
                    ? SearcherEvent.ofLookup(selText) // LOOKUP
                    : SearcherEvent.ofSearch(selText) // SEARCH
            );
            event.consume();
            return;
        }
        // Ctrl + D
        if (event.isShortcutDown() && event.getCode() == KeyCode.D) {
            // 如果有选中文字，则按选中文字处理
            String origText = this.webPane.executeScript("getValidSelectionText()");
            String trimText = null == origText ? null : origText.strip().replace('\n', ' ');
            final String availText = StringHelper.isBlank(trimText) ? null : trimText;

            final String str = null == availText ? null : StringHelper.trimChars(availText, 20, "");
            app.eventBus.fireEvent(DictionaryEvent.ofSearch(str));
            event.consume();
            return;
        }
        //
        super.onWebPaneShortcutsPressed(event);
    }

    protected MenuItem createMenu_copy(WebSelection selection) {
        MenuItem menuItem = new MenuItem("复制文字");
        menuItem.setDisable(!selection.hasText);
        menuItem.setOnAction(event -> FxHelper.copyText(selection.text));
        return menuItem;
    }

    protected MenuItem createMenu_search(String textTip, String textForSearch) {
        MenuItem menuItem = new MenuItem("全文检索" + textTip);
        menuItem.setOnAction(event -> app.eventBus.fireEvent(SearcherEvent.ofSearch(textForSearch)));
        return menuItem;
    }

    protected MenuItem createMenu_searchExact(String textTip, String textForSearch) {
        MenuItem menuItem = new MenuItem("全文检索（精确检索）" + textTip);
        menuItem.setOnAction(event -> app.eventBus.fireEvent(
                SearcherEvent.ofSearch(null == textForSearch ? "" : "\"" + textForSearch + "\"")));
        return menuItem;
    }

    protected MenuItem createMenu_lookup(String textTip, String textForSearch) {
        MenuItem menuItem = new MenuItem("快捷检索（经名、作译者等）" + textTip);
        menuItem.setOnAction(event -> app.eventBus.fireEvent(SearcherEvent.ofLookup(textForSearch)));
        return menuItem;
    }

    protected MenuItem createMenu_finder(String textTip, WebSelection selection) {
        MenuItem menuItem = new MenuItem("页内查找" + textTip);
        menuItem.setOnAction(event -> webFinder().find(selection.trims));
        return menuItem;
    }

    protected MenuItem createMenu_dict(WebSelection selection) {
        MenuItem menuItem = new MenuItem();
        if (selection.hasTrims) {
            menuItem.setText("查词典：" + StringHelper.trimChars(selection.trims, 10));
        } else {
            menuItem.setText("查词典");
        }
        menuItem.setOnAction(event -> app.eventBus.fireEvent(DictionaryEvent.ofSearch(
                selection.hasTrims ? StringHelper.trimChars(selection.trims, 20, "") : null)));
        return menuItem;
    }

    protected MenuItem createMenu_pinyin(WebSelection selection) {
        MenuItem menuItem = new MenuItem();
        if (selection.hasTrims) {
            final String str = StringHelper.trimChars(selection.trims, 10, "");
            final String strPy = PinyinHelper.pinyin(str, true);
            menuItem.setText("查拼音：" + strPy);
            menuItem.setOnAction(event -> {
                FxHelper.copyText(str + "\n" + strPy);
                app.toast("已复制拼音到剪贴板！");
            });
        } else {
            menuItem.setText("查拼音：<选择1~10字>，右键查看（点此可复制）");
        }
        return menuItem;
    }

    @Override
    protected WebJavaBridgeImpl createWebJavaBridge() {
        return new WebJavaBridgeImpl();
    }

    public class WebJavaBridgeImpl extends WebJavaBridge {
        public void updateFinderState(int index, int count) {
            if (null != webFinder) {
                webFinder.state(index, count);
            }
        }
    }
}
