package org.appxi.javafx.control;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;

import java.util.function.Consumer;
import java.util.function.Function;

public class WebViewFinder extends HBox {
    public final TextField input;
    public final Button prev;
    public final Button next;

    public WebViewFinder(WebViewer webViewer, Function<String, String> inputConvertor) {
        super();
        setAlignment(Pos.CENTER);
        getStyleClass().add("web-view-finder");

        input = new TextField();
        input.setPromptText("Find in page");
        input.setTooltip(new Tooltip("输入即查找，Enter 查找下一个，Shift + Enter 查找上一个"));

        prev = new Button("<");
        prev.getStyleClass().add("prev");
        prev.setDisable(true);

        next = new Button(">");
        next.getStyleClass().add("next");
        next.setDisable(true);

        final Consumer<Boolean> searcher = forwardsOrElseBackwards -> {
            String inputText = input.getText();
            if (null != inputText && null != inputConvertor)
                inputText = inputConvertor.apply(inputText);
            if (null != inputText && !inputText.isBlank() && webViewer.findInPage(inputText, forwardsOrElseBackwards)) {
                prev.setDisable(false);
                next.setDisable(false);
            } else {
                prev.setDisable(true);
                next.setDisable(true);
            }
        };

        input.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                searcher.accept(!event.isShiftDown());
                event.consume();
            }
        });
        input.textProperty().addListener((o, ov, nv) -> searcher.accept(true));
        prev.setOnAction(event -> searcher.accept(false));
        next.setOnAction(event -> searcher.accept(true));

        getChildren().setAll(input, prev, next);
    }

    public void search(String text) {
        input.requestFocus();
        input.setText(text);
    }
}
