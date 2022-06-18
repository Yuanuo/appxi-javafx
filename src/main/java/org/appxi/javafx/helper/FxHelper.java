package org.appxi.javafx.helper;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Labeled;
import javafx.scene.control.SelectionModel;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.DataFormat;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.appxi.javafx.control.ListViewEx;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

public abstract class FxHelper {
    private FxHelper() {
    }

    public static void runLater(Runnable runnable) {
        if (Platform.isFxApplicationThread())
            runnable.run();
        else Platform.runLater(runnable);
    }

    public static void runThread(Runnable runnable) {
        new Thread(() -> FxHelper.runLater(runnable)).start();
    }

    public static void runThread(long afterMillis, Runnable runnable) {
        new Thread(() -> {
            sleepSilently(afterMillis);
            FxHelper.runLater(runnable);
        }).start();
    }

    public static void sleepSilently(long millis) {
        if (millis <= 0) return;
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void highlight(Labeled labeled, Set<String> keywords) {
        String text = labeled.getText();
        if (null == text) {
            text = "<TEXT-LABEL>";
            keywords = null;// skip
        }
        if (null != keywords && !keywords.isEmpty()) {
            List<String> lines = new ArrayList<>(List.of(text));
            for (String keyword : keywords) {
                for (int i = 0; i < lines.size(); i++) {
                    final String line = lines.get(i);
                    if (line.startsWith("§§#§§")) continue;

                    List<String> list = List.of(line
                            .replace(keyword, "\n§§#§§".concat(keyword).concat("\n"))
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

    public static <T> void connectTextFieldAndListView(TextField input, ListViewEx<T> listView) {
        input.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            switch (event.getCode()) {
                case UP -> {
                    SelectionModel<T> model = listView.getSelectionModel();
                    int selIdx = model.getSelectedIndex() - 1;
                    if (selIdx < 0)
                        selIdx = listView.getItems().size() - 1;
                    model.select(selIdx);
                    listView.scrollToIfNotVisible(selIdx);
                    event.consume();
                }
                case DOWN -> {
                    SelectionModel<T> model = listView.getSelectionModel();
                    int selIdx = model.getSelectedIndex() + 1;
                    if (selIdx >= listView.getItems().size())
                        selIdx = 0;
                    model.select(selIdx);
                    listView.scrollToIfNotVisible(selIdx);
                    event.consume();
                }
                case ENTER -> {
                    event.consume();
                    T item = listView.getSelectionModel().getSelectedItem();
                    if (null != item && null != listView.enterOrDoubleClickAction())
                        listView.enterOrDoubleClickAction().accept(event, item);
                }
            }
        });
    }

    public static Optional<Node> filterParent(Node node, String styleClass) {
        return filterParent(node, n -> n.getStyleClass().contains(styleClass));
    }

    public static Optional<Node> filterParent(Node node, Predicate<Node> predicate) {
        while (null != node) {
            if (predicate.test(node)) return Optional.of(node);
            node = node.getParent();
        }
        return Optional.empty();
    }

    /**
     * 复制文字到剪贴板
     *
     * @param text 要复制的文字
     */
    public static void copyText(String text) {
        // 必须用Platform.runLater，否则内容会被清除掉而复制失败
        Platform.runLater(() -> Clipboard.getSystemClipboard().setContent(Map.of(DataFormat.PLAIN_TEXT, text)));
    }
}
