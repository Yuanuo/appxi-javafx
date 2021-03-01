package org.appxi.javafx.workbench.views;

import org.appxi.javafx.workbench.WorkbenchApplication;
import org.appxi.javafx.workbench.WorkbenchViewController;
import org.appxi.javafx.workbench.WorkbenchViewLocation;

public abstract class WorkbenchSideToolController extends WorkbenchViewController {
    public WorkbenchSideToolController(String viewId, String viewName, WorkbenchApplication application) {
        super(viewId, viewName, application);
    }

    @Override
    public final WorkbenchViewLocation getWorkbenchViewLocation() {
        return WorkbenchViewLocation.sideTool;
    }

    @Override
    public final <T> T getViewport() {
        return null;
    }

    @Override
    public void setupInitialize() {
    }

    @Override
    public final void onViewportHide(boolean hideOrElseClose) {
        // do nothing, should never called
    }
}
