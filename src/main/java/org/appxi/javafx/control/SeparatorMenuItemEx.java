package org.appxi.javafx.control;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.SeparatorMenuItem;

public class SeparatorMenuItemEx extends SeparatorMenuItem {

    /***************************************************************************
     *                                                                         *
     * Constructors                                                            *
     *                                                                         *
     **************************************************************************/

    public SeparatorMenuItemEx(String label) {
        this(new Label(label));
    }

    public SeparatorMenuItemEx(Label label) {
        this(label, Pos.CENTER);
    }

    public SeparatorMenuItemEx(Label label, Pos pos) {
        super();

        final AlignedSeparator separator = new AlignedSeparator();
        separator.setBoxAlignment(Pos.CENTER_LEFT);
        separator.addAligned(pos, label);
        separator.setPrefWidth(200);
        setContent(separator);
    }

    public final AlignedSeparator getAlignedSeparator() {
        return (super.getContent() instanceof AlignedSeparator sep) ? sep : null;
    }
}
