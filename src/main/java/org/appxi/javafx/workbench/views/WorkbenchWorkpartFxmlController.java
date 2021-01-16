package org.appxi.javafx.workbench.views;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import org.appxi.javafx.views.FxmlController;

public abstract class WorkbenchWorkpartFxmlController extends WorkbenchWorkpartController implements FxmlController {
    @FXML
    private Label fxmlViewpartLabel;
    @FXML
    private Node fxmlViewpartContent;

    public WorkbenchWorkpartFxmlController(String viewId, String viewName) {
        super(viewId, viewName);
    }

    public final Label getViewpartInfo() {
        return fxmlViewpartLabel;
    }

    public final Node getViewport() {
        return fxmlViewpartContent;
    }
}
