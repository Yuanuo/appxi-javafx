package org.appxi.javafx.workbench.views;

import org.appxi.javafx.control.WorkbenchPane;

public abstract class WorkbenchWorktoolController extends WorkbenchViewpartController implements WorkbenchPane.WorktoolListener {
    public WorkbenchWorktoolController(String viewId, String viewName) {
        super(viewId, viewName);
    }

    @Override
    public abstract void onViewportSelected(boolean firstTime);
}
