package org.appxi.javafx.control;

import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;

public class HBoxEx extends HBox implements AlignedBar {
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

    @Override
    public ObservableList<Node> getAlignedItems() {
        return this.getChildren();
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
