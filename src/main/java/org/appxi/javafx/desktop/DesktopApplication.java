package org.appxi.javafx.desktop;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.application.Preloader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.appxi.javafx.control.StackPaneEx;
import org.appxi.javafx.event.EventBus;
import org.appxi.javafx.helper.StateHelper;
import org.appxi.javafx.theme.ThemeProvider;
import org.appxi.javafx.views.ViewController;
import org.appxi.prefs.PreferencesInProperties;
import org.appxi.prefs.UserPrefs;
import org.appxi.util.DevtoolHelper;
import org.appxi.util.StringHelper;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public abstract class DesktopApplication extends Application {
    private final long startTime = System.currentTimeMillis();

    public final EventBus eventBus = new EventBus();
    public final ThemeProvider themeProvider = new ThemeProvider(eventBus);

    private Stage primaryStage;
    private Scene primaryScene;
    private ViewController primaryController;
    private StackPaneEx primaryViewport;
    private String primaryFontStyle;

    //
    //            PUBLIC METHODS
    //

    public final Stage getPrimaryStage() {
        return primaryStage;
    }

    public final Scene getPrimaryScene() {
        return primaryScene;
    }

    public StackPaneEx getPrimaryViewport() {
        return primaryViewport;
    }

    public final void setPrimaryTitle(String title) {
        if (null == title)
            title = getApplicationTitle();
        else
            title = StringHelper.concat(title, " - ", getApplicationTitle());
        this.primaryStage.setTitle(title);
    }

    public String getPrimaryFontStyle() {
        return primaryFontStyle;
    }

    //
    //            USER FOCUSED METHODS
    //

    protected abstract String getApplicationId();

    protected abstract String getApplicationTitle();

    protected abstract List<URL> getApplicationIcons();

    protected abstract ViewController createPrimaryController();

    //
    //            BASIC INIT LOGICS
    //

    @Override
    public void init() throws Exception {
        // 1, init user prefs
        UserPrefs.setupDataDirectory(UserPrefs.dataDir().resolve("." + getApplicationId()), null);
        UserPrefs.prefs = new PreferencesInProperties(UserPrefs.confDir().resolve(".prefs"));
        UserPrefs.recents = new PreferencesInProperties(UserPrefs.confDir().resolve(".recents"));
        this.steps = UserPrefs.prefs.getDouble("ui.used", 20);
        Thread.setDefaultUncaughtExceptionHandler(this::handleDefaultUncaughtException);
    }

    protected void handleDefaultUncaughtException(Thread thread, Throwable throwable) {
        // for debug only
        throwable.printStackTrace();

        List<String> lines = StringHelper.getThrowableAsLines(throwable);
        if (lines.size() > 5)
            lines = lines.subList(0, 5);
        lines.add("...");

        final Alert alert = new Alert(Alert.AlertType.ERROR, StringHelper.joinLines(lines));
        alert.setResizable(true);
        alert.initOwner(this.primaryStage);
        alert.setWidth(800);
        alert.setHeight(600);
        alert.show();
    }

    @Override
    public final void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;

        updateStartingProgress();

        // 2, init main ui
        this.primaryController = createPrimaryController();
        this.primaryViewport = null == primaryController ? new StackPaneEx() : primaryController.getViewport();

        updateStartingProgress();

        // 3, init title and size, icons, etc.
        this.primaryScene = StateHelper.restoreScene(UserPrefs.prefs, this.primaryViewport);
        primaryStage.setScene(primaryScene);
        StateHelper.restoreStage(UserPrefs.prefs, primaryStage);
        this.setPrimaryTitle(null);
        this.primaryFontStyle = createPrimaryFontStyle();
        final Parent root = primaryScene.getRoot();
        root.setStyle(primaryFontStyle.concat(" ").concat(root.getStyle()));

        // 4, init the theme provider
        themeProvider.addScene(primaryScene);

        // 9, init something in async mode, then show the main stage
        CompletableFuture.runAsync(() -> {
            updateStartingProgress();

            // 5, init the theme provider
            final List<Image> icons = new ArrayList<>();
            for (URL iconRes : getApplicationIcons()) {
                try (InputStream iconStream = iconRes.openStream()) {
                    icons.add(new Image(iconStream));
                } catch (Exception ex) {
                    // ignore
                }
            }
            primaryStage.getIcons().setAll(icons);

            updateStartingProgress();
            // 6, a very special notification for special things(if you catch it)
            notifyPreloader(new Preloader.ProgressNotification(0.111));
            //
            updateStartingProgress();

            // 7, init the rootController
            if (null != primaryController) {
                primaryController.setupInitialize();
            }
            // 8, fire starting event for more custom things
            eventBus.fireEvent(new ApplicationEvent(ApplicationEvent.STARTING));
            starting();
        }).whenComplete((o, throwable) -> {
            if (null != throwable)
                handleDefaultUncaughtException(null, throwable);
            // for show main stage
            Platform.runLater(primaryStage::show);

            // for hide stage of preloader
            notifyPreloader(new Preloader.StateChangeNotification(Preloader.StateChangeNotification.Type.BEFORE_START));

            // finally
            eventBus.fireEvent(new ApplicationEvent(ApplicationEvent.STARTED));
            started();

            // for debug only
            DevtoolHelper.LOG.info(StringHelper.msg("App startup used steps/times: ", step, "/", System.currentTimeMillis() - startTime));
        });
    }

    protected String createPrimaryFontStyle() {
        /*-fx-font-family: "Microsoft YaHei", "PingFang SC", "Hiragino Sans GB", STXihei, Arial, "Helvetica Neue", Helvetica, sans-serif;*/
        final String osName = System.getProperty("os.name").toLowerCase();
        final String fontStyle;
        if (osName.contains("windows")) {
            fontStyle = "-fx-font: 16 \"Microsoft YaHei\"";
        } else if (osName.contains("mac") || osName.contains("osx")) {
            fontStyle = "-fx-font: 16 \"PingFang SC\"";
        } else if (osName.contains("linux") || osName.contains("ubuntu")) {
            fontStyle = "-fx-font: 16 \"WenQuanYi Micro Hei\"";
        } else {
            fontStyle = "-fx-font: 16 sans-serif";
        }
        return fontStyle;
    }

    protected void starting() {
    }

    protected void started() {
    }

    @Override
    public void stop() {
        try {
            eventBus.fireEvent(new ApplicationEvent(ApplicationEvent.STOPPING));
        } catch (Throwable ignored) {
        }
        UserPrefs.prefs.setProperty("ui.used", this.step);
        StateHelper.storeScene(UserPrefs.prefs, primaryStage.getScene());
        StateHelper.storeStage(UserPrefs.prefs, primaryStage);
        UserPrefs.prefs.save();
        UserPrefs.recents.save();
    }

    private double step = 1, steps = 20;

    public final void updateStartingProgress() {
        notifyPreloader(new Preloader.ProgressNotification(step++ / steps));
    }
}
