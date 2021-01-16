package org.appxi.javafx.workbench.views;

import javafx.scene.Node;
import org.appxi.javafx.control.WorkbenchPane;

public abstract class WorkbenchOpenpartController extends WorkbenchViewpartController implements WorkbenchPane.OpenpartListener {
    public WorkbenchOpenpartController(String viewId, String viewName) {
        super(viewId, viewName);
    }

    @Override
    public abstract Node getViewport();

    public boolean isWorktoolSupport() {
        return false;
    }
}
