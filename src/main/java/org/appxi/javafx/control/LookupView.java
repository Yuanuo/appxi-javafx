package org.appxi.javafx.control;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.appxi.util.StringHelper;

import java.util.*;
import java.util.stream.Stream;

public abstract class LookupView {
    private final StackPane owner;

    private boolean showing;
    private MaskingPane masking;
    private DialogPaneEx dialogPane;
    private Label searchInfo;
    private TextField searchInput;
    private ListViewExt<Object> searchResult;

    private boolean searching;
    private String searchedText;
    private LookupRequest lookupRequest;

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
        final Collection<Object> lookupResult = lookup(inputText, resultLimit);
        // 允许具体实现中无必要进行新搜索时返回null以保持现状
        if (null != lookupResult)
            searchResult.getItems().setAll(lookupResult);
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
                if (event.getCode() == KeyCode.UP) {
                    SelectionModel<Object> model = searchResult.getSelectionModel();
                    int selIdx = model.getSelectedIndex() - 1;
                    if (selIdx < 0)
                        selIdx = searchResult.getItems().size() - 1;
                    model.select(selIdx);
                    searchResult.scrollToIfNotVisible(selIdx);
                    event.consume();
                } else if (event.getCode() == KeyCode.DOWN) {
                    SelectionModel<Object> model = searchResult.getSelectionModel();
                    int selIdx = model.getSelectedIndex() + 1;
                    if (selIdx >= searchResult.getItems().size())
                        selIdx = 0;
                    model.select(selIdx);
                    searchResult.scrollToIfNotVisible(selIdx);
                    event.consume();
                } else if (event.getCode() == KeyCode.ENTER) {
                    event.consume();
                    Object item = searchResult.getSelectionModel().getSelectedItem();
                    if (null != item)
                        handleEnterOrDoubleClickActionOnSearchResultList(event, item);
                } else if (event.getCode() == KeyCode.QUOTE && event.isShiftDown()) {
                    String selectedText = searchInput.getSelectedText();
                    if (null != selectedText && !selectedText.isEmpty()) {
                        event.consume();
                        selectedText = "\"".concat(selectedText);
                        searchInput.replaceSelection(selectedText);
                    }
                }
            });
            searchResult = new ListViewExt<>(this::handleEnterOrDoubleClickActionOnSearchResultList);
            VBox.setVgrow(searchResult, Priority.ALWAYS);
            searchResult.setFocusTraversable(false);
            searchResult.setCellFactory(v -> new ListCell<>() {
                Object updatedItem;

                @Override
                protected void updateItem(Object item, boolean empty) {
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
                    updateItemOnce(this, item);
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
        return 100;
    }

    protected void updateItemOnce(Labeled labeled, Object item) {
        String text = labeled.getText();
        if (null == text) text = null == item ? "<TEXT>" : item.toString();
        //
        if (null != lookupRequest && !lookupRequest.keywords.isEmpty()) {
            List<String> lines = new ArrayList<>(List.of(text));
            for (LookupKeyword keyword : lookupRequest.keywords()) {
                for (int i = 0; i < lines.size(); i++) {
                    final String line = lines.get(i);
                    if (line.startsWith("§§#§§")) continue;

                    List<String> list = List.of(line
                            .replace(keyword.text, "\n§§#§§".concat(keyword.text).concat("\n"))
                            .split("\n"));
                    if (list.size() > 1) {
                        lines.remove(i);
                        lines.addAll(i, list);
                        i++;
                    }
                }
            }
            List<Text> texts = new ArrayList<>(lines.size());
            for (String line : lines) {
                if (line.startsWith("§§#§§")) {
                    Text text1 = new Text(line.substring(5));
                    text1.getStyleClass().add("highlight");
                    texts.add(text1);
                } else {
                    final Text text1 = new Text(line);
                    text1.getStyleClass().add("plaintext");
                    texts.add(text1);
                }
            }
            TextFlow textFlow = new TextFlow(texts.toArray(new Node[0]));
            textFlow.getStyleClass().add("text-flow");
            labeled.setText(text);
            labeled.setGraphic(textFlow);
            labeled.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        } else {
            labeled.setText(text);
            labeled.setContentDisplay(ContentDisplay.TEXT_ONLY);
        }
    }

    protected abstract String getHeaderText();

    protected abstract String getUsagesText();

    protected abstract void handleEnterOrDoubleClickActionOnSearchResultList(InputEvent event, Object item);

    private Collection<Object> lookup(String inputText, int resultLimit) {
        if (!inputText.isEmpty() && inputText.matches("[(（“\"]")) return null;
        if (!inputText.isEmpty() && inputText.charAt(0) == '#') {
            String[] searchTerms = inputText.substring(1).split("[;；]");
            Collection<Object> result = new ArrayList<>(searchTerms.length);
            for (String searchTerm : searchTerms) {
                lookupByCommands(searchTerm.strip(), result);
            }
            return result;
        }
        // 如果此时的输入并无必要进行搜索，允许子类实现中返回null以中断并保持现状
        inputText = prepareLookupText(inputText);
        if (null == inputText) return null;

        lookupRequest = new LookupRequest(
                Stream.of(StringHelper.split(inputText, " ", "[()（）“”\"]"))
                        .map(str -> str.replaceAll("[()（）“”\"]", "")
                                .replaceAll("\s+", " ").toLowerCase().strip())
                        .filter(str -> !str.isEmpty())
                        .map(str -> new LookupKeyword(str, str.matches("[a-zA-Z0-9\s]+")))
                        .toList(),
                inputText, resultLimit);
        List<LookupResultItem> result = lookupByKeywords(lookupRequest);
        if (!inputText.isEmpty()) {
            // 默认时无输入，不需对结果进行排序
            result.sort(Comparator.<LookupResultItem>comparingDouble(v -> v.score).reversed());
            if (result.size() > resultLimit + 1)
                result = result.subList(0, resultLimit + 1);
        }
        return result.stream().map(v -> v.data).toList();
    }

    protected String prepareLookupText(String lookupText) {
        return lookupText;
    }

    protected abstract List<LookupResultItem> lookupByKeywords(LookupRequest lookupRequest);

    protected abstract void lookupByCommands(String searchTerm, Collection<Object> result);

    public record LookupKeyword(String text, boolean isFullAscii) {
    }

    public record LookupRequest(List<LookupKeyword> keywords, String text, int resultLimit) {
    }

    public record LookupResultItem(Object data, double score) {
    }
}
