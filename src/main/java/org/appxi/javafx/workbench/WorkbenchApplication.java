package org.appxi.javafx.workbench;

import org.appxi.javafx.desktop.ApplicationEvent;
import org.appxi.javafx.desktop.DesktopApplication;

public abstract class WorkbenchApplication extends DesktopApplication {
    private static final String CSS = WorkbenchApplication.class.getResource("/appxi/javafx/workbench.css").toExternalForm();

    @Override
    public WorkbenchPane getPrimaryViewport() {
        return (WorkbenchPane) super.getPrimaryViewport();
    }

    @Override
    public void init() throws Exception {
        super.init();
        eventBus.addEventHandler(ApplicationEvent.STARTING,
                event -> getPrimaryStage().getScene().getStylesheets().add(CSS));
    }

    @Override
    protected abstract WorkbenchPrimaryController createPrimaryController();
}
