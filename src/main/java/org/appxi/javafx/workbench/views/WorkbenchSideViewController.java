package org.appxi.javafx.workbench.views;

import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.appxi.javafx.control.AlignedBar;
import org.appxi.javafx.workbench.WorkbenchApplication;
import org.appxi.javafx.workbench.WorkbenchViewController;

public abstract class WorkbenchSideViewController extends WorkbenchViewController {
    protected BorderPane viewport;
    protected VBox viewportVBox;
    protected AlignedBar toolbar;

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

            this.toolbar = new AlignedBar();
            this.viewport.setTop(this.toolbar);

            this.viewportVBox = new VBox();
            this.viewportVBox.getStyleClass().addAll("vbox", "side-vbox");
            this.viewport.setCenter(this.viewportVBox);
            //
            final Label headline = new Label(this.viewName);
            headline.getStyleClass().add("headline");
            this.toolbar.addLeft(headline);
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
