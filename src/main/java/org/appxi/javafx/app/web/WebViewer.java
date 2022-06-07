package org.appxi.javafx.app.web;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.Clipboard;
import javafx.scene.input.DataFormat;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import org.appxi.javafx.app.AppEvent;
import org.appxi.javafx.app.dict.DictionaryEvent;
import org.appxi.javafx.app.search.SearcherEvent;
import org.appxi.javafx.web.WebFindByMarks;
import org.appxi.javafx.workbench.WorkbenchPane;
import org.appxi.prefs.UserPrefs;
import org.appxi.smartcn.pinyin.PinyinHelper;
import org.appxi.util.StringHelper;
import org.appxi.util.ext.Attributes;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public abstract class WebViewer extends WebRenderer {
    protected WebFindByMarks webFinder;
    protected Attributes position;

    public WebViewer(WorkbenchPane workbench, StackPane viewport) {
        super(workbench, viewport);
    }

    protected abstract Object location();

    protected abstract String locationId();

    public void saveUserData() {
        if (null != this.webPane()) {
            try {
                final double scrollTopPercentage = webPane().getScrollTopPercentage();
                UserPrefs.recents.setProperty(locationId() + ".percent", scrollTopPercentage);

                final String selector = webPane().executeScript("getScrollTop1Selector()");
                //            System.out.println(selector + " SET selector for " + path.get());
                UserPrefs.recents.setProperty(locationId() + ".selector", selector);
            } catch (Exception ignore) {
            }
        }
    }

    @Override
    protected void onAppEventStopping(AppEvent event) {
        saveUserData();
        super.onAppEventStopping(event);
    }

    @Override
    public void uninstall() {
        saveUserData();
        super.uninstall();
    }

    @Override
    public void install() {
        super.install();
        //
        webFinder = new WebFindByMarks(webPane(), viewport);
        webFinder.setAsciiConvertor(PinyinHelper::pinyin);
        webPane().getTopBox().addRight(webFinder);
        //
        webPane().addEventHandler(KeyEvent.KEY_PRESSED, this::handleWebViewShortcuts);
    }

    @Override
    protected void onWebEngineLoadSucceeded() {
        super.onWebEngineLoadSucceeded();
        //
        webPane().setContextMenuBuilder(this::createWebViewContextMenu);
        //
        position(position);
        //
        webPane().webView().requestFocus();
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
                if (null != longText && webPane().findInPage(longText, true)) {
                    webFinder.mark(posTerm);
                } else if (null != longText && webPane().findInWindow(longText, true)) {
                    webFinder.mark(posTerm);
                } else {
                    webFinder.find(posTerm);
                }
            } else if (null != pos && pos.hasAttr("position.selector")) {
                webPane().executeScript("setScrollTop1BySelectors(\"" + pos.removeAttr("position.selector") + "\")");
            } else if (null != pos && pos.hasAttr("anchor")) {
                webPane().executeScript("setScrollTop1BySelectors(\"" + pos.removeAttr("anchor") + "\")");
            } else {
                final String selector = UserPrefs.recents.getString(locationId() + ".selector", null);
//                final double percent = UserPrefs.recents.getDouble(selectorKey() + ".percent", 0);
                if (null != selector) {
//                    System.out.println(selector + " GET selector for " + path.get());
                    webPane().executeScript("setScrollTop1BySelectors(\"" + selector + "\")");
                } else if (location() instanceof Attributes attr && attr.hasAttr("anchor")) {
                    webPane().executeScript("setScrollTop1BySelectors(\"" + attr.attr("anchor") + "\")");
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    protected void handleWebViewShortcuts(KeyEvent event) {
        onWebShortcut_findInPage(event);
        onWebShortcut_search(event);
        onWebShortcut_dict(event);
    }

    protected void onWebShortcut_findInPage(KeyEvent event) {
        // Ctrl + F
        if (!event.isConsumed() && event.isShortcutDown()
                && event.getCode() == KeyCode.F) {
            // 如果有选中文字，则按查找选中文字处理
            String selText = webPane().executeScript("getValidSelectionText()");
            selText = null == selText ? null : selText.strip().replace('\n', ' ');
            webFinder.find(StringHelper.isBlank(selText) ? null : selText);
            event.consume();
        }
    }

    protected void onWebShortcut_search(KeyEvent event) {
        // Ctrl + G, Ctrl + H
        if (!event.isConsumed() && event.isShortcutDown()
                && (event.getCode() == KeyCode.G || event.getCode() == KeyCode.H)) {
            // 如果有选中文字，则按查找选中文字处理
            final String selText = webPane().executeScript("getValidSelectionText()");
            app.eventBus.fireEvent(event.getCode() == KeyCode.G
                    ? SearcherEvent.ofLookup(selText) // LOOKUP
                    : SearcherEvent.ofSearch(selText) // SEARCH
            );
            event.consume();
        }
    }

    protected void onWebShortcut_dict(KeyEvent event) {
        // Ctrl + D
        if (!event.isConsumed() && event.isShortcutDown()
                && event.getCode() == KeyCode.D) {
            // 如果有选中文字，则按选中文字处理
            String origText = webPane().executeScript("getValidSelectionText()");
            String trimText = null == origText ? null : origText.strip().replace('\n', ' ');
            final String availText = StringHelper.isBlank(trimText) ? null : trimText;

            final String str = null == availText ? null : StringHelper.trimChars(availText, 20, "");
            app.eventBus.fireEvent(DictionaryEvent.ofSearch(str));
            event.consume();
        }
    }

    protected ContextMenu createWebViewContextMenu() {
        String origText = webPane().executeScript("getValidSelectionText()");
        String trimText = null == origText ? null : origText.strip().replace('\n', ' ');
        final String availText = StringHelper.isBlank(trimText) ? null : trimText;
//        String textTip = null == availText ? "" : "：".concat(StringHelper.trimChars(availText, 8));
//        String textForSearch = null == availText ? null : prepareTextForSearch(availText);
        return new ContextMenu();
    }

    protected MenuItem createMenu_copy(String origText, String availText) {
        MenuItem menuItem = new MenuItem("复制文字");
        menuItem.setDisable(null == availText);
        menuItem.setOnAction(event -> Clipboard.getSystemClipboard().setContent(Map.of(DataFormat.PLAIN_TEXT, origText)));
        return menuItem;
    }

    protected MenuItem createMenu_search(String textTip, String textForSearch) {
        MenuItem menuItem = new MenuItem("全文检索".concat(textTip));
        menuItem.setOnAction(event -> app.eventBus.fireEvent(SearcherEvent.ofSearch(textForSearch)));
        return menuItem;
    }

    protected MenuItem createMenu_searchExact(String textTip, String textForSearch) {
        MenuItem menuItem = new MenuItem("全文检索（精确检索）".concat(textTip));
        menuItem.setOnAction(event -> app.eventBus.fireEvent(
                SearcherEvent.ofSearch(null == textForSearch ? "" : "\"".concat(textForSearch).concat("\""))));
        return menuItem;
    }

    protected MenuItem createMenu_lookup(String textTip, String textForSearch) {
        MenuItem menuItem = new MenuItem("快捷检索（经名、作译者等）".concat(textTip));
        menuItem.setOnAction(event -> app.eventBus.fireEvent(SearcherEvent.ofLookup(textForSearch)));
        return menuItem;
    }

    protected MenuItem createMenu_finder(String textTip, String availText) {
        MenuItem menuItem = new MenuItem("页内查找".concat(textTip));
        menuItem.setOnAction(event -> webFinder.find(availText));
        return menuItem;
    }

    protected MenuItem createMenu_dict(String availText) {
        MenuItem menuItem = new MenuItem();
        if (null != availText) {
            final String str = StringHelper.trimChars(availText, 10);
            menuItem.setText("查词典：" + str);
        } else {
            menuItem.setText("查词典");
        }
        menuItem.setOnAction(event -> {
            final String str = null == availText ? null : StringHelper.trimChars(availText, 20, "");
            app.eventBus.fireEvent(DictionaryEvent.ofSearch(str));
        });
        return menuItem;
    }

    protected MenuItem createMenu_pinyin(String availText) {
        MenuItem menuItem = new MenuItem();
        if (null != availText) {
            final String str = StringHelper.trimChars(availText, 10, "");
            final String strPy = PinyinHelper.pinyin(str, true);
            menuItem.setText("查拼音：" + strPy);
            menuItem.setOnAction(event -> {
                Clipboard.getSystemClipboard().setContent(Map.of(DataFormat.PLAIN_TEXT, str + "\n" + strPy));
                app.toast("已复制拼音到剪贴板！");
            });
        } else {
            menuItem.setText("查拼音：<选择1~10字>，并点击可复制");
        }
        return menuItem;
    }

}
