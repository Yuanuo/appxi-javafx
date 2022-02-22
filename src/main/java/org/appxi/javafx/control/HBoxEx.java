package org.appxi.javafx.control;

import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;

import java.util.Arrays;

public class HBoxEx extends HBox {
    protected final Node spaceFill1, spaceFill2;

    public HBoxEx() {
        super();
        this.getStyleClass().addAll("aligned-box", "h-box");
        this.setAlignment(Pos.CENTER_LEFT);

        this.spaceFill1 = new Pane();
        this.spaceFill1.getStyleClass().add("space-fill");
        HBox.setHgrow(spaceFill1, Priority.SOMETIMES);

        this.spaceFill2 = new Pane();
        this.spaceFill2.getStyleClass().add("space-fill");
        HBox.setHgrow(spaceFill2, Priority.SOMETIMES);

        this.getChildren().setAll(spaceFill1, spaceFill2);
    }

    public HBoxEx addAligned(Pos pos, Node... nodes) {
        switch (pos) {
            case TOP_LEFT, CENTER_LEFT, BOTTOM_LEFT -> this.addLeft(nodes);
            case TOP_CENTER, CENTER, BOTTOM_CENTER -> this.addCenter(nodes);
            default -> this.addRight(nodes);
        }
        return this;
    }

    public HBoxEx addLeft(Node... nodes) {
        final ObservableList<Node> items = getChildren();
        final int idx = items.indexOf(this.spaceFill1);
        items.addAll(idx == -1 ? 0 : idx, Arrays.asList(nodes));
        return this;
    }

    public HBoxEx addCenter(Node... nodes) {
        final ObservableList<Node> items = getChildren();
        final int idx = items.indexOf(this.spaceFill2);
        items.addAll(idx == -1 ? 0 : idx, Arrays.asList(nodes));
        return this;
    }

    public HBoxEx addRight(Node... nodes) {
        getChildren().addAll(nodes);
        return this;
    }
}
