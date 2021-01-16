package org.appxi.javafx.control;

import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AlignedBox extends StackPane {
    protected final Node spaceFill1, spaceFill2;

    private Pane box;
    private Orientation orientation;

    public AlignedBox() {
        this(Orientation.HORIZONTAL);
    }

    public AlignedBox(Orientation orientation) {
        super();
        this.getStyleClass().add("aligned-box");

        this.spaceFill1 = createSpaceFillNode();
        this.spaceFill1.getStyleClass().add("space-fill");

        this.spaceFill2 = createSpaceFillNode();
        this.spaceFill2.getStyleClass().add("space-fill");

        this.setOrientation(orientation);
    }

    protected Node createSpaceFillNode() {
        return new Pane();
    }

    public void setOrientation(Orientation orientation) {
        orientation = null == orientation ? Orientation.HORIZONTAL : orientation;
        if (this.orientation == orientation)
            return;
        this.orientation = orientation;

        final List<Node> items = new ArrayList<>();
        if (null != box) {
            items.addAll(getAlignedItems());
            this.getChildren().remove(box);
        } else {
            items.add(this.spaceFill1);
            items.add(this.spaceFill2);
        }

        if (orientation == Orientation.HORIZONTAL) {
            this.box = new HBox();
            this.box.getStyleClass().add("h-box");
            HBox.setHgrow(spaceFill1, Priority.ALWAYS);
            HBox.setHgrow(spaceFill2, Priority.ALWAYS);
            VBox.setVgrow(spaceFill1, null);
            VBox.setVgrow(spaceFill2, null);
        } else {
            this.box = new VBox();
            this.box.getStyleClass().add("v-box");
            VBox.setVgrow(spaceFill1, Priority.ALWAYS);
            VBox.setVgrow(spaceFill2, Priority.ALWAYS);
            HBox.setHgrow(spaceFill1, null);
            HBox.setHgrow(spaceFill2, null);
        }
        this.getAlignedItems().addAll(items);
        this.getChildren().add(box);
    }

    public AlignedBox setBoxAlignment(Pos pos) {
        if (this.box instanceof HBox hBox)
            hBox.setAlignment(pos);
        else if (this.box instanceof VBox vBox)
            vBox.setAlignment(pos);
        return this;
    }

    public ObservableList<Node> getAlignedItems() {
        return this.box.getChildren();
    }

    public <T> T getBox() {
        if (this.box instanceof HBox hBox)
            return (T) hBox;
        else if (this.box instanceof VBox vBox)
            return (T) vBox;
        return (T) this.box;
    }

    public AlignedBox addAligned(Pos pos, Node... nodes) {
        switch (pos) {
            case TOP_LEFT, CENTER_LEFT, BOTTOM_LEFT -> this.addLeft(nodes);
            case TOP_CENTER, CENTER, BOTTOM_CENTER -> this.addCenter(nodes);
            default -> this.addRight(nodes);
        }
        return this;
    }

    public AlignedBox addLeft(Node... nodes) {
        final ObservableList<Node> items = getAlignedItems();
        final int idx = items.indexOf(this.spaceFill1);
        items.addAll(idx == -1 ? 0 : idx, Arrays.asList(nodes));
        return this;
    }

    public AlignedBox addCenter(Node... nodes) {
        final ObservableList<Node> items = getAlignedItems();
        final int idx = items.indexOf(this.spaceFill2);
        items.addAll(idx == -1 ? 0 : idx, Arrays.asList(nodes));
        return this;
    }

    public AlignedBox addRight(Node... nodes) {
        getAlignedItems().addAll(nodes);
        return this;
    }
}
