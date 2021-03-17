package org.appxi.javafx.workbench;

import org.appxi.javafx.views.ViewController;

public abstract class WorkbenchViewController extends ViewController {
    public WorkbenchViewController(String viewId, WorkbenchApplication application) {
        super(viewId, application);
    }

    public final WorkbenchApplication getApplication() {
        return (WorkbenchApplication) this.application;
    }

    public final WorkbenchPane getPrimaryViewport() {
        return this.getApplication().getPrimaryViewport();
    }

    public abstract void onViewportShowing(boolean firstTime);
}
