package org.appxi.javafx.workbench.views;

import org.appxi.javafx.workbench.WorkbenchApplication;
import org.appxi.javafx.workbench.WorkbenchViewController;

public abstract class WorkbenchNoneViewController extends WorkbenchViewController {
    public WorkbenchNoneViewController(String viewId, WorkbenchApplication application) {
        super(viewId, application);
    }

    @Override
    public final <T> T getViewport() {
        throw new UnsupportedOperationException("Not support");
    }
}
