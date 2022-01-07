package org.appxi.javafx.control;

import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

public class OpaqueLayer extends BorderPane {
    public OpaqueLayer() {
        this(null);
    }

    public OpaqueLayer(Node centerNode) {
        super(centerNode);
        this.getStyleClass().add("opaque-layer");
        this.setStyle("-fx-background-color: rgba(50, 50, 50, 0.75);");
    }

    public final boolean isShowing() {
        return this.getParent() instanceof StackPane;
    }

    public void show(StackPane glassPane) {
        if (!glassPane.getChildren().contains(this))
            glassPane.getChildren().add(this);
    }

    public void hide() {
        if (this.getParent() instanceof StackPane glassPane)
            glassPane.getChildren().remove(this);
    }
}
