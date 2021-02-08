package org.appxi.javafx.workbench.views;

import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.appxi.javafx.control.ToolBarEx;
import org.appxi.javafx.workbench.WorkbenchApplication;
import org.appxi.javafx.workbench.WorkbenchViewController;

public abstract class WorkbenchSideViewController extends WorkbenchViewController {
    protected BorderPane viewport;
    protected VBox viewportVBox;
    protected ToolBarEx toolbar;
    protected Label titleBar;

    public WorkbenchSideViewController(String viewId, String viewName, WorkbenchApplication application) {
        super(viewId, viewName, application);
    }

    @Override
    public final Boolean isPlaceInSideViews() {
        return true;
    }

    @Override
    public final BorderPane getViewport() {
        if (null == this.viewport) {
            this.viewport = new BorderPane();

            this.titleBar = new Label(this.viewName);
            this.titleBar.getStyleClass().add("title-bar");

            final HBox headBar = new HBox(titleBar);
            headBar.getStyleClass().add("head-bar");
            this.viewport.setTop(headBar);

            if (isToolbarEnable()) {
                this.toolbar = new ToolBarEx();
                HBox.setHgrow(this.toolbar, Priority.ALWAYS);
                headBar.getChildren().add(this.toolbar);
            }

            this.viewportVBox = new VBox();
            this.viewportVBox.getStyleClass().addAll("vbox", "side-vbox");
            this.viewport.setCenter(this.viewportVBox);
            //
            onViewportInitOnce();
        }
        return viewport;
    }

    protected boolean isToolbarEnable() {
        return false;
    }

    protected abstract void onViewportInitOnce();

    @Override
    public void onViewportHide(boolean hideOrElseClose) {
    }

    public final void setViewTitle(String title) {
        if (null == this.titleBar)
            return;

        if (null == title)
            title = this.viewName;
        else
            title = this.viewName.concat(" ").concat(title);
        this.titleBar.setText(title);
    }
}
