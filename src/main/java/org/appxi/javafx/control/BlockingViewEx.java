package org.appxi.javafx.control;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class BlockingViewEx extends BlockingView {
    public final Label blockingMessage;
    public final Label progressMessage;

    public BlockingViewEx(String blockMessage) {
        super();

        final VBox vBox = new VBox();
        vBox.getStyleClass().add("v-box");
        vBox.setAlignment(Pos.CENTER);
        vBox.setSpacing(50);
        vBox.setStyle(getStyle().concat("-fx-background-color:-fx-background;"));

        blockingMessage = new Label(blockMessage);
        blockingMessage.setWrapText(true);

        progressMessage = new Label();

        vBox.getChildren().setAll(blockingMessage, progressIndicator, progressMessage);

        getChildren().setAll(masking, vBox);
    }
}
