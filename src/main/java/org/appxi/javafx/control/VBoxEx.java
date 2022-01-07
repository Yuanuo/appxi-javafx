package org.appxi.javafx.control;

import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.Arrays;

public class VBoxEx extends VBox {
    protected final Node spaceFill1, spaceFill2;

    public VBoxEx() {
        super();
        this.getStyleClass().addAll("aligned-box", "v-box");
        this.setAlignment(Pos.CENTER_LEFT);

        this.spaceFill1 = new Pane();
        this.spaceFill1.getStyleClass().add("space-fill");
        VBox.setVgrow(spaceFill1, Priority.ALWAYS);

        this.spaceFill2 = new Pane();
        this.spaceFill2.getStyleClass().add("space-fill");
        VBox.setVgrow(spaceFill2, Priority.ALWAYS);

        this.getChildren().setAll(spaceFill1, spaceFill2);
    }

    public VBoxEx addAligned(Pos pos, Node... nodes) {
        switch (pos) {
            case TOP_LEFT, CENTER_LEFT, BOTTOM_LEFT -> this.addLeft(nodes);
            case TOP_CENTER, CENTER, BOTTOM_CENTER -> this.addCenter(nodes);
            default -> this.addRight(nodes);
        }
        return this;
    }

    public VBoxEx addLeft(Node... nodes) {
        final ObservableList<Node> items = getChildren();
        final int idx = items.indexOf(this.spaceFill1);
        items.addAll(idx == -1 ? 0 : idx, Arrays.asList(nodes));
        return this;
    }

    public VBoxEx addCenter(Node... nodes) {
        final ObservableList<Node> items = getChildren();
        final int idx = items.indexOf(this.spaceFill2);
        items.addAll(idx == -1 ? 0 : idx, Arrays.asList(nodes));
        return this;
    }

    public VBoxEx addRight(Node... nodes) {
        getChildren().addAll(nodes);
        return this;
    }
}
