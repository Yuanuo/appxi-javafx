package org.appxi.javafx.control;

import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Separator;

public class AlignedSeparator extends AlignedBox {
    public AlignedSeparator() {
        this(Orientation.HORIZONTAL);
    }

    public AlignedSeparator(Orientation orientation) {
        super(orientation);
        this.getStyleClass().add("aligned-separator");
    }

    @Override
    protected Node createSpaceFillNode() {
        return new Separator();
    }

    public void setOrientation(Orientation orientation) {
        orientation = null == orientation ? Orientation.HORIZONTAL : orientation;

        ((Separator) this.spaceFill1).setOrientation(orientation);
        ((Separator) this.spaceFill2).setOrientation(orientation);

        super.setOrientation(orientation);
    }

    public AlignedSeparator addAligned(Pos pos, Node... nodes) {
        return (AlignedSeparator) super.addAligned(pos, nodes);
    }

    public AlignedSeparator addLeft(Node... nodes) {
        return (AlignedSeparator) super.addLeft(nodes);
    }

    public AlignedSeparator addCenter(Node... nodes) {
        return (AlignedSeparator) super.addCenter(nodes);
    }

    public AlignedSeparator addRight(Node... nodes) {
        return (AlignedSeparator) super.addRight(nodes);
    }
}
