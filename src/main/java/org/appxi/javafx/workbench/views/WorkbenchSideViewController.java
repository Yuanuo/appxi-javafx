package org.appxi.javafx.workbench.views;

import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.appxi.javafx.workbench.WorkbenchApplication;
import org.appxi.javafx.workbench.WorkbenchViewController;

public abstract class WorkbenchSideViewController extends WorkbenchViewController {
    protected BorderPane viewport;
    protected VBox viewportVBox;
    protected HBox headBar;

    public WorkbenchSideViewController(String viewId, WorkbenchApplication application) {
        super(viewId, application);
    }

    @Override
    public final BorderPane getViewport() {
        if (null == this.viewport) {
            this.viewport = new BorderPane();

            final Label titleBar = new Label();
            titleBar.getStyleClass().add("title-bar");
            titleBar.textProperty().bind(viewTitle);

            this.headBar = new HBox(titleBar);
            this.headBar.getStyleClass().addAll("flat-tool-bar");
            this.viewport.setTop(headBar);

            this.viewportVBox = new VBox();
            this.viewportVBox.getStyleClass().addAll("vbox", "side-vbox");
            this.viewport.setCenter(this.viewportVBox);
            //
            onViewportInitOnce();
        }
        return viewport;
    }

    protected abstract void onViewportInitOnce();

    public abstract void onViewportHiding();
}
