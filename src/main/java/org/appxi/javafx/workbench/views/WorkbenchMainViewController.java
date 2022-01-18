package org.appxi.javafx.workbench.views;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Tab;
import javafx.scene.layout.StackPane;
import org.appxi.javafx.workbench.WorkbenchPane;
import org.appxi.javafx.workbench.WorkbenchViewController;

public abstract class WorkbenchMainViewController extends WorkbenchViewController {
    public final ReadOnlyObjectProperty<Tab> tab = new SimpleObjectProperty<>();
    private StackPane viewport;

    public WorkbenchMainViewController(String viewId, WorkbenchPane workbench) {
        super(viewId, workbench);
    }

    @Override
    public final StackPane getViewport() {
        if (null == this.viewport) {
            this.viewport = new StackPane();
            //
            onViewportInitOnce(this.viewport);
        }
        return viewport;
    }

    protected abstract void onViewportInitOnce(StackPane viewport);

    public abstract void onViewportHiding();

    public abstract void onViewportClosing(boolean selected);

    @Override
    protected void setTitles(String title) {
        super.setTitles(title);
        appTitle.set(title);
    }

    public final StringProperty appTitle = new SimpleStringProperty();
}
