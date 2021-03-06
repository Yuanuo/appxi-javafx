package org.appxi.javafx.workbench.views;

import javafx.scene.control.Tab;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;
import org.appxi.javafx.workbench.WorkbenchApplication;
import org.appxi.javafx.workbench.WorkbenchViewController;
import org.appxi.javafx.workbench.WorkbenchViewLocation;

public abstract class WorkbenchMainViewController extends WorkbenchViewController {
    private StackPane viewport;

    public WorkbenchMainViewController(String viewId, String viewName, WorkbenchApplication application) {
        super(viewId, viewName, application);
    }

    @Override
    public final WorkbenchViewLocation getWorkbenchViewLocation() {
        return isWithSideTool() ? WorkbenchViewLocation.mainViewWithSideTool : WorkbenchViewLocation.mainView;
    }

    protected boolean isWithSideTool() {
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
    public final void onViewportShow(boolean firstTime) {
        setPrimaryTitle(null == viewTitle ? this.viewName : viewTitle);
        onViewportShowing(firstTime);
    }

    protected abstract void onViewportShowing(boolean firstTime);

    @Override
    public final void onViewportHide(boolean hideOrElseClose) {
        if (hideOrElseClose) {
            onViewportHiding();
            setPrimaryTitle(null);
        } else {
            onViewportClosing();
            Tab tab = getPrimaryViewport().findMainViewTab(this.viewId);
            if (null != tab && tab.isSelected())
                setPrimaryTitle(null);
        }
    }

    protected void onViewportHiding() {
    }

    protected void onViewportClosing() {
    }

    private String viewTitle;

    protected final void setViewTitle(String title) {
        if (null == title)
            title = this.viewName;

        this.viewTitle = title;

        Tab tab = getPrimaryViewport().findMainViewTab(this.viewId);
        tab.setText(title);
        tab.setTooltip(new Tooltip(title));
        setPrimaryTitle(title);
    }
}
