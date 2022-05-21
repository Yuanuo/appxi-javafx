package org.appxi.javafx.control;

import javafx.collections.ObservableList;
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
        if (!glassPane.getChildren().contains(this)) {
            hideOpaqueLayer(glassPane);
            glassPane.getChildren().add(this);
        }
    }

    public void hide() {
        if (this.getParent() instanceof StackPane glassPane)
            glassPane.getChildren().remove(this);
    }

    public static void hideOpaqueLayer(StackPane glassPane) {
        ObservableList<Node> nodes = glassPane.getChildren();
        if (!nodes.isEmpty() && nodes.get(nodes.size() - 1) instanceof OpaqueLayer opaqueLayer) {
            nodes.remove(opaqueLayer);
        }
    }
}
