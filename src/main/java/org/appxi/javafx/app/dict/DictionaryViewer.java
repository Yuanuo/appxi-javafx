package org.appxi.javafx.app.dict;

import appxi.dict.DictionaryApi;
import appxi.dict.SearchMode;
import appxi.dict.doc.DictEntry;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tooltip;
import org.appxi.javafx.app.web.WebCallback;
import org.appxi.javafx.app.web.WebRenderer;
import org.appxi.javafx.app.web.WebToolPrinter;
import org.appxi.javafx.app.web.WebViewer;
import org.appxi.javafx.visual.MaterialIcon;
import org.appxi.util.StringHelper;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

class DictionaryViewer extends WebViewer {
    final DictionaryController controller;
    final DictEntry dictEntry;

    private boolean _searchAllDictionaries;

    public DictionaryViewer(DictionaryController controller, DictEntry dictEntry) {
        super(controller.workbench, null);
        this.controller = controller;
        this.dictEntry = dictEntry;
    }

    @Override
    protected DictEntry location() {
        return dictEntry;
    }

    @Override
    protected String locationId() {
        return "dict@" + dictEntry.dictionary.id + "@" + dictEntry.id;
    }

    @Override
    public void saveUserData() {
        //
    }

    @Override
    protected List<InputStream> getAdditionalStyleSheets() {
        return null == controller.webCssSupplier ? List.of() : controller.webCssSupplier.get();
    }

    @Override
    public void install() {
        super.install();
        //
        addTool_searchAllDictionaries();
        new WebToolPrinter(this);
    }

    protected void addTool_searchAllDictionaries() {
        Button button = MaterialIcon.TRAVEL_EXPLORE.flatButton();
        button.setText("查全部词典");
        button.setTooltip(new Tooltip("从所有词典中精确查词“" + dictEntry.title + "”"));
        button.setOnAction(event -> {
            _searchAllDictionaries = true;
            navigate(null);
            // 已从全部词典查询，此时禁用掉此按钮
            button.setDisable(true);
        });
        //
        webPane().getTopBar().addLeft(button);
    }

    @Override
    protected Object createWebContent() {
        final StringBuilder buff = new StringBuilder();
        //
        buff.append("<!DOCTYPE html><html lang=\"zh\"><head><meta charset=\"UTF-8\">");
        StringHelper.buildWebIncludes(buff, controller.webIncludesSupplier.get());
        //
        buff.append("</head><body><article style=\"padding: 0 1rem;\">");
        //
        if (_searchAllDictionaries) {
            List<DictEntry> list = new ArrayList<>();
            DictionaryApi.api().searchTitle(SearchMode.TitleEquals, dictEntry.title, null, null)
                    .forEachRemaining(entry -> list.add(entry.dictEntry.dictionary == dictEntry.dictionary ? 0 : list.size(), entry.dictEntry));
            for (DictEntry entry : list) {
                buff.append(DictionaryApi.toHtmlDocument(entry));
            }
        } else {
            buff.append(DictionaryApi.toHtmlDocument(dictEntry));
        }
        //
        buff.append("</article></body></html>");
        //
        return buff.toString();
    }

    @Override
    protected WebCallback createWebCallback() {
        return new WebCallbackImpl(this);
    }

    protected ContextMenu createWebViewContextMenu() {
        final String origText = webPane().executeScript("getValidSelectionText()");
        String trimText = null == origText ? null : origText.strip().replace('\n', ' ');
        final String availText = StringHelper.isBlank(trimText) ? null : trimText;
        //
        String textTip = null == availText ? "" : "：".concat(StringHelper.trimChars(availText, 8));
        String textForSearch = availText;
        //
        MenuItem copyRef = new MenuItem("复制引用");
        copyRef.setDisable(true);

        //
        return new ContextMenu(
                createMenu_copy(origText, availText),
                copyRef,
                new SeparatorMenuItem(),
                createMenu_search(textTip, textForSearch),
                createMenu_searchExact(textTip, textForSearch),
                createMenu_lookup(textTip, textForSearch),
                createMenu_finder(textTip, availText),
                new SeparatorMenuItem(),
                createMenu_dict(availText),
                createMenu_pinyin(availText)
        );
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public class WebCallbackImpl extends WebCallback {
        public WebCallbackImpl(WebRenderer webRenderer) {
            super(webRenderer);
        }

        public void seeAlso(String dictId, String keyword) {
            controller.app.eventBus.fireEvent(DictionaryEvent.ofSearchExact(dictId, keyword));
        }
    }
}
