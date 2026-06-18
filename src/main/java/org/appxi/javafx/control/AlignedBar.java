package org.appxi.javafx.control;

import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;

import java.util.Arrays;

public interface AlignedBar {
    ObservableList<Node> getAlignedItems();

    Node spaceFill1();

    Node spaceFill2();

    default AlignedBar addAligned(Pos pos, Node... nodes) {
        switch (pos) {
            case TOP_LEFT, CENTER_LEFT, BOTTOM_LEFT -> this.addLeft(nodes);
            case TOP_CENTER, CENTER, BOTTOM_CENTER -> this.addCenter(nodes);
            default -> this.addRight(nodes);
        }
        return this;
    }

    default AlignedBar addLeft(Node... nodes) {
        return addLeft(-1, nodes);
    }

    default AlignedBar addLeft(int idx, Node... nodes) {
        final ObservableList<Node> items = this.getAlignedItems();
        final int maxIdx = items.indexOf(this.spaceFill1());
        idx = (idx < 0 || idx > maxIdx) ? maxIdx : idx;
        items.addAll(idx, Arrays.asList(nodes));
        return this;
    }

    default AlignedBar addCenter(Node... nodes) {
        return addCenter(-1, nodes);
    }

    default AlignedBar addCenter(int idx, Node... nodes) {
        final ObservableList<Node> items = this.getAlignedItems();
        final int minIdx = items.indexOf(this.spaceFill1());
        final int maxIdx = items.indexOf(this.spaceFill2());
        idx = (idx < 0 || idx > maxIdx) ? maxIdx : (minIdx + idx);
        idx = (idx < minIdx || idx > maxIdx) ? maxIdx : idx;
        items.addAll(idx, Arrays.asList(nodes));
        return this;
    }

    default AlignedBar addRight(Node... nodes) {
        return addRight(-1, nodes);
    }

    default AlignedBar addRight(int idx, Node... nodes) {
        final ObservableList<Node> items = this.getAlignedItems();
        final int minIdx = items.indexOf(this.spaceFill2());
        final int maxIdx = items.size();
        idx = (idx < 0 || idx > maxIdx) ? maxIdx : (minIdx + idx);
        idx = (idx < minIdx || idx > maxIdx) ? maxIdx : idx;
        items.addAll(idx, Arrays.asList(nodes));
        return this;
    }

    default <T extends Node> T findById(String id) {
        return (T) this.getAlignedItems().stream().filter(n -> id.equals(n.getId())).findFirst().orElse(null);
    }
}
