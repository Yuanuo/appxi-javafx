package org.appxi.javafx.helper;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.appxi.prefs.Preferences;

public interface StateHelper {
    static void storeWindow(Preferences prefs, Window window) {
        prefs.setProperty("ui.window.x", window.getX());
        prefs.setProperty("ui.window.y", window.getY());
        prefs.setProperty("ui.window.width", window.getWidth());
        prefs.setProperty("ui.window.height", window.getHeight());
    }

    static void restoreWindow(Preferences prefs, Window window) {
        final double x = prefs.getDouble("ui.window.x", -99999);
        if (x != -99999) window.setX(x);
        final double y = prefs.getDouble("ui.window.y", -99999);
        if (y != -99999) window.setY(y);
        window.setWidth(prefs.getDouble("ui.window.width", 1280));
        window.setHeight(prefs.getDouble("ui.window.height", 720));
    }

    static void storeStage(Preferences prefs, Stage stage) {
        if (!stage.isMaximized())
            storeWindow(prefs, stage);
        storeMaximized(prefs, stage);
    }

    static void restoreStage(Preferences prefs, Stage stage) {
        restoreWindow(prefs, stage);
        restoreMaximized(prefs, stage);
    }

    static void storeMaximized(Preferences prefs, Stage stage) {
        prefs.setProperty("ui.window.maximized", stage.isMaximized());
    }

    static void restoreMaximized(Preferences prefs, Stage stage) {
        stage.setMaximized(prefs.getBoolean("ui.window.maximized", false));
    }

    static void storeScene(Preferences prefs, Stage stage) {
        if (stage.isMaximized())
            return;
        prefs.setProperty("ui.scene.width", stage.getScene().getWidth());
        prefs.setProperty("ui.scene.height", stage.getScene().getHeight());
    }

    static Scene restoreScene(Preferences prefs, Parent root) {
        return new Scene(root,
                prefs.getDouble("ui.scene.width", -1),
                prefs.getDouble("ui.scene.height", -1));
    }

}
