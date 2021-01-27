package org.appxi.javafx.workbench;

import javafx.scene.Node;
import org.appxi.javafx.views.ViewController;

public abstract class WorkbenchViewController extends ViewController {
    public WorkbenchViewController(String viewId, String viewName, WorkbenchApplication application) {
        super(viewId, viewName, application);
    }

    @Override
    public final WorkbenchApplication getApplication() {
        return (WorkbenchApplication) this.application;
    }

    @Override
    public final WorkbenchPane getPrimaryViewport() {
        return this.getApplication().getPrimaryViewport();
    }

    /**
     * @return null for side-tools only; true for side-views; false for main-views;
     */
    public abstract Boolean isPlaceInSideViews();

    public abstract Node createToolIconGraphic(Boolean placeInSideViews);

    public abstract void showViewport(boolean firstTime);

    public abstract void hideViewport(boolean hideOrElseClose);
}
