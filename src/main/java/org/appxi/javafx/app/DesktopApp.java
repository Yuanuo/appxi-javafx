package org.appxi.javafx.app;

import javafx.scene.Scene;
import javafx.stage.Stage;
import org.appxi.javafx.control.Notifications;
import org.appxi.javafx.helper.FxHelper;
import org.appxi.prefs.Preferences;
import org.appxi.prefs.UserPrefs;
import org.appxi.util.StringHelper;

import java.nio.file.Path;

public abstract class DesktopApp extends BaseApp {
    @Override
    protected void starting(Scene primaryScene) {
        restoreStage(UserPrefs.prefs, getPrimaryStage());
        getPrimaryStage().setMinWidth(960);
        getPrimaryStage().setMinHeight(480);
        eventBus.addEventHandler(AppEvent.STOPPING, e -> storeStage(UserPrefs.prefs, getPrimaryStage()));

        super.starting(primaryScene);
    }

    static void storeStage(Preferences prefs, Stage stage) {
        if (!stage.isMaximized()) {
            prefs.setProperty("ui.window.x", stage.getX());
            prefs.setProperty("ui.window.y", stage.getY());
            prefs.setProperty("ui.window.width", stage.getWidth());
            prefs.setProperty("ui.window.height", stage.getHeight());

            prefs.setProperty("ui.scene.width", stage.getScene().getWidth());
            prefs.setProperty("ui.scene.height", stage.getScene().getHeight());
        }

        prefs.setProperty("ui.window.maximized", stage.isMaximized());
    }

    static void restoreStage(Preferences prefs, Stage stage) {
        final double x = prefs.getDouble("ui.window.x", -99999);
        if (x != -99999) stage.setX(x);
        final double y = prefs.getDouble("ui.window.y", -99999);
        if (y != -99999) stage.setY(y);
        stage.setWidth(prefs.getDouble("ui.window.width", 1280));
        stage.setHeight(prefs.getDouble("ui.window.height", 720));

        stage.setMaximized(prefs.getBoolean("ui.window.maximized", false));
    }

    @Override
    protected void handleUncaughtException(Thread thread, Throwable throwable) {
        super.handleUncaughtException(thread, throwable);
        toastError(throwable.getMessage());
    }

    public void toast(String msg) {
        FxHelper.runLater(() -> Notifications.of().description(null == msg ? "<EMPTY>" : StringHelper.trimChars(msg, 512))
                .owner(getPrimaryStage())
                .showInformation()
        );
    }

    public void toastError(String msg) {
        FxHelper.runLater(() -> Notifications.of().description(null == msg ? "<EMPTY>" : StringHelper.trimChars(msg, 512))
                .owner(getPrimaryStage())
                .showError()
        );
    }

    public static final boolean productionMode = null != System.getProperty("jpackage.app-path");
    private static final Object _appDirInit = new Object();
    private static Path _appDir = null;

    public static Path appDir() {
        if (null != _appDir)
            return _appDir;

        synchronized (_appDirInit) {
            if (null != _appDir)
                return _appDir;

            String appDir = System.getenv("app-dir");
            if (null == appDir)
                appDir = System.getProperty("app-dir");
            if (null == appDir)
                appDir = System.getProperty("jpackage.app-dir");

            if (null == appDir) {
                appDir = System.getProperty("jpackage.app-path");
                if (null != appDir) {
                    String osName = System.getProperty("os.name").toLowerCase();
                    Path appPath = Path.of(appDir).getParent();
                    if (osName.contains("win")) {
                        appDir = appPath.resolve("app").toString();
                    } else if (osName.contains("mac")) {

                    } else {
                        appDir = appPath.resolve("lib").toString();
                    }
                }
            }
            if (null == appDir)
                appDir = "";
            _appDir = Path.of(appDir).toAbsolutePath();
        }
        return _appDir;
    }
}
