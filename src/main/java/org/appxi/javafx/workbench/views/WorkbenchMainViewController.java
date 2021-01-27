package org.appxi.javafx.workbench.views;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.appxi.javafx.workbench.WorkbenchApplication;
import org.appxi.javafx.workbench.WorkbenchViewController;

public abstract class WorkbenchMainViewController extends WorkbenchViewController {
    protected BorderPane viewport;
    protected VBox viewportVBox;

    public WorkbenchMainViewController(String viewId, String viewName, WorkbenchApplication application) {
        super(viewId, viewName, application);
    }

    @Override
    public final Boolean isPlaceInSideViews() {
        return false;
    }

    @Override
    public final BorderPane getViewport() {
        if (null == this.viewport) {
            this.viewportVBox = new VBox();
            this.viewportVBox.getStyleClass().addAll("vbox", "main-vbox");
            this.viewport = new BorderPane(this.viewportVBox);
            //
            initViewport();
        }
        return viewport;
    }

    protected abstract void initViewport();

    @Override
    public void hideViewport(boolean hideOrElseClose) {
    }
}
