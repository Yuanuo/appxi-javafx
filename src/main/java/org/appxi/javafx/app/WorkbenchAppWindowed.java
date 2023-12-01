package org.appxi.javafx.app;

import javafx.stage.Stage;
import org.appxi.javafx.visual.VisualProvider;
import org.appxi.javafx.workbench.WorkbenchApp;

import java.net.URL;
import java.nio.file.Path;
import java.util.List;

public abstract class WorkbenchAppWindowed extends WorkbenchApp {
    public final BaseApp app;

    public WorkbenchAppWindowed(Path workspace, BaseApp app) {
        super(workspace);
        this.app = app;
        this.init();
    }

    @Override
    public String getAppName() {
        return app.getAppName();
    }

    @Override
    protected List<URL> getAppIcons() {
        return app.getAppIcons();
    }

    @Override
    public VisualProvider visualProvider() {
        return app.visualProvider();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setOnHiding(wEvt -> stop());
        super.start(primaryStage);
    }
}
