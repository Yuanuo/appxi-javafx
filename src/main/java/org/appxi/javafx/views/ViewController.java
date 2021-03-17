package org.appxi.javafx.views;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.appxi.javafx.desktop.DesktopApplication;
import org.appxi.javafx.event.EventBus;
import org.appxi.javafx.theme.ThemeProvider;
import org.appxi.util.StringHelper;
import org.appxi.util.ext.Attributes;

public abstract class ViewController extends Attributes {
    public final String viewId;
    public final DesktopApplication application;

    public ViewController(String viewId, DesktopApplication application) {
        this.viewId = viewId;
        this.application = application;
    }

    public abstract <T> T getViewport();

    public abstract void setupInitialize();

    /*
     * titles
     */

    protected void setTitles(String title) {
        setTitles(title, title);
    }

    protected void setTitles(String title, String tooltip) {
        viewTitle.set(null == title ? null : StringHelper.trimChars(title, 20));
        viewTooltip.set(tooltip);
    }

    public final StringProperty viewTitle = new SimpleStringProperty();
    public final StringProperty viewTooltip = new SimpleStringProperty();
    public final ObjectProperty<Node> viewIcon = new SimpleObjectProperty<>();

    /*
     * methods just for easy access
     */

    public final Stage getPrimaryStage() {
        return this.application.getPrimaryStage();
    }

    public final Scene getPrimaryScene() {
        return this.application.getPrimaryScene();
    }

    public final EventBus getEventBus() {
        return this.application.eventBus;
    }

    public final ThemeProvider getThemeProvider() {
        return this.application.themeProvider;
    }
}
