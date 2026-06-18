package org.appxi.javafx.control;

import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class VBoxEx extends VBox implements AlignedBar {
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
