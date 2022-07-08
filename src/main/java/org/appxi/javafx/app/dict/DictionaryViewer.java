package org.appxi.javafx.app.dict;

import appxi.dict.DictEntry;
import appxi.dict.DictionaryApi;
import appxi.dict.SearchMode;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tooltip;
import org.appxi.javafx.app.web.WebToolPrinter;
import org.appxi.javafx.app.web.WebViewer;
import org.appxi.javafx.visual.MaterialIcon;
import org.appxi.javafx.web.WebSelection;
import org.appxi.prefs.UserPrefs;
import org.appxi.util.FileHelper;
import org.appxi.util.StringHelper;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

class DictionaryViewer extends WebViewer {
    final DictionaryController controller;
    final DictEntry dictEntry;

    private boolean _searchAllDictionaries;

    public DictionaryViewer(DictionaryController controller, DictEntry dictEntry) {
        super(controller.workbench);
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
    protected void saveUserData() {
        //
    }

    @Override
    public void initialize() {
        super.initialize();
        //
        addTool_searchAllDictionaries();
        new WebToolPrinter(this);
    }

    protected void addTool_searchAllDictionaries() {
        Button button = MaterialIcon.TRAVEL_EXPLORE.flatButton();
        button.setText("查全部词典");
        button.setTooltip(new Tooltip("从所有词典中精确查词“" + dictEntry.title() + "”"));
        button.setOnAction(this::onSearchAllDictionaries);
        //
        this.webPane.getTopBar().addLeft(button);
    }

    void onSearchAllDictionaries(ActionEvent event) {
        _searchAllDictionaries = true;
        navigate(null);
        // 已从全部词典查询，此时禁用掉此按钮
        ((Button) event.getSource()).setDisable(true);
    }

    @Override
    protected Object createWebContent() {
        final StringBuilder buff = new StringBuilder();
        //
        buff.append("<!DOCTYPE html><html lang=\"zh\"><head><meta charset=\"UTF-8\">");
        StringHelper.buildWebIncludes(buff, controller.webIncludesSupplier.get());
        buff.append("""
                <script type="text/javascript">
                function onJavaReady(args) {
                    setWebStyleTheme(args.theme);
                }
                </script>
                """);
        //
        buff.append("</head><body><article style=\"padding: 0 1rem;\">");
        //
        StringBuilder htmlDoc = new StringBuilder();
        if (_searchAllDictionaries) {
            List<DictEntry> list = new ArrayList<>();
            DictionaryApi.api().searchTitle(SearchMode.TitleEquals, dictEntry.title(), null, null)
                    .forEachRemaining(entry -> list.add(entry.dictionary == dictEntry.dictionary ? 0 : list.size(), entry));
            for (DictEntry entry : list) {
                htmlDoc.append(DictionaryApi.toHtmlDocument(entry));
            }
        } else {
            htmlDoc.append(DictionaryApi.toHtmlDocument(dictEntry));
        }
        if (null != controller.htmlDocumentWrapper) {
            buff.append(controller.htmlDocumentWrapper.apply(htmlDoc.toString()));
        } else {
            buff.append(htmlDoc);
        }
        //
        buff.append("</article></body></html>");
        // 由于词条内容可能涉及特殊字符，此处使用本地文件以保证正常显示
        String tempInfo = dictEntry.dictionary.id + "." + dictEntry.id + (_searchAllDictionaries ? ".all" : "");
        Path tempFile = UserPrefs.cacheDir().resolve(FileHelper.makeEncodedPath(tempInfo, ".html"));
        FileHelper.writeString(buff.toString(), tempFile);
        return tempFile;
    }

    @Override
    protected WebJavaBridgeImpl createWebJavaBridge() {
        return new WebJavaBridgeImpl();
    }

    protected void onWebViewContextMenuRequest(List<MenuItem> model, WebSelection selection) {
        super.onWebViewContextMenuRequest(model, selection);
        //
        String textTip = selection.hasTrims ? "：" + StringHelper.trimChars(selection.trims, 8) : "";
        String textForSearch = selection.hasTrims ? selection.trims : null;
        //
        MenuItem copyRef = new MenuItem("复制引用");
        copyRef.setDisable(true);

        //
        model.add(createMenu_copy(selection));
        model.add(copyRef);
        model.add(new SeparatorMenuItem());
        model.add(createMenu_search(textTip, textForSearch));
        model.add(createMenu_searchExact(textTip, textForSearch));
        model.add(createMenu_lookup(textTip, textForSearch));
        model.add(createMenu_finder(textTip, selection));
        model.add(new SeparatorMenuItem());
        model.add(createMenu_dict(selection));
        model.add(createMenu_pinyin(selection));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public class WebJavaBridgeImpl extends WebViewer.WebJavaBridgeImpl {
        public void seeAlso(String dictId, String keyword) {
            app.eventBus.fireEvent(DictionaryEvent.ofSearchExact(dictId, keyword));
        }
    }
}
