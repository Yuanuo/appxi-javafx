package org.appxi.javafx.workbench.views;

import javafx.scene.control.Label;
import org.appxi.javafx.desktop.DesktopApplication;
import org.appxi.javafx.views.ViewController;
import org.appxi.javafx.workbench.WorkbenchController;
import org.appxi.javafx.workbench.WorkbenchPaneEx;

public abstract class WorkbenchViewpartController extends ViewController {
    private WorkbenchController workbenchController;

    public WorkbenchViewpartController(String viewId, String viewName) {
        super(viewId, viewName);
    }

    @Override
    public final void setupApplication(DesktopApplication application) {
        super.setupApplication(application);
    }

    public void setupApplication(DesktopApplication application, WorkbenchController workbenchController) {
        this.setupApplication(application);
        this.workbenchController = workbenchController;
    }

    // for subclass access
    public final WorkbenchController getWorkbenchController() {
        return workbenchController;
    }

    public final WorkbenchPaneEx getWorkbenchViewport() {
        return workbenchController.getViewport();
    }

    // sub viewpart should impl the basic methods
    public abstract Label getViewpartInfo();
}
