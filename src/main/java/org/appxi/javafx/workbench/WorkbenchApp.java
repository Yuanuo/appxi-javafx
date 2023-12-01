package org.appxi.javafx.workbench;

import javafx.scene.Scene;
import javafx.stage.Stage;
import org.appxi.javafx.app.AppEvent;
import org.appxi.javafx.app.BaseApp;
import org.appxi.javafx.helper.FxHelper;
import org.appxi.util.StringHelper;

import java.nio.file.Path;
import java.util.List;

public abstract class WorkbenchApp extends BaseApp {
    private WorkbenchPane workbench;

    public WorkbenchApp(Path workspace) {
        super(workspace);
    }

    public WorkbenchPane workbench() {
        return workbench;
    }

    @Override
    protected void starting(Scene primaryScene) {
        workbench = new WorkbenchPane(this, this::createWorkbenchParts);
        getPrimaryGlass().getChildren().setAll(this.workbench);
        eventBus.addEventHandler(AppEvent.STOPPING, e -> {
            config.setProperty("workbench.views.divider", workbench.getRootViewsDividerPosition());
            config.setProperty("workbench.sides.visible", workbench.isSideViewsVisible());
        });

        super.starting(primaryScene);
    }

    @Override
    protected void started(Stage primaryStage) {
        workbench.setRootViewsDividerPosition(config.getDouble("workbench.views.divider", 0.2));
        if (config.getBoolean("workbench.sides.visible", true))
            FxHelper.runThread(() -> {
                workbench.selectSideTool(null);
                logger.info(StringHelper.concat("home-view shown after: ", System.currentTimeMillis() - startTime));
            });

        super.started(primaryStage);
    }

    protected abstract List<WorkbenchPart> createWorkbenchParts(WorkbenchPane workbench);
}
