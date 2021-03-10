package org.appxi.javafx.workbench;

import javafx.scene.Node;
import org.appxi.javafx.views.ViewController;

public abstract class WorkbenchViewController extends ViewController {
    public WorkbenchViewController(String viewId, String viewName, WorkbenchApplication application) {
        super(viewId, viewName, application);
    }

    @Override
    public final WorkbenchApplication getApplication() {
        return (WorkbenchApplication) this.application;
    }

    @Override
    public final WorkbenchPane getPrimaryViewport() {
        return this.getApplication().getPrimaryViewport();
    }

    public abstract WorkbenchViewLocation getWorkbenchViewLocation();

    public abstract Node createToolIconGraphic(boolean sideToolOrElseViewTool);

    public String createToolTooltipText() {
        return this.viewName;
    }

    public abstract void onViewportShow(boolean firstTime);

    public abstract void onViewportHide(boolean hideOrElseClose);
}
