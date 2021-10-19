package org.appxi.javafx.control;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.appxi.util.StringHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public abstract class LookupView<T> {
    private final StackPane owner;

    private boolean showing;
    private MaskingPane masking;
    private DialogPaneEx dialogPane;
    private Label searchInfo;
    private TextField searchInput;
    private ListViewExt<T> searchResult;

    private boolean searching;
    private String searchedText;

    public LookupView(StackPane owner) {
        this.owner = owner;
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
        if (Objects.equals(this.searchedText, inputText))
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
        if (!showing)
            return;
        showing = false;
        owner.getChildren().removeAll(masking, dialogPane);
    }

    public void show(String searchText) {
        if (null == dialogPane) {
            final EventHandler<Event> handleEventToHide = evt -> {
                boolean handled = false;
                if (evt instanceof KeyEvent event) {
                    handled = showing && event.getCode() == KeyCode.ESCAPE;
                } else if (evt instanceof MouseEvent event) {
                    handled = showing && event.getButton() == MouseButton.PRIMARY;
                } else if (evt instanceof ActionEvent) {
                    handled = showing;
                }
                if (handled) {
                    if (searching)
                        searching = false;
                    else hide();
                    evt.consume();
                }
            };

            masking = new MaskingPane();
            masking.addEventHandler(KeyEvent.KEY_PRESSED, handleEventToHide);
            masking.addEventHandler(MouseEvent.MOUSE_PRESSED, handleEventToHide);

            dialogPane = new DialogPaneEx();
            dialogPane.getStyleClass().add("lookup-view");
            StackPane.setAlignment(dialogPane, Pos.TOP_CENTER);
            dialogPane.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
            dialogPane.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
            dialogPane.addEventHandler(KeyEvent.KEY_PRESSED, handleEventToHide);

            //
            final AlignedBox headBox = new AlignedBox();

            final Label labelHeader = new Label(getHeaderText());
            labelHeader.setStyle("-fx-font-weight: bold;");
            headBox.getStyleClass().add("tool-bar");
            headBox.addLeft(labelHeader);

            searchInfo = new Label("请输入...");
            headBox.addRight(searchInfo);

            final Label labelUsages = new Label(getUsagesText());
            labelUsages.setWrapText(true);
            final AlignedBox helpBox = new AlignedBox();
            helpBox.getStyleClass().add("tool-bar");
            helpBox.addLeft(labelUsages);

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
            searchInput.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
                switch (event.getCode()) {
                    case UP -> {
                        SelectionModel<T> model = searchResult.getSelectionModel();
                        int selIdx = model.getSelectedIndex() - 1;
                        if (selIdx < 0)
                            selIdx = searchResult.getItems().size() - 1;
                        model.select(selIdx);
                        searchResult.scrollToIfNotVisible(selIdx);
                        event.consume();
                    }
                    case DOWN -> {
                        SelectionModel<T> model = searchResult.getSelectionModel();
                        int selIdx = model.getSelectedIndex() + 1;
                        if (selIdx >= searchResult.getItems().size())
                            selIdx = 0;
                        model.select(selIdx);
                        searchResult.scrollToIfNotVisible(selIdx);
                        event.consume();
                    }
                    case ENTER -> {
                        event.consume();
                        T item = searchResult.getSelectionModel().getSelectedItem();
                        if (null != item)
                            handleEnterOrDoubleClickActionOnSearchResultList(event, item);
                    }
                }
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
            searchResult = new ListViewExt<>(this::handleEnterOrDoubleClickActionOnSearchResultList);
            VBox.setVgrow(searchResult, Priority.ALWAYS);
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

            dialogPane.setContent(new VBox(headBox, helpBox, searchInput, searchResult));
        }
        if (!showing) {
            showing = true;
            dialogPane.setPrefSize(getPrefWidth(), getPrefHeight());
            owner.getChildren().addAll(masking, dialogPane);
        }
        searchInput.requestFocus();
        search(null != searchText ? searchText : this.searchedText);
        searchInput.selectAll();
    }

    protected int getPrefWidth() {
        return 1080;
    }

    protected int getPrefHeight() {
        return 640;
    }

    protected int getResultLimit() {
        return 300;
    }

    protected abstract void updateItemLabel(Labeled labeled, T item);

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
