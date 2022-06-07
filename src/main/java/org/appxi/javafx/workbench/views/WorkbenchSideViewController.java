package org.appxi.javafx.workbench.views;

import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.appxi.javafx.control.HBoxEx;
import org.appxi.javafx.workbench.WorkbenchPane;
import org.appxi.javafx.workbench.WorkbenchViewController;

public abstract class WorkbenchSideViewController extends WorkbenchViewController {
    private BorderPane viewport;
    protected HBoxEx topBar;

    public WorkbenchSideViewController(String viewId, WorkbenchPane workbench) {
        super(viewId, workbench);
    }

    @Override
    public final BorderPane getViewport() {
        if (null == this.viewport) {
            this.viewport = new BorderPane();

            final Label title = new Label();
            title.getStyleClass().addAll("font-bold", "title");
            title.textProperty().bind(this.title);
            HBox.setHgrow(title, Priority.ALWAYS);

            this.topBar = new HBoxEx();
            this.topBar.getStyleClass().addAll("compact", "bob-line");
            this.viewport.setTop(this.topBar);

            this.topBar.addLeft(title);

            //
            initViewport(viewport);
        }
        return viewport;
    }

    protected abstract void initViewport(BorderPane viewport);

    public abstract void onViewportHiding();
}
