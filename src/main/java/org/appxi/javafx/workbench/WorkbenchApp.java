package org.appxi.javafx.workbench;

import javafx.scene.Scene;
import javafx.stage.Stage;
import org.appxi.javafx.app.AppEvent;
import org.appxi.javafx.app.DesktopApp;
import org.appxi.javafx.helper.FxHelper;
import org.appxi.prefs.UserPrefs;
import org.appxi.util.StringHelper;

import java.util.List;

public abstract class WorkbenchApp extends DesktopApp {
    private WorkbenchPane workbench;

    public WorkbenchPane workbench() {
        return workbench;
    }

    @Override
    protected void starting(Scene primaryScene) {
        workbench = new WorkbenchPane(this);
        workbench.initialize(createWorkbenchParts(workbench));
        getPrimaryGlass().getChildren().setAll(this.workbench);
        eventBus.addEventHandler(AppEvent.STOPPING, e -> {
            UserPrefs.prefs.setProperty("workbench.views.divider", workbench.getRootViewsDividerPosition());
            UserPrefs.prefs.setProperty("workbench.sides.visible", workbench.isSideViewsVisible());
        });

        super.starting(primaryScene);
    }

    @Override
    protected void started(Stage primaryStage) {
        workbench.setRootViewsDividerPosition(UserPrefs.prefs.getDouble("workbench.views.divider", 0.2));
        if (UserPrefs.prefs.getBoolean("workbench.sides.visible", true))
            FxHelper.runThread(() -> {
                workbench.selectSideTool(null);
                logger.info(StringHelper.concat("home-view shown after: ", System.currentTimeMillis() - startTime));
            });

        super.started(primaryStage);
    }

    protected abstract List<WorkbenchPart> createWorkbenchParts(WorkbenchPane workbench);
}
