package org.appxi.javafx.workbench.views;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.Event;
import javafx.scene.layout.StackPane;
import org.appxi.javafx.workbench.WorkbenchPane;
import org.appxi.javafx.workbench.WorkbenchViewController;

public abstract class WorkbenchMainViewController extends WorkbenchViewController {
    public final StringProperty appTitle = new SimpleStringProperty();

    private StackPane viewport;

    public WorkbenchMainViewController(String viewId, WorkbenchPane workbench) {
        super(viewId, workbench);
        this.appTitle.bind(this.title);
    }

    @Override
    public final StackPane getViewport() {
        if (null == this.viewport) {
            initViewport(this.viewport = new StackPane());
        }
        return viewport;
    }

    protected abstract void initViewport(StackPane viewport);

    public abstract void onViewportHiding();

    public abstract void onViewportClosing(Event event, boolean selected);
}
