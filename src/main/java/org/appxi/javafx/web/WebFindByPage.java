package org.appxi.javafx.web;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.function.Consumer;
import java.util.function.Function;

public class WebFindByPage extends WebFinder {
    private Function<String, String> inputConvertor;

    public WebFindByPage(WebPane webPane) {
        super();

        final Consumer<Boolean> finder = forwardsOrElseBackwards -> {
            String inputText = input.getText();
            if (null != inputText && null != inputConvertor)
                inputText = inputConvertor.apply(inputText);
            if (null != inputText && !inputText.isBlank() && webPane.findInPage(inputText, forwardsOrElseBackwards)) {
                prev.setDisable(false);
                next.setDisable(false);
                clear.setDisable(false);
            } else {
                prev.setDisable(true);
                next.setDisable(true);
                clear.setDisable(null == inputText || inputText.isEmpty());
            }
        };

        input.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            if (event.getCode() == KeyCode.ENTER) finder.accept(!event.isShiftDown());
        });
        input.textProperty().addListener((o, ov, nv) -> finder.accept(true));
        prev.setOnAction(event -> finder.accept(false));
        next.setOnAction(event -> finder.accept(true));
    }

    public final WebFindByPage setInputConvertor(Function<String, String> inputConvertor) {
        this.inputConvertor = inputConvertor;
        return this;
    }
}
