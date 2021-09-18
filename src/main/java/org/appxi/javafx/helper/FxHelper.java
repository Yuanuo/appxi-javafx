package org.appxi.javafx.helper;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import org.appxi.javafx.control.BlockingView;
import org.appxi.javafx.desktop.DesktopApplication;
import org.appxi.util.StringHelper;

import java.nio.file.Path;
import java.util.List;

public abstract class FxHelper {
    /**
     * 用于标记当前App是否是用户安装后的运行状态
     */
    public static final boolean productionMode = null != System.getProperty("jpackage.app-path");
    private static Path _appDir = null;
    private static final Object appDirInit = new Object();

    public static Path appDir() {
        if (null != _appDir)
            return _appDir;

        synchronized (appDirInit) {
            if (null != _appDir)
                return _appDir;

            String appDir = System.getenv("app-dir");
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

    static class DisabledEffectsListener implements ChangeListener<Boolean> {
        private final Node node;
        private final ColorAdjust effects = new ColorAdjust();

        public DisabledEffectsListener(Node node) {
            this.node = node;
            effects.setInput(new BoxBlur());
            effects.setBrightness(-0.5);
        }

        @Override
        public void changed(ObservableValue<? extends Boolean> o, Boolean ov, Boolean nv) {
            node.setEffect(nv ? effects : null);
        }
    }

    public static <T extends Node> T setDisabledEffects(T node) {
        DisabledEffectsListener listener = (DisabledEffectsListener) node.getProperties().get(DisabledEffectsListener.class);
        if (null == listener)
            node.getProperties().put(DisabledEffectsListener.class, listener = new DisabledEffectsListener(node));
        node.disabledProperty().removeListener(listener);
        node.disabledProperty().addListener(listener);
        return node;
    }

    public static void runLater(Runnable runnable) {
        if (Platform.isFxApplicationThread())
            runnable.run();
        else Platform.runLater(runnable);
    }

    public static void alertError(DesktopApplication application, Throwable throwable) {
        Runnable runnable = () -> alertErrorEx(application, throwable).show();
        if (Platform.isFxApplicationThread())
            runnable.run();
        else Platform.runLater(runnable);
    }

    public static Alert alertErrorEx(DesktopApplication application, Throwable throwable) {
        // for debug only
        throwable.printStackTrace();

        List<String> lines = StringHelper.getThrowableAsLines(throwable);
        if (lines.size() > 5)
            lines = lines.subList(0, 5);
        lines.add("...");

        final Alert alert = new Alert(Alert.AlertType.ERROR, StringHelper.joinLines(lines));
        alert.setResizable(true);
        alert.initOwner(application.getPrimaryStage());
        alert.setWidth(800);
        alert.setHeight(600);
        return FxHelper.withTheme(application, alert);
    }

    public static Scene withStyle(DesktopApplication application, Scene scene) {
        scene.getRoot().setStyle(application.getPrimaryFontStyle().concat(";").concat(scene.getRoot().getStyle()));
        final Stage stage = (Stage) scene.getWindow();
        if (null != stage)
            stage.getIcons().addAll(application.getPrimaryStage().getIcons());
        return scene;
    }

    public static Scene withTheme(DesktopApplication application, Scene scene) {
        withStyle(application, scene);
        application.themeProvider.applyThemeFor(scene);
        return scene;
    }

    public static <T extends Dialog<?>> T withTheme(DesktopApplication application, T dialog) {
        if (null == dialog.getOwner())
            dialog.initOwner(application.getPrimaryStage());
        final DialogPane pane = dialog.getDialogPane();
        // 必须要有至少一个按钮才能关闭此窗口
        if (pane.getButtonTypes().isEmpty())
            pane.getButtonTypes().add(ButtonType.OK);
        withStyle(application, pane.getScene());
        // force center on screen
        final Window window = dialog.getDialogPane().getScene().getWindow();
        window.addEventHandler(WindowEvent.WINDOW_SHOWN, event -> {
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            window.setX((screenBounds.getWidth() - window.getWidth()) / 2);
            window.setY((screenBounds.getHeight() - window.getHeight()) / 2);

        });
        return dialog;
    }

    /////////////////
    public static void runBlocking(Pane pane, Runnable runnable) {
        new Thread(() -> {
            final BlockingView blockingView = new BlockingView();
            try {
                Platform.runLater(() -> pane.getChildren().add(blockingView));
                runnable.run();
            } finally {
                Platform.runLater(() -> pane.getChildren().remove(blockingView));
            }
        }).start();
    }

    public static Runnable manualBlocking(Pane pane, Runnable runnable) {
        final BlockingView blockingView = new BlockingView();
        new Thread(() -> {
            Platform.runLater(() -> pane.getChildren().add(blockingView));
            runnable.run();
        }).start();
        return () -> Platform.runLater(() -> pane.getChildren().remove(blockingView));
    }

    public static Runnable manualBlocking(Pane pane) {
        final BlockingView blockingView = new BlockingView();
        Platform.runLater(() -> pane.getChildren().add(blockingView));
        return () -> Platform.runLater(() -> pane.getChildren().remove(blockingView));
    }
}
