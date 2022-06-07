package org.appxi.javafx.web;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import org.appxi.javafx.helper.FxHelper;
import org.appxi.javafx.visual.MaterialIcon;

public class WebFinder extends HBox {
    public final TextField input;
    public final Label info;
    public final Button prev, next, clear;

    public WebFinder() {
        super();
        setAlignment(Pos.CENTER);
        getStyleClass().add("web-finder");

        info = new Label();
        info.getStyleClass().add("info");

        prev = MaterialIcon.ARROW_UPWARD.flatButton();
        prev.setTooltip(new Tooltip("上一个"));
        prev.setDisable(true);

        next = MaterialIcon.ARROW_DOWNWARD.flatButton();
        next.setTooltip(new Tooltip("下一个"));
        next.setDisable(true);

        input = new TextField();
        input.setStyle("-fx-alignment: center; -fx-pref-width: 10em; -fx-min-width: 6em;");
        input.setPromptText("页内查找");
        input.setTooltip(new Tooltip("输入即查找，Enter 查找下一个，Shift + Enter 查找上一个"));
        input.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                if (event.isShiftDown()) prev.fire();
                else next.fire();
            }
        });

        clear = MaterialIcon.CLEAR.flatButton();
        clear.setTooltip(new Tooltip("清除"));
        clear.setDisable(true);

        this.getChildren().setAll(input, info, prev, next, clear);
    }

    public void find(String text) {
        FxHelper.runThread(30, input::requestFocus);
        input.setText(text);
    }

    public void state(int index, int count) {
        prev.setDisable(index <= 0);
        next.setDisable(index <= 0);
        clear.setDisable(index == 0);

        if (index == 0) info.setText(null);
        else if (index < 0) info.setText("0");
        else info.setText(" %d/%d ".formatted(index, count));
    }
}
