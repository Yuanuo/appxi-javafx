package org.appxi.javafx.workbench;

import org.appxi.javafx.desktop.ApplicationEvent;
import org.appxi.javafx.views.ViewController;
import org.appxi.prefs.UserPrefs;
import org.appxi.util.DevtoolHelper;

import java.util.List;

public abstract class WorkbenchPrimaryController extends ViewController {
    private WorkbenchPane viewport;

    public WorkbenchPrimaryController(String viewId, String viewName, WorkbenchApplication application) {
        super(viewId, viewName, application);
    }

    @Override
    public final WorkbenchApplication getApplication() {
        return (WorkbenchApplication) super.getApplication();
    }

    @Override
    public final WorkbenchPane getViewport() {
        return null != this.viewport ? this.viewport : (this.viewport = new WorkbenchPane());
    }

    @Override
    public void setupInitialize() {
        final long st0 = System.currentTimeMillis();

        getApplication().updateStartingProgress();
        this.getThemeProvider().applyTheme(UserPrefs.prefs.getString("ui.theme", "light"));

        getEventBus().addEventHandler(ApplicationEvent.STARTED, event ->
                this.viewport.setRootViewsDividerPosition(UserPrefs.prefs.getDouble("workbench.views.divider", 0.2)));
        getEventBus().addEventHandler(ApplicationEvent.STOPPING, event -> {
            UserPrefs.prefs.setProperty("workbench.views.divider", this.viewport.getRootViewsDividerPosition());
            UserPrefs.prefs.setProperty("workbench.sideviews.visible", this.viewport.isSideViewsVisible());
        });
        final List<WorkbenchViewController> viewControllers = createViewControllers();
        viewControllers.forEach(controller -> {
            getApplication().updateStartingProgress();
            this.viewport.addWorkbenchView(controller);
        });
        viewControllers.forEach(controller -> {
            getApplication().updateStartingProgress();
            controller.setupInitialize();
        });
        getApplication().updateStartingProgress();

        this.viewport.setRootViewsDividerPosition(UserPrefs.prefs.getDouble("workbench.views.divider", 0.2));
        if (UserPrefs.prefs.getBoolean("workbench.sideviews.visible", true))
            this.viewport.selectSideTool(null);
        DevtoolHelper.LOG.info("load views used time: " + (System.currentTimeMillis() - st0));
    }

    protected abstract List<WorkbenchViewController> createViewControllers();
}
