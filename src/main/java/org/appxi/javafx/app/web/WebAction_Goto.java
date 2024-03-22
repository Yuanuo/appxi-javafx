package org.appxi.javafx.app.web;

import javafx.event.ActionEvent;
import javafx.scene.control.Labeled;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.StackPane;
import org.appxi.javafx.control.LookupLayer;
import org.appxi.javafx.helper.FxHelper;
import org.appxi.smartcn.pinyin.PinyinHelper;
import org.appxi.util.ext.Attributes;
import org.appxi.util.ext.LookupExpression;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class WebAction_Goto extends WebAction {
    private final String posSelector, txtSelector;
    private LookupLayer<String> lookupLayer;

    public WebAction_Goto(WebViewer webViewer, String posSelector, String txtSelector) {
        super(webViewer);
        this.posSelector = posSelector;
        this.txtSelector = txtSelector;
    }

    public void reset() {
        if (null != lookupLayer) {
            lookupLayer.reset();
        }
    }

    @Override
    public void action() {
        if (null == lookupLayer) {
            lookupLayer = new LookupLayerImpl(webViewer.viewport);
        }
        lookupLayer.show(null);
    }

    class LookupLayerImpl extends LookupLayer<String> {
        public LookupLayerImpl(StackPane owner) {
            super(owner);
        }

        @Override
        protected String getHeaderText() {
            return "快捷跳转 标记/章节/标题";
        }

        @Override
        protected void helpButtonAction(ActionEvent actionEvent) {
            FxHelper.showTextViewerWindow(app, "appGotoChapters.helpWindow", getHeaderText() + "使用方法",
                    """
                            >> 快捷键：Ctrl+T 在阅读视图中开启；ESC 或 点击透明区 退出此界面；上下方向键选择列表项；回车键打开；
                                    """);
        }

        private Set<String> usedKeywords;

        @Override
        protected void updateItemLabel(Labeled labeled, String data) {
            labeled.setText(data.split("\\|\\|", 2)[1]);
            //
            FxHelper.highlight(labeled, usedKeywords);
        }

        @Override
        protected LookupResult<String> lookupByKeywords(String lookupText, int resultLimit) {
            final List<String> result = new ArrayList<>();
            usedKeywords = new LinkedHashSet<>();
            //
            final boolean isInputEmpty = lookupText.isBlank();
            Optional<LookupExpression> optional = isInputEmpty ? Optional.empty() : LookupExpression.of(lookupText,
                    (parent, text) -> new LookupExpression.Keyword(parent, text) {
                        @Override
                        public double score(Object data) {
                            final String text = null == data ? "" : data.toString();
                            if (this.isAsciiKeyword()) {
                                String dataInAscii = PinyinHelper.pinyin(text);
                                if (dataInAscii.contains(this.keyword())) return 1;
                            }
                            return super.score(data);
                        }
                    });
            if (!isInputEmpty && optional.isEmpty()) {
                // not a valid expression
                return new LookupResult<>(0, 0, result);
            }
            final LookupExpression lookupExpression = optional.orElse(null);
            //
            String headings = webPane.executeScript("getPositions('" + posSelector + "', '" + txtSelector + "')");
            if (null != headings && !headings.isEmpty()) {
                headings.lines().forEach(str -> {
                    String[] arr = str.split("\\|\\|", 2);
                    if (arr.length != 2 || arr[1].isBlank()) return;

                    final String hTxt = arr[1].strip();

                    double score = isInputEmpty ? 1 : lookupExpression.score(hTxt);
                    if (score > 0)
                        result.add(arr[0].concat("||").concat(hTxt));
                });
            }
            //
            if (null != lookupExpression)
                lookupExpression.keywords().forEach(k -> usedKeywords.add(k.keyword()));

            return new LookupResult<>(result.size(), result.size(), result);
        }

        @Override
        protected void lookupByCommands(String searchTerm, Collection<String> result) {
        }

        @Override
        protected void handleEnterOrDoubleClickActionOnSearchResultList(InputEvent event, String data) {
            String[] arr = data.split("\\|\\|", 2);
            if (arr[0].isEmpty()) return;
            Attributes item = new Attributes();
            item.attr("position.selector", arr[0]);

            hide();
            webViewer.position(item);
        }

        @Override
        public void hide() {
            super.hide();
            webPane.webView().requestFocus();
        }
    }
}
