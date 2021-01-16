package org.appxi.javafx.workbench.views;

import javafx.scene.Node;
import org.appxi.javafx.control.WorkbenchPane;

public abstract class WorkbenchWorkpartController extends WorkbenchViewpartController implements WorkbenchPane.WorkpartListener {
    public WorkbenchWorkpartController(String viewId, String viewName) {
        super(viewId, viewName);
    }

    @Override
    public abstract Node getViewport();
}
