package org.appxi.javafx.app.web;

import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import org.appxi.event.Event;
import org.appxi.javafx.app.BaseApp;
import org.appxi.javafx.app.search.SearcherEvent;
import org.appxi.javafx.helper.FxHelper;
import org.appxi.javafx.web.WebFindByMarks;
import org.appxi.javafx.web.WebPane;
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
        if (!FxHelper.isDevMode) {
            return FxHelper.appDir().resolve("template/web-incl");
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

    public WebViewer(BaseApp app) {
        super(app);
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
            final String locationId = locationId();
            if (null == locationId) {
                return;
            }
            final double scrollTopPercentage = this.webPane.getScrollTopPercentage();
            app.recents.setProperty(locationId + ".percent", scrollTopPercentage);

            final String selector = this.webPane.executeScript("getScrollTop1Selector()");
            //            System.out.println(selector + " SET selector for " + path.get());
            app.recents.setProperty(locationId + ".selector", selector);
        } catch (Exception ignore) {
        }
    }

    @Override
    protected void onAppEventStopping(Event event) {
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
        webFinder = new WebFindByMarks(app, this.webPane, viewport);
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
                if (null != posText && !posText.isBlank()) {
                    if (posText.length() > 50) {
                        posText = posText.substring(0, 50);
                    }
                    int marksLen = webPane.executeScript("mark_text_and_count('" + posText + "')");
                    if (marksLen > 0) {
                        this.webPane.executeScript("setScrollTop1BySelectors('mark')");
                        return;
                    }

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
                final String selector = app.recents.getString(locationId() + ".selector", null);
//                final double percent = UserPrefs.recents.getDouble(selectorKey() + ".percent", 0);
                if (null != selector) {
//                    System.out.println(selector + " GET selector for " + path.get());
                    this.webPane.executeScript("typeof(setScrollTop1BySelectors)==='function' && setScrollTop1BySelectors(\"" + selector + "\")");
                } else if (location() instanceof Attributes attr && attr.hasAttr("anchor")) {
                    this.webPane.executeScript("typeof(setScrollTop1BySelectors)==='function' && setScrollTop1BySelectors(\"" + attr.attr("anchor") + "\")");
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
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

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static void addShortcutKeys(WebViewer webViewer) {
        final WebPane webPane = webViewer.webPane;
        final BaseApp app = webViewer.app;
        // Ctrl + F
        webPane.shortcutKeys.put(new KeyCodeCombination(KeyCode.F, KeyCombination.SHORTCUT_DOWN), (event, selection) -> {
            // 如果有选中文字，则按查找选中文字处理
            String selText = selection.text;
            selText = null == selText ? null : selText.strip().replace('\n', ' ');
            webViewer.webFinder().find(StringHelper.isBlank(selText) ? null : selText);
            event.consume();
        });
        // Ctrl + G
        webPane.shortcutKeys.put(new KeyCodeCombination(KeyCode.G, KeyCombination.SHORTCUT_DOWN), (event, selection) -> {
            // 如果有选中文字，则按查找选中文字处理
            final String selText = selection.text;
            app.eventBus.fireEvent(SearcherEvent.ofLookup(selText));// LOOKUP
            event.consume();
        });
        // Ctrl + H (Win) / J (Mac) ,MacOS平台上Cmd+H与系统快捷键冲突
        webPane.shortcutKeys.put(new KeyCodeCombination(OSVersions.isMac ? KeyCode.J : KeyCode.H, KeyCombination.SHORTCUT_DOWN), (event, selection) -> {
            // 如果有选中文字，则按查找选中文字处理
            final String selText = selection.text;
            app.eventBus.fireEvent(SearcherEvent.ofSearch(selText)); // SEARCH
            event.consume();
        });
    }

    public static void addShortcutMenu(WebRenderer webViewer) {
        final WebPane webPane = webViewer.webPane;
        final BaseApp app = webViewer.app;
        //
        webPane.shortcutMenu.add(selection -> {
            MenuItem menuItem = new MenuItem("复制文字");
            menuItem.getProperties().put(WebPane.GRP_MENU, "copy");
            menuItem.setDisable(!selection.hasText);
            menuItem.setOnAction(event -> FxHelper.copyText(selection.text));
            return List.of(menuItem);
        });
        webPane.shortcutMenu.add(selection -> {
            String textTip = selection.hasTrims ? "：" + StringHelper.trimChars(selection.trims, 8) : "";
            String textForSearch = selection.hasTrims ? selection.trims : null;

            final List<MenuItem> menuItems = new ArrayList<>();
            //
            MenuItem search = new MenuItem("全文检索" + textTip);
            menuItems.add(search);
            search.getProperties().put(WebPane.GRP_MENU, "search");
            search.setOnAction(event -> app.eventBus.fireEvent(SearcherEvent.ofSearch(textForSearch)));
            //
            MenuItem searchExact = new MenuItem("全文检索（精确检索）" + textTip);
            menuItems.add(searchExact);
            searchExact.getProperties().put(WebPane.GRP_MENU, "search");
            searchExact.setOnAction(event -> app.eventBus.fireEvent(
                    SearcherEvent.ofSearch(null == textForSearch ? "" : "\"" + textForSearch + "\"")));
            //
            MenuItem lookup = new MenuItem("快捷检索（经名、作译者等）" + textTip);
            menuItems.add(lookup);
            lookup.getProperties().put(WebPane.GRP_MENU, "search");
            lookup.setOnAction(event -> app.eventBus.fireEvent(SearcherEvent.ofLookup(textForSearch)));
            //

            if (webViewer instanceof WebViewer webViewer1) {
                MenuItem findInPage = new MenuItem("页内查找" + textTip);
                menuItems.add(findInPage);
                findInPage.getProperties().put(WebPane.GRP_MENU, "search");
                findInPage.setOnAction(event -> webViewer1.webFinder().find(selection.trims));
            }

            //
            return menuItems;
        });
        webPane.shortcutMenu.add(selection -> {
            MenuItem menuItem = new MenuItem();
            menuItem.getProperties().put(WebPane.GRP_MENU, "search2");
            if (selection.hasTrims) {
                final String str = StringHelper.trimChars(selection.trims, 10, "");
                final String strPy = PinyinHelper.pinyin(str, true, false, " ");
                menuItem.setText("查拼音：" + strPy);
                menuItem.setOnAction(event -> {
                    FxHelper.copyText(str + "\n" + strPy);
                    app.toast("已复制拼音到剪贴板！");
                });
            } else {
                menuItem.setText("查拼音：<选择1~10字>，右键查看（点此可复制）");
            }
            return List.of(menuItem);
        });
    }
}
