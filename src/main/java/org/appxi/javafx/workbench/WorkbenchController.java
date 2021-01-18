package org.appxi.javafx.workbench;

import org.appxi.javafx.desktop.ApplicationEvent;
import org.appxi.javafx.views.ViewController;
import org.appxi.javafx.workbench.views.WorkbenchOpenpartController;
import org.appxi.javafx.workbench.views.WorkbenchViewpartController;
import org.appxi.javafx.workbench.views.WorkbenchWorkpartController;
import org.appxi.javafx.workbench.views.WorkbenchWorktoolController;
import org.appxi.prefs.UserPrefs;
import org.appxi.util.DevtoolHelper;

import java.util.List;

public abstract class WorkbenchController extends ViewController {
    private WorkbenchPaneEx viewport;

    public WorkbenchController(String viewId, String viewName) {
        super(viewId, viewName);
    }

    @Override
    public final WorkbenchPaneEx getViewport() {
        return null != this.viewport ? this.viewport : (this.viewport = new WorkbenchPaneEx());
    }

    @Override
    public void setupInitialize() {
        final long st0 = System.currentTimeMillis();

        getApplication().updateStartingProgress();
        this.getThemeProvider().applyTheme(UserPrefs.prefs.getString("ui.theme", "light"));

        getEventBus().addEventHandler(ApplicationEvent.STARTED, event -> {
            getViewport().setWorkviewsDividerPosition(UserPrefs.prefs.getDouble("workbench.views.divider", 0.2));
        });
        getEventBus().addEventHandler(ApplicationEvent.STOPPING, event -> {
            UserPrefs.prefs.setProperty("workbench.views.divider", getViewport().getWorkviewsDividerPosition());
            UserPrefs.prefs.setProperty("workbench.workviews.visible", getViewport().isWorkviewsVisible());
        });
        final List<WorkbenchViewpartController> viewControllers = createViewpartControllers();
        viewControllers.forEach(viewController -> {
            getApplication().updateStartingProgress();
            viewController.setupApplication(getApplication(), this);
        });
        viewControllers.forEach(viewController -> {
            getApplication().updateStartingProgress();
            this.addWorkbenchViewpartController(viewController);
        });
        viewControllers.forEach(controller -> {
            getApplication().updateStartingProgress();
            controller.setupInitialize();
        });
        getApplication().updateStartingProgress();

        getViewport().setWorkviewsDividerPosition(UserPrefs.prefs.getDouble("workbench.views.divider", 0.2));
        if (UserPrefs.prefs.getBoolean("workbench.workviews.visible", true))
            this.getViewport().selectWorktool(null);
        DevtoolHelper.LOG.info("load viewpart views used time: " + (System.currentTimeMillis() - st0));
    }

    public void addWorkbenchViewpartController(WorkbenchViewpartController controller) {
        if (null == controller.getWorkbenchController())
            controller.setupApplication(getApplication(), this);
        //
        if (controller instanceof WorkbenchWorkpartController controller1) {
            this.getViewport().addWorkpart(controller1);
        } else if (controller instanceof WorkbenchOpenpartController controller1) {
            this.getViewport().addOpenpart(controller1);
        } else if (controller instanceof WorkbenchWorktoolController controller1) {
            this.getViewport().addWorktool(controller1);
        }
    }

    protected abstract List<WorkbenchViewpartController> createViewpartControllers();
}
