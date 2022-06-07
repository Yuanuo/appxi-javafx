//package org.appxi.javafx.app.web;
//
//import javafx.scene.control.Button;
//import javafx.scene.control.Labeled;
//import javafx.scene.control.Tooltip;
//import javafx.scene.input.InputEvent;
//import org.appxi.javafx.control.LookupLayer;
//import org.appxi.javafx.helper.FxHelper;
//import org.appxi.javafx.visual.MaterialIcon;
//import org.appxi.smartcn.pinyin.PinyinHelper;
//import org.appxi.util.ext.LookupExpression;
//
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.LinkedHashSet;
//import java.util.List;
//import java.util.Optional;
//import java.util.Set;
//
//public class WebToolGoto extends WebTool {
//    public final Button button;
//    private LookupLayer<String> lookupLayer;
//
//    public WebToolGoto(WebRenderer webRenderer) {
//        super(webRenderer);
//        //
//        button = MaterialIcon.NEAR_ME.flatButton();
//        button.setText("转到");
//        button.setTooltip(new Tooltip("转到 (Ctrl+T)"));
//        button.setOnAction(event -> {
//            if (null == lookupLayer) {
//                lookupLayer = new LookupLayerImpl();
//            }
//            lookupLayer.show(null);
//        });
//        //
//        webPane.getTopAsBar().addLeft(button);
//    }
//
//    class LookupLayerImpl extends LookupLayer<String> {
//        LookupLayerImpl() {
//            super(webRenderer.viewport);
//        }
//
//        @Override
//        protected String getHeaderText() {
//            return "快捷跳转章节/标题";
//        }
//
//        @Override
//        protected String getUsagesText() {
//            return """
//                    >> 快捷键：Ctrl+T 在阅读视图中开启；ESC 或 点击透明区 退出此界面；上下方向键选择列表项；回车键打开；
//                    """;
//        }
//
//        private Set<String> usedKeywords;
//
//        @Override
//        protected void updateItemLabel(Labeled labeled, String data) {
//            labeled.setText(data.split("#", 2)[1]);
//            //
//            FxHelper.highlight(labeled, usedKeywords);
//        }
//
//        @Override
//        protected Collection<String> lookupByKeywords(String lookupText, int resultLimit) {
//            final List<String> result = new ArrayList<>();
//            usedKeywords = new LinkedHashSet<>();
//            //
//            final boolean isInputEmpty = lookupText.isBlank();
//            Optional<LookupExpression> optional = isInputEmpty ? Optional.empty() : LookupExpression.of(lookupText,
//                    (parent, text) -> new LookupExpression.Keyword(parent, text) {
//                        @Override
//                        public double score(Object data) {
//                            final String text = null == data ? "" : data.toString();
//                            if (this.isAsciiKeyword()) {
//                                String dataInAscii = PinyinHelper.pinyin(text);
//                                if (dataInAscii.contains(this.keyword())) return 1;
//                            }
//                            return super.score(data);
//                        }
//                    });
//            if (!isInputEmpty && optional.isEmpty()) {
//                // not a valid expression
//                return result;
//            }
//            final LookupExpression lookupExpression = optional.orElse(null);
//            //
//            String headings = webPane.executeScript("getHeadings()");
//            if (null != headings && headings.length() > 0) {
//                headings.lines().forEach(str -> {
//                    String[] arr = str.split("#", 2);
//                    if (arr.length != 2 || arr[1].isBlank()) return;
//
//                    final String hTxt = arr[1].strip();
//
//                    double score = isInputEmpty ? 1 : lookupExpression.score(hTxt);
//                    if (score > 0)
//                        result.add(arr[0].concat("#").concat(hTxt));
//                });
//            }
//            //
//            if (null != lookupExpression)
//                lookupExpression.keywords().forEach(k -> usedKeywords.add(k.keyword()));
//            return result;
//        }
//
//        @Override
//        protected void lookupByCommands(String searchTerm, Collection<String> result) {
//        }
//
//        @Override
//        protected void handleEnterOrDoubleClickActionOnSearchResultList(InputEvent event, String data) {
//            String[] arr = data.split("#", 2);
//            if (arr[0].isEmpty()) return;
//            openedItem.attr("position.selector", "#".concat(arr[0]));
//
//            hide();
//            position(openedItem);
//        }
//
//        @Override
//        public void hide() {
//            super.hide();
//            webPane.webView().requestFocus();
//        }
//    }
//}
