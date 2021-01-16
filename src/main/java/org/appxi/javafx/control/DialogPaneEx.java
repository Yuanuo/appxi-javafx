package org.appxi.javafx.control;

import javafx.scene.Node;
import javafx.scene.control.DialogPane;

public class DialogPaneEx extends DialogPane {
    public DialogPaneEx() {
        super();
    }

    @Override
    protected Node createButtonBar() {
        return null;
    }
}
