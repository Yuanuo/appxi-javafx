package org.appxi.javafx.workbench.views;

import javafx.scene.control.Tab;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;
import org.appxi.javafx.workbench.WorkbenchApplication;
import org.appxi.javafx.workbench.WorkbenchViewController;
import org.appxi.javafx.workbench.WorkbenchViewLocation;
import org.appxi.util.StringHelper;

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
        setPrimaryTitle(getMainTitle());
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

    protected String getMainTitle() {
        return null == mainTitle ? this.viewName : mainTitle;
    }

    private String mainTitle;

    protected final void setTitles(String title) {
        this.setTitles(title, title, title);
    }

    protected final void setTitles(String mainTitle, String toolTitle, String toolTip) {
        if (null != mainTitle)
            setPrimaryTitle(this.mainTitle = mainTitle);

        if (null != toolTitle) {
            toolTitle = StringHelper.trimBytes(toolTitle, 24);
            Tab tab = getPrimaryViewport().findMainViewTab(this.viewId);
            tab.setText(toolTitle);
            tab.setTooltip(new Tooltip(null == toolTip ? toolTitle : toolTip));
        }
    }
}
