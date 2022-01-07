package org.appxi.javafx.control;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.appxi.javafx.helper.FxHelper;

import java.util.function.Consumer;

public class ProgressLayer extends VBox {
    public final OpaqueLayer opaqueLayer;
    public final Label header, message;
    public final ProgressIndicator indicator;

    public ProgressLayer() {
        super(50);
        this.getStyleClass().add("progress-layer");
        this.setAlignment(Pos.CENTER);

        this.header = new Label();
        this.header.getStyleClass().addAll("header", "font-bold");
        this.header.setStyle("-fx-font-size: 150%;");

        this.indicator = new ProgressIndicator();
        this.indicator.setStyle("-fx-min-width: 12.5em; -fx-min-height: 12.5em;");

        this.message = new Label();
        this.message.getStyleClass().addAll("message");
        this.message.setWrapText(true);

        this.getChildren().setAll(this.header, this.indicator, this.message);
        this.opaqueLayer = new OpaqueLayer(this);
    }

    public final boolean isShowing() {
        return this.opaqueLayer.isShowing();
    }

    public final void show(StackPane stackPane) {
        if (this.opaqueLayer.getCenter() != this)
            this.opaqueLayer.setCenter(this);
        this.opaqueLayer.show(stackPane);
    }

    public final void hide() {
        this.opaqueLayer.hide();
    }


    /////////////////
    public static void showAndWait(StackPane stackPane, Consumer<ProgressLayer> consumer) {
        final ProgressLayer progressLayer = new ProgressLayer();
        FxHelper.runLater(() -> progressLayer.show(stackPane));
        new Thread(() -> {
            try {
                consumer.accept(progressLayer);
            } finally {
                FxHelper.runLater(progressLayer::hide);
            }
        }).start();
    }

    public static Runnable show(StackPane stackPane, Consumer<ProgressLayer> consumer) {
        final ProgressLayer progressLayer = new ProgressLayer();
        FxHelper.runLater(() -> progressLayer.show(stackPane));
        new Thread(() -> consumer.accept(progressLayer)).start();
        return () -> FxHelper.runLater(progressLayer::hide);
    }

    public static Runnable showIndicator(StackPane stackPane) {
        final ProgressLayer progressLayer = new ProgressLayer();
        FxHelper.runLater(() -> progressLayer.show(stackPane));
        return () -> FxHelper.runLater(progressLayer::hide);
    }
}
