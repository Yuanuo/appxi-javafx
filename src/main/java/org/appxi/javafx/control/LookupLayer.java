package org.appxi.javafx.control;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.appxi.javafx.helper.FxHelper;
import org.appxi.util.StringHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public abstract class LookupLayer<T> {
    private final StackPane glassPane;

    private DialogLayer dialogLayer;
    private Label searchInfo;
    private TextField searchInput;
    private ListViewEx<T> searchResult;

    private boolean searching;
    private String searchedText;

    public LookupLayer(StackPane glassPane) {
        this.glassPane = glassPane;
    }

    public final void search(String text) {
        if (null != searchInput) {
            searchInput.setText(text);
            doSearch();
        }
    }

    private void doSearch() {
        final String text = searchInput.getText();
        String inputText = null == text ? "" : text.strip();
        if (inputText.length() > 32)
            inputText = inputText.substring(0, 32);
        // 如果相同关键词在上一次获得空列表，此时有必要再次尝试
        if (Objects.equals(this.searchedText, inputText) && !searchResult.getItems().isEmpty())
            return;
        searching = true;
//        try {
//            System.out.println("Quick Searching for " +
//                    new String(inputText.getBytes(), "GBK")
//                    + ", searchedText = " + new String(searchedText.getBytes(), "GBK"));
//        } catch (Exception ignore) {
//        }
        this.searchedText = inputText;

        final int resultLimit = getResultLimit();
        final Collection<T> lookupResult = lookup(inputText, resultLimit);
        // 允许具体实现中无必要进行新搜索时返回null以保持现状
        if (null != lookupResult) {
            searchResult.getItems().setAll(lookupResult);
            searchResult.scrollToIfNotVisible(0);
        }
        if (this.searchResult.getItems().isEmpty()) {
            searchInfo.setText("请输入...");
        } else {
            int matches = searchResult.getItems().size();
            searchInfo.setText(matches < 1
                    ? "未找到匹配项"
                    : StringHelper.concat("找到 ", Math.min(matches, resultLimit), matches > resultLimit ? "+" : "", " 项"));
        }
        searching = false;
    }

    public void refresh() {
        if (null != searchResult) searchResult.refresh();
    }

    public void reset() {
        this.searchedText = null;
        if (null != searchResult) searchResult.getItems().clear();
    }

    public void hide() {
        if (null != dialogLayer && dialogLayer.isShowing()) dialogLayer.hide();
    }

    public void show(String searchText) {
        if (null == dialogLayer) {
            final EventHandler<Event> handleEventToHide = evt -> {
                boolean handled = false;
                if (evt instanceof KeyEvent event) {
                    handled = dialogLayer.isShowing() && event.getCode() == KeyCode.ESCAPE;
                } else if (evt instanceof MouseEvent event) {
                    handled = dialogLayer.isShowing() && event.getButton() == MouseButton.PRIMARY;
                } else if (evt instanceof ActionEvent) {
                    handled = dialogLayer.isShowing();
                }
                if (handled) {
                    if (searching)
                        searching = false;
                    else hide();
                    evt.consume();
                }
            };

            dialogLayer = new DialogLayer();
            dialogLayer.setOnKeyPressed(handleEventToHide);
            dialogLayer.setOnMousePressed(Event::consume);
            dialogLayer.opaqueLayer.setOnMousePressed(handleEventToHide);

            int pad = getPaddingSizeOfParent();
            dialogLayer.autoPadding(pad, pad);

            dialogLayer.setHeaderText(getHeaderText());

            searchInfo = new Label("请输入...");
            dialogLayer.setGraphic(searchInfo);

            final Label labelUsages = new Label(getUsagesText());
            labelUsages.setWrapText(true);
            VBox.setVgrow(labelUsages, Priority.ALWAYS);

            //
            searchInput = new TextField();
            searchInput.setPromptText("在此输入");
            searchInput.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
                switch (event.getCode()) {
                    case CONTROL, SHIFT, SHORTCUT, ALT, META -> {
                        return;
                    }
                }
                doSearch();
            });
            searchInput.addEventHandler(KeyEvent.KEY_TYPED, event -> {
                if (!searchInput.isEditable() || searchInput.isDisabled()) return;

                final String character = event.getCharacter();
                if (character.length() == 0) return;
                String autoPairLeft = null, autoPairRight = null;
                switch (character) {
                    case "\"", "'" -> {
                        autoPairLeft = character;
                        autoPairRight = character;
                    }
                    case "“" -> {
                        autoPairLeft = character;
                        autoPairRight = "”";
                    }
                    case "(" -> {
                        autoPairLeft = character;
                        autoPairRight = ")";
                    }
                    case "（" -> {
                        autoPairLeft = character;
                        autoPairRight = "）";
                    }
                }
                if (null != autoPairLeft) {
                    final String selectedText = searchInput.getSelectedText();
                    String changedText;
                    if (null == selectedText || selectedText.isEmpty()) {
                        changedText = autoPairLeft.concat(autoPairRight);
                        searchInput.insertText(searchInput.getCaretPosition(), changedText);
                        searchInput.backward();
                    } else if (selectedText.matches("[" + autoPairLeft + "].*[" + autoPairRight + "]")) {
                        changedText = selectedText.substring(1, selectedText.length() - 1);
                        searchInput.replaceSelection(changedText);
                    } else {
                        changedText = autoPairLeft.concat(selectedText).concat(autoPairRight);
                        searchInput.replaceSelection(changedText);
                        searchInput.backward();
                    }
                    event.consume();
                }
            });
            searchResult = new ListViewEx<>(this::handleEnterOrDoubleClickActionOnSearchResultList);
            VBox.setVgrow(searchResult, Priority.ALWAYS);
            FxHelper.connectTextFieldAndListView(searchInput, searchResult);
            searchResult.setFocusTraversable(false);
            searchResult.setCellFactory(v -> new ListCell<>() {
                T updatedItem;

                @Override
                protected void updateItem(T item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        updatedItem = null;
                        this.setText(null);
                        this.setGraphic(null);
                        this.setContentDisplay(null);
                        return;
                    }
                    if (item == updatedItem)
                        return;//
                    updatedItem = item;
                    updateItemLabel(this, item);
                }
            });

            dialogLayer.setContent(new VBox(3, labelUsages, searchInput, searchResult));
        }
        if (!dialogLayer.isShowing()) {
            dialogLayer.show(glassPane);
        }
        searchInput.requestFocus();
        search(null != searchText ? searchText : this.searchedText);
        searchInput.selectAll();
    }

    protected int getPaddingSizeOfParent() {
        return 100;
    }

    protected int getResultLimit() {
        return 200;
    }

    protected abstract void  updateItemLabel(Labeled labeled, T item);

    protected abstract String getHeaderText();

    protected abstract String getUsagesText();

    protected abstract void handleEnterOrDoubleClickActionOnSearchResultList(InputEvent event, T item);

    private Collection<T> lookup(String inputText, int resultLimit) {
        if (!inputText.isEmpty() && inputText.charAt(0) == '#') {
            String[] searchTerms = inputText.substring(1).split("[;；]");
            Collection<T> result = new ArrayList<>(searchTerms.length);
            for (String searchTerm : searchTerms) {
                lookupByCommands(searchTerm.strip(), result);
            }
            return result;
        }
        return lookupByKeywords(inputText, resultLimit);
    }

    protected abstract Collection<T> lookupByKeywords(String lookupText, int resultLimit);

    protected abstract void lookupByCommands(String searchTerm, Collection<T> result);
}
