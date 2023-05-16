package org.appxi.javafx.web;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Labeled;
import javafx.scene.control.Tooltip;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.StackPane;
import org.appxi.javafx.app.BaseApp;
import org.appxi.javafx.control.LookupLayer;
import org.appxi.javafx.helper.FxHelper;
import org.appxi.javafx.visual.MaterialIcon;
import org.appxi.util.ext.LookupExpression;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public class WebFindByMarks extends WebFindByMark {
    public final Button more;

    private LookupLayer<String> lookupLayer;
    private Function<String, String> asciiConvertor;

    public WebFindByMarks(BaseApp app, WebPane webPane, StackPane glassPane) {
        super(webPane);

        more = MaterialIcon.MORE_VERT.flatButton();
        more.setTooltip(new Tooltip("在列表中显示"));
        more.setDisable(true);
        more.setOnAction(event -> {
            if (null == lookupLayer) {
                lookupLayer = new LookupLayer<>(glassPane) {
                    @Override
                    protected String getHeaderText() {
                        return "页内查找结果列表";
                    }

                    @Override
                    protected void helpButtonAction(ActionEvent actionEvent) {
                        FxHelper.showTextViewerWindow(app, "webFindByMarks.helpWindow", "页内查找使用方法",
                                """
                                        >> 快捷键：ESC 或 点击透明区 退出此界面；上下方向键选择列表项；回车键打开；
                                        """);
                    }

                    private Set<String> usedKeywords;

                    @Override
                    protected void updateItemLabel(Labeled labeled, String data) {
                        labeled.setText(data.split("#", 2)[1]);
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
                                        if (this.isAsciiKeyword() && null != asciiConvertor) {
                                            String dataInAscii = asciiConvertor.apply(text);
                                            if (null != dataInAscii && dataInAscii.contains(this.keyword()))
                                                return 1;
                                        }
                                        return super.score(data);
                                    }
                                });
                        if (!isInputEmpty && optional.isEmpty()) {
                            // not a valid expression
                            return new LookupResult<>(result.size(), result.size(), result);
                        }
                        final LookupExpression lookupExpression = optional.orElse(null);
                        //
                        String highlights = webPane.executeScript("markFinder.getHighlights()");
                        if (null != highlights && highlights.length() > 0) {
                            highlights.lines().forEach(str -> {
                                String[] arr = str.split("#", 2);
                                if (arr.length != 2 || arr[1].isBlank()) return;

                                final String hTxt = arr[1].strip();

                                double score = isInputEmpty ? 1 : lookupExpression.score(hTxt);
                                if (score > 0)
                                    result.add(arr[0].concat("#").concat(hTxt));
                            });
                        }
                        //
                        if (null != lookupExpression)
                            lookupExpression.keywords().forEach(k -> usedKeywords.add(k.keyword()));
                        if (usedKeywords.isEmpty())
                            usedKeywords.add(searchedText);
                        return new LookupResult<>(result.size(), result.size(), result);
                    }

                    @Override
                    protected void lookupByCommands(String searchTerm, Collection<String> result) {
                    }

                    @Override
                    protected void handleEnterOrDoubleClickActionOnSearchResultList(InputEvent event, String data) {
                        String[] arr = data.split("#", 2);
                        if (arr[0].isEmpty()) return;
                        webPane.executeScript("markFinder.active(%s)".formatted(arr[0]));
                        hide();
                    }
                };
            }
            lookupLayer.show(null);
        });

        getChildren().add(more);
    }

    public final WebFindByMarks setAsciiConvertor(Function<String, String> asciiConvertor) {
        this.asciiConvertor = asciiConvertor;
        return this;
    }

    @Override
    public void find(String text) {
        super.find(text);
        if (null != lookupLayer) lookupLayer.reset();
    }

    @Override
    public void mark(String text) {
        super.mark(text);
        if (null != lookupLayer) lookupLayer.reset();
    }

    @Override
    public void state(int index, int count) {
        super.state(index, count);

        more.setDisable(index <= 0);
    }
}
