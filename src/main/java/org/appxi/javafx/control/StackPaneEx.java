package org.appxi.javafx.control;

import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StackPaneEx extends StackPane {
    public final Pane masking = new Pane();

    public StackPaneEx() {
        super();
        this.masking.getStyleClass().add("masking");
//        final ColorAdjust overlayEffects = new ColorAdjust();
//        overlayEffects.setInput(new BoxBlur());
//        overlayEffects.setBrightness(-0.5);
//        masking.setEffect(overlayEffects);
//        masking.setStyle("-fx-background-color: grey;-fx-opacity: 0.5;");
    }

    public StackPaneEx(Node... children) {
        super(children);
    }

    public void showMasking(Node... nodes) {
        final List<Node> list = new ArrayList<>(nodes.length + 1);
        list.add(this.masking);
        list.addAll(Arrays.asList(nodes));
        if (!getChildren().containsAll(list))
            getChildren().addAll(list);
    }

    public void hideMasking(Node... nodes) {
        final List<Node> list = new ArrayList<>(nodes.length + 1);
        list.add(this.masking);
        list.addAll(Arrays.asList(nodes));
        getChildren().removeAll(list);
    }

    public void removeMasking() {
        final List<Node> items = getChildren();
        final int overlayIdx = items.indexOf(this.masking);
        if (overlayIdx != -1) {
            items.removeAll(items.subList(overlayIdx, items.size()));
        }
    }

    public void show(Node... nodes) {
        final List<Node> list = new ArrayList<>(Arrays.asList(nodes));
        if (!getChildren().containsAll(list))
            getChildren().addAll(list);
    }

    public void hide(Node... nodes) {
        final List<Node> list = new ArrayList<>(Arrays.asList(nodes));
        getChildren().removeAll(list);
    }
}
