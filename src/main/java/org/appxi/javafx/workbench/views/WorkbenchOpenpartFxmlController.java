package org.appxi.javafx.workbench.views;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import org.appxi.javafx.views.FxmlController;

public abstract class WorkbenchOpenpartFxmlController extends WorkbenchOpenpartController implements FxmlController {
    @FXML
    private Label fxmlViewpartLabel;
    @FXML
    private Node fxmlViewpartContent;

    public WorkbenchOpenpartFxmlController(String viewId, String viewName) {
        super(viewId, viewName);
    }

    public final Label getViewpartInfo() {
        return fxmlViewpartLabel;
    }

    public final Node getViewport() {
        return fxmlViewpartContent;
    }
}
