package org.appxi.javafx.workbench.views;

import org.appxi.javafx.workbench.WorkbenchPane;
import org.appxi.javafx.workbench.WorkbenchViewController;

public abstract class WorkbenchSideToolController extends WorkbenchViewController {
    public WorkbenchSideToolController(String viewId, WorkbenchPane workbench) {
        super(viewId, workbench);
    }

    @Override
    public final <T> T getViewport() {
        throw new UnsupportedOperationException("Not support");
    }
}
