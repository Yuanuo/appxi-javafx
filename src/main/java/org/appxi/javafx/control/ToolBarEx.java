package org.appxi.javafx.control;

import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class ToolBarEx extends ToolBar implements AlignedBar {
    private final Pane spaceFill1, spaceFill2;

    public ToolBarEx() {
        this(Orientation.HORIZONTAL);
    }

    public ToolBarEx(Orientation orientation) {
        super();

        this.spaceFill1 = new Pane();
        this.spaceFill1.setId("spaceFill1");
        this.spaceFill1.getStyleClass().add("space-fill");

        this.spaceFill2 = new Pane();
        this.spaceFill2.setId("spaceFill2");
        this.spaceFill2.getStyleClass().add("space-fill");

        this.setOrientation(orientation);
        this.getItems().setAll(this.spaceFill1, this.spaceFill2);

        updateGrowsInfo(orientation);
    }

    private void updateGrowsInfo(Orientation orientation) {
        if (orientation == Orientation.HORIZONTAL) {
            HBox.setHgrow(spaceFill1, Priority.ALWAYS);
            VBox.setVgrow(spaceFill1, null);
            HBox.setHgrow(spaceFill2, Priority.ALWAYS);
            VBox.setVgrow(spaceFill2, null);
        } else {
            VBox.setVgrow(spaceFill1, Priority.ALWAYS);
            HBox.setHgrow(spaceFill1, null);
            VBox.setVgrow(spaceFill2, Priority.ALWAYS);
            HBox.setHgrow(spaceFill2, null);
        }
    }

    @Override
    public ObservableList<Node> getAlignedItems() {
        return this.getItems();
    }

    @Override
    public Node spaceFill1() {
        return this.spaceFill1;
    }

    @Override
    public Node spaceFill2() {
        return this.spaceFill2;
    }
}
