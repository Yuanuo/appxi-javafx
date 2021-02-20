package org.appxi.javafx.control;

import javafx.geometry.Pos;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;

public class BlockingView extends StackPane {
    public final MaskingPane masking;
    public final ProgressIndicator progressIndicator;

    public BlockingView() {
        super();
        getStyleClass().add("blocking-view");

        masking = new MaskingPane();

        progressIndicator = new ProgressIndicator();
        progressIndicator.setMinSize(200, 200);
        StackPane.setAlignment(progressIndicator, Pos.CENTER);

        getChildren().setAll(masking, progressIndicator);
    }
}