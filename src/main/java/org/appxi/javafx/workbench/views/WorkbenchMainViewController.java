package org.appxi.javafx.workbench.views;

import javafx.scene.control.Tab;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;
import org.appxi.javafx.workbench.WorkbenchApplication;
import org.appxi.javafx.workbench.WorkbenchViewController;

public abstract class WorkbenchMainViewController extends WorkbenchViewController {
    private StackPane viewport;

    public WorkbenchMainViewController(String viewId, String viewName, WorkbenchApplication application) {
        super(viewId, viewName, application);
    }

    @Override
    public final Boolean isPlaceInSideViews() {
        return false;
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

    @Override
    public void onViewportHide(boolean hideOrElseClose) {
    }

    protected final void setSecondaryTitle(String title) {
        if (null == title)
            title = this.viewName;
        Tab tab = getPrimaryViewport().findMainViewTab(this.viewId);
        tab.setText(title);
        tab.setTooltip(new Tooltip(title));
    }
}
