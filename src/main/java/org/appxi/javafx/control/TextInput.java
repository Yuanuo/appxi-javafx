package org.appxi.javafx.control;

import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public class TextInput extends HBox {
    public final TextField input;
    public final HBox leftArea, rightArea;

    public TextInput() {
        this.input = new TextField();
        HBox.setHgrow(input, Priority.ALWAYS);

        this.leftArea = new HBox(5);
        this.leftArea.getStyleClass().add("like-text-input");
        this.leftArea.setAlignment(Pos.CENTER_LEFT);

        this.rightArea = new HBox(5);
        this.rightArea.getStyleClass().add("like-text-input");
        this.rightArea.setAlignment(Pos.CENTER_LEFT);

        input.focusedProperty().addListener((o, ov, nv) -> {
            if (nv) {
                leftArea.getStyleClass().remove("like-text-input");
                leftArea.getStyleClass().add("like-text-input-focused");
                rightArea.getStyleClass().remove("like-text-input");
                rightArea.getStyleClass().add("like-text-input-focused");
            } else {
                leftArea.getStyleClass().remove("like-text-input-focused");
                leftArea.getStyleClass().add("like-text-input");
                rightArea.getStyleClass().remove("like-text-input-focused");
                rightArea.getStyleClass().add("like-text-input");
            }
        });

        this.setAlignment(Pos.BOTTOM_CENTER);
        this.getChildren().setAll(leftArea, input, rightArea);
    }
}
