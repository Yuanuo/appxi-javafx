package org.appxi.javafx.views;

import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.stage.Stage;
import org.appxi.javafx.control.StackPaneEx;
import org.appxi.javafx.desktop.DesktopApplication;
import org.appxi.javafx.event.EventBus;
import org.appxi.javafx.theme.ThemeProvider;
import org.appxi.util.ext.Attributes;

import java.util.function.Consumer;

public abstract class ViewController extends Attributes {
    public final String viewId, viewName;
    public final DesktopApplication application;

    public ViewController(String viewId, String viewName, DesktopApplication application) {
        this.viewId = viewId;
        this.viewName = viewName;
        this.application = application;
    }

    public abstract <T> T getViewport();

    public abstract void setupInitialize();

    /*
     * methods just for easy access
     */

    public DesktopApplication getApplication() {
        return application;
    }

    public final Stage getPrimaryStage() {
        return this.application.getPrimaryStage();
    }

    public final Scene getPrimaryScene() {
        return this.application.getPrimaryScene();
    }

    public StackPaneEx getPrimaryViewport() {
        return this.application.getPrimaryViewport();
    }

    public final EventBus getEventBus() {
        return this.application.eventBus;
    }

    public final ThemeProvider getThemeProvider() {
        return this.application.themeProvider;
    }

    public final ViewController setPrimaryTitle(String title) {
        this.application.setPrimaryTitle(title);
        return this;
    }


    public void showAlertWithThemeAndWaitForNothing(Alert alert) {
        final DialogPane alertPane = alert.getDialogPane();
        if (alertPane.getButtonTypes().isEmpty())
            alertPane.getButtonTypes().add(ButtonType.OK);
        final Scene scene = alertPane.getScene();
        scene.getRoot().setStyle(getPrimaryScene().getRoot().getStyle());
        final Stage stage = (Stage) scene.getWindow();
        stage.getIcons().addAll(getPrimaryStage().getIcons());
        getThemeProvider().addScene(scene);
        final Consumer<ButtonType> action = v -> getThemeProvider().removeScene(scene);
        alert.showAndWait().ifPresentOrElse(action, () -> action.accept(null));
    }
}
