package org.appxi.javafx.workbench;

import org.appxi.javafx.desktop.ApplicationEvent;
import org.appxi.javafx.views.ViewController;
import org.appxi.javafx.workbench.views.WorkbenchOpenpartController;
import org.appxi.javafx.workbench.views.WorkbenchViewpartController;
import org.appxi.javafx.workbench.views.WorkbenchWorkpartController;
import org.appxi.javafx.workbench.views.WorkbenchWorktoolController;
import org.appxi.prefs.UserPrefs;

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
//        long st = st0;
        final List<WorkbenchViewpartController> viewControllers = createViewpartControllers();
        viewControllers.forEach(viewController -> {
            getApplication().updateStartingProgress();
            viewController.setupApplication(getApplication(), this);
        });
//        System.out.println("setup viewpart views used time: " + (System.currentTimeMillis() - st));
//        st = System.currentTimeMillis();
        viewControllers.forEach(viewController -> {
            getApplication().updateStartingProgress();
            this.addWorkbenchViewpartController(viewController);
        });
//        System.out.println("add viewpart views used time: " + (System.currentTimeMillis() - st));
//        st = System.currentTimeMillis();
        viewControllers.forEach(controller -> {
//            final long st1 = System.currentTimeMillis();
            getApplication().updateStartingProgress();
            controller.setupInitialize();
//            System.out.println("init 1 viewpart view used time: " + (System.currentTimeMillis() - st1));
        });
//        System.out.println("init all viewpart views used time: " + (System.currentTimeMillis() - st));
        getApplication().updateStartingProgress();

        getViewport().setWorkviewsDividerPosition(UserPrefs.prefs.getDouble("workbench.views.divider", 0.2));
        if (UserPrefs.prefs.getBoolean("workbench.workviews.visible", true))
            this.getViewport().selectWorktool(null);
        System.out.println("load viewpart views used time: " + (System.currentTimeMillis() - st0));
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
