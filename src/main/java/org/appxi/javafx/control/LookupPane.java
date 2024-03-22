package org.appxi.javafx.control;

import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.ListCell;
import javafx.scene.control.Tooltip;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.appxi.javafx.helper.FxHelper;
import org.appxi.javafx.visual.MaterialIcon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public abstract class LookupPane<T> extends VBox {
    public final TextInput textInput;
    public final Hyperlink sourceInfo;
    public final Labeled resultInfo;
    public final Button helpButton;
    public final ListViewEx<T> resultList;

    private String searchedText;
    private final ChangeListener<String> _inputChangeListener = (o, ov, nv) -> doSearch();

    public LookupPane() {
        super(3);
        getStyleClass().add("non-transparent");

        textInput = new TextInput();
        textInput.input.setPromptText("在此输入");
        textInput.input.setAlignment(Pos.CENTER);
        textInput.input.textProperty().addListener(_inputChangeListener);

        resultList = new ListViewEx<>(this::handleEnterOrDoubleClickActionOnSearchResultList);
        resultList.setFocusTraversable(false);
        resultList.setCellFactory(v -> new ListCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    this.setText(null);
                    this.setGraphic(null);
                    this.setContentDisplay(null);
                    return;
                }
                updateItemLabel(this, item);
            }
        });

        sourceInfo = new Hyperlink();
        sourceInfo.getStyleClass().add("never-visited");
        textInput.leftArea.getChildren().add(sourceInfo);

        resultInfo = new Label("请输入...");
//        resultInfo.getStyleClass().add("never-visited");
        textInput.rightArea.getChildren().add(resultInfo);

        helpButton = MaterialIcon.HELP_OUTLINE.iconButton(this::helpButtonAction);
        helpButton.setTooltip(new Tooltip("查看用法"));
        textInput.rightArea.getChildren().add(helpButton);

        VBox.setVgrow(resultList, Priority.ALWAYS);
        FxHelper.connectTextFieldAndListView(textInput.input, resultList);
        textInput.input.prefHeightProperty().bind(Bindings.createDoubleBinding(() -> helpButton.getHeight() + 8, helpButton.prefHeightProperty()));

        this.getChildren().setAll(textInput, resultList);
    }

    public final String getSearchedText() {
        return searchedText;
    }

    public final void search(String text) {
        setInputTextSilently(text);
        doSearch();
    }

    private void doSearch() {
        final String text = textInput.input.getText();
        String inputText = null == text ? "" : text.strip();
        if (inputText.length() > 32)
            inputText = inputText.substring(0, 32);
        // 如果相同关键词在上一次获得空列表，此时有必要再次尝试
        if (Objects.equals(this.searchedText, inputText) && !resultList.getItems().isEmpty())
            return;

        long searchedTime = System.currentTimeMillis();
        this.searchedText = inputText;

        final int resultLimit = getResultLimit();
        final LookupResult<T> lookupResult = lookup(inputText, resultLimit);
        // 允许具体实现中无必要进行新搜索时返回null以保持现状
        if (null != lookupResult.items) {
            resultList.getItems().setAll(lookupResult.items);
            resultList.scrollToIfNotVisible(0);
        }

        searchedTime = System.currentTimeMillis() - searchedTime;

        final int resultSize = resultList.getItems().size();
        if (resultSize < 1) {
            resultInfo.setText("未找到匹配项");
        } else if (lookupResult.total == lookupResult.count || lookupResult.total < 1) {
            resultInfo.setText("显示 %d 项".formatted(resultSize));
        } else {
            resultInfo.setText("显示 %d / %d 项".formatted(resultSize, lookupResult.count));
        }
        resultInfo.setText(resultInfo.getText() + " 用时%.2f秒".formatted(searchedTime / 1000f));
    }

    public LookupPane<T> refresh() {
        if (null != resultList) {
            resultList.refresh();
        }
        return this;
    }

    public LookupPane<T> reset() {
        this.searchedText = null;
        resultList.getItems().clear();
        setInputTextSilently(null);
        return this;
    }

    private void setInputTextSilently(String text) {
        textInput.input.textProperty().removeListener(_inputChangeListener);
        textInput.input.setText(text);
        textInput.input.textProperty().addListener(_inputChangeListener);
    }

    protected int getResultLimit() {
        return 200;
    }

    protected abstract void updateItemLabel(Labeled labeled, T item);

    protected abstract void helpButtonAction(ActionEvent actionEvent);

    protected abstract void handleEnterOrDoubleClickActionOnSearchResultList(InputEvent event, T item);

    private LookupResult<T> lookup(String inputText, int resultLimit) {
        if (!inputText.isEmpty() && inputText.charAt(0) == '#') {
            String[] searchTerms = inputText.substring(1).split("[;；]");
            Collection<T> result = new ArrayList<>(searchTerms.length);
            for (String searchTerm : searchTerms) {
                lookupByCommands(searchTerm.strip(), result);
            }
            return new LookupResult<>(result.size(), result.size(), result);
        }
        return lookupByKeywords(inputText, resultLimit);
    }

    protected abstract LookupResult<T> lookupByKeywords(String lookupText, int resultLimit);

    protected abstract void lookupByCommands(String searchTerm, Collection<T> result);

    public static class LookupResult<T> {
        /**
         * 总数据量
         */
        final long total;
        /**
         * 匹配数量
         */
        final long count;
        /**
         * 已限制数量的列表
         */
        final Collection<T> items;

        public LookupResult(long total, long count, Collection<T> items) {
            this.total = total;
            this.count = count;
            this.items = items;
        }
    }
}
