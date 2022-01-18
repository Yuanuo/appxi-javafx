package org.appxi.javafx.workbench;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import org.appxi.util.StringHelper;
import org.appxi.util.ext.Attributes;

public abstract class WorkbenchViewController extends Attributes {
    public final StringProperty viewTitle = new SimpleStringProperty();
    public final StringProperty viewTooltip = new SimpleStringProperty();
    public final ObjectProperty<Node> viewGraphic = new SimpleObjectProperty<>();

    public final WorkbenchApp app;
    public final WorkbenchPane workbench;
    public final SimpleStringProperty viewId;

    public WorkbenchViewController(String viewId, WorkbenchPane workbench) {
        this.app = workbench.application;
        this.workbench = workbench;
        this.viewId = new SimpleStringProperty(this, "viewId", viewId);
    }

    public abstract <T> T getViewport();

    public abstract void initialize();

    protected void setTitles(String title) {
        setTitles(title, title);
    }

    protected void setTitles(String title, String tooltip) {
        viewTitle.set(null == title ? null : StringHelper.trimChars(title, 20));
        viewTooltip.set(tooltip);
    }

    public abstract void onViewportShowing(boolean firstTime);
}
