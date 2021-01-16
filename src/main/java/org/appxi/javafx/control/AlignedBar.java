package org.appxi.javafx.control;

import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.*;

import java.util.Arrays;

public class AlignedBar extends StackPane {
    private final Pane spaceFill1, spaceFill2;
    private final ToolBar bar;

    public AlignedBar() {
        this(Orientation.HORIZONTAL);
    }

    public AlignedBar(Orientation orientation) {
        super();
        this.getStyleClass().add("aligned-bar");

        this.spaceFill1 = new Pane();
        this.spaceFill1.getStyleClass().add("space-fill");

        this.spaceFill2 = new Pane();
        this.spaceFill2.getStyleClass().add("space-fill");

        this.bar = new ToolBar(this.spaceFill1, this.spaceFill2);
        this.bar.setOrientation(orientation);
        this.getChildren().add(this.bar);

        updateGrowsInfo(orientation);
    }

    public void setOrientation(Orientation orientation) {
        orientation = null == orientation ? Orientation.HORIZONTAL : orientation;
        if (this.bar.getOrientation() == orientation)
            return;
        this.bar.setOrientation(orientation);
        updateGrowsInfo(orientation);
    }

    private void updateGrowsInfo(Orientation orientation) {
        if (orientation == Orientation.HORIZONTAL) {
            HBox.setHgrow(spaceFill1, Priority.ALWAYS);
            HBox.setHgrow(spaceFill2, Priority.ALWAYS);
            VBox.setVgrow(spaceFill1, null);
            VBox.setVgrow(spaceFill2, null);
        } else {
            VBox.setVgrow(spaceFill1, Priority.ALWAYS);
            VBox.setVgrow(spaceFill2, Priority.ALWAYS);
            HBox.setHgrow(spaceFill1, null);
            HBox.setHgrow(spaceFill2, null);
        }
    }

    public ObservableList<Node> getAlignedItems() {
        return this.bar.getItems();
    }

    public AlignedBar addAligned(Pos pos, Node... nodes) {
        switch (pos) {
            case TOP_LEFT, CENTER_LEFT, BOTTOM_LEFT -> this.addLeft(nodes);
            case TOP_CENTER, CENTER, BOTTOM_CENTER -> this.addCenter(nodes);
            default -> this.addRight(nodes);
        }
        return this;
    }

    public AlignedBar addLeft(Node... nodes) {
        final ObservableList<Node> items = getAlignedItems();
        final int idx = items.indexOf(this.spaceFill1);
        items.addAll(idx == -1 ? 0 : idx, Arrays.asList(nodes));
        return this;
    }

    public AlignedBar addCenter(Node... nodes) {
        final ObservableList<Node> items = getAlignedItems();
        final int idx = items.indexOf(this.spaceFill2);
        items.addAll(idx == -1 ? 0 : idx, Arrays.asList(nodes));
        return this;
    }

    public AlignedBar addRight(Node... nodes) {
        getAlignedItems().addAll(nodes);
        return this;
    }
}
