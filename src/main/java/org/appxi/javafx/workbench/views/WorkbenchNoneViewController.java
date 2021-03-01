package org.appxi.javafx.workbench.views;

import javafx.scene.Node;
import org.appxi.javafx.workbench.WorkbenchApplication;
import org.appxi.javafx.workbench.WorkbenchViewController;
import org.appxi.javafx.workbench.WorkbenchViewLocation;

public abstract class WorkbenchNoneViewController extends WorkbenchViewController {
    public WorkbenchNoneViewController(String viewId, String viewName, WorkbenchApplication application) {
        super(viewId, viewName, application);
    }

    @Override
    public final WorkbenchViewLocation getWorkbenchViewLocation() {
        return null;
    }

    @Override
    public final Node createToolIconGraphic(boolean sideToolOrElseViewTool) {
        return null;
    }

    @Override
    public final <T> T getViewport() {
        return null;
    }

    @Override
    public final void onViewportHide(boolean hideOrElseClose) {
    }
}
