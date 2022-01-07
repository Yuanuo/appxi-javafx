package org.appxi.javafx.control;

import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.Arrays;

public class ToolBarEx extends ToolBar {
    private final Pane spaceFill;

    public ToolBarEx() {
        this(Orientation.HORIZONTAL);
    }

    public ToolBarEx(Orientation orientation) {
        super();

        this.spaceFill = new Pane();
        this.spaceFill.getStyleClass().add("space-fill");

        this.setOrientation(orientation);
        this.getItems().setAll(this.spaceFill);

        updateGrowsInfo(orientation);
    }

    private void updateGrowsInfo(Orientation orientation) {
        if (orientation == Orientation.HORIZONTAL) {
            HBox.setHgrow(spaceFill, Priority.ALWAYS);
            VBox.setVgrow(spaceFill, null);
        } else {
            VBox.setVgrow(spaceFill, Priority.ALWAYS);
            HBox.setHgrow(spaceFill, null);
        }
    }

    public ObservableList<Node> getAlignedItems() {
        return this.getItems();
    }

    public ToolBarEx addAligned(Pos pos, Node... nodes) {
        switch (pos) {
            case TOP_LEFT, CENTER_LEFT, BOTTOM_LEFT -> this.addLeft(nodes);
            default -> this.addRight(nodes);
        }
        return this;
    }

    public ToolBarEx addLeft(Node... nodes) {
        final ObservableList<Node> items = getAlignedItems();
        final int idx = items.indexOf(this.spaceFill);
        items.addAll(idx == -1 ? 0 : idx, Arrays.asList(nodes));
        return this;
    }

    public ToolBarEx addRight(Node... nodes) {
        getAlignedItems().addAll(nodes);
        return this;
    }
}
