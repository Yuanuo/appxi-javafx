package org.appxi.javafx.workbench.views;

import org.appxi.javafx.workbench.WorkbenchApplication;
import org.appxi.javafx.workbench.WorkbenchViewController;

public abstract class WorkbenchSideToolController extends WorkbenchViewController {
    public WorkbenchSideToolController(String viewId, String viewName, WorkbenchApplication application) {
        super(viewId, viewName, application);
    }

    @Override
    public final Boolean isPlaceInSideViews() {
        return null;
    }

    @Override
    public final Object getViewport() {
        return null;
    }

    @Override
    public void setupInitialize() {
    }

    @Override
    public final void hideViewport(boolean hideOrElseClose) {
        // do nothing, should never called
    }
}
