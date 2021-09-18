package org.appxi.javafx.desktop;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.application.Preloader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.appxi.javafx.event.EventBus;
import org.appxi.javafx.helper.FxHelper;
import org.appxi.javafx.helper.StateHelper;
import org.appxi.javafx.theme.ThemeProvider;
import org.appxi.javafx.views.ViewController;
import org.appxi.prefs.PreferencesInProperties;
import org.appxi.prefs.UserPrefs;
import org.appxi.util.StringHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public abstract class DesktopApplication extends Application {
    protected final Logger logger = LoggerFactory.getLogger(DesktopApplication.class);

    private final long startTime = System.currentTimeMillis();

    public final EventBus eventBus = new EventBus();
    public final ThemeProvider themeProvider = new ThemeProvider(eventBus);

    private Stage primaryStage;
    private Scene primaryScene;
    private ViewController primaryController;
    private StackPane primaryViewport;
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

    public StackPane getPrimaryViewport() {
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
        UserPrefs.prefs = new PreferencesInProperties(UserPrefs.confDir().resolve(".prefs"));
        UserPrefs.recents = new PreferencesInProperties(UserPrefs.confDir().resolve(".recents"));
        UserPrefs.favorites = new PreferencesInProperties(UserPrefs.confDir().resolve(".favorites"));
        this.steps = UserPrefs.prefs.getDouble("ui.used", 20);
        Thread.setDefaultUncaughtExceptionHandler((t, err) -> FxHelper.alertError(this, err));


        updateStartingProgress();
        // a very special notification for special things(if you catch it)
        notifyPreloader(new Preloader.ProgressNotification(0.111));
    }

    @Override
    public final void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;

        updateStartingProgress();

        // 2, init main ui
        this.primaryController = createPrimaryController();
        this.primaryViewport = null == primaryController ? new StackPane() : primaryController.getViewport();

        updateStartingProgress();

        // 3, init title and size, icons, etc.
        this.primaryScene = StateHelper.restoreScene(UserPrefs.prefs, this.primaryViewport);
        primaryStage.setScene(primaryScene);
        StateHelper.restoreStage(UserPrefs.prefs, primaryStage);
        primaryStage.setMinWidth(1366);
        primaryStage.setMinHeight(768);
        this.setPrimaryTitle(null);
        this.primaryFontStyle = createPrimaryFontStyle();
        final Parent root = primaryScene.getRoot();
        root.setStyle(primaryFontStyle.concat(";").concat(root.getStyle()));

        // 4, init the theme provider
        themeProvider.addScene(primaryScene);

        start();
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

            //
            updateStartingProgress();

            // 7, init the rootController
            if (null != primaryController) {
                primaryController.setupInitialize();
            }

            // 8, fire starting event for more custom things
            eventBus.fireEvent(new ApplicationEvent(ApplicationEvent.STARTING));
            starting();

            // for show main stage
            Platform.runLater(primaryStage::show);

            // for hide stage of preloader
            notifyPreloader(new Preloader.StateChangeNotification(Preloader.StateChangeNotification.Type.BEFORE_START));

            // finally
            eventBus.fireEvent(new ApplicationEvent(ApplicationEvent.STARTED));
            started();

            // for debug only
            logger.info(StringHelper.concat("App startup used steps/times: ", step, "/", System.currentTimeMillis() - startTime));
        }).whenComplete((o, throwable) -> {
            if (null != throwable)
                FxHelper.alertError(this, throwable);
        });
    }

    protected void start() {
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
        try {
            UserPrefs.prefs.setProperty("ui.used", this.step);
            StateHelper.storeScene(UserPrefs.prefs, primaryStage);
            StateHelper.storeStage(UserPrefs.prefs, primaryStage);
            UserPrefs.prefs.save();
        } catch (Throwable ignored) {
        }
        System.exit(0);
    }

    private double step = 1, steps = 20;

    public final void updateStartingProgress() {
        notifyPreloader(new Preloader.ProgressNotification(step++ / steps));
    }

    protected String createPrimaryFontStyle() {
        String fontSize = UserPrefs.prefs.getString("ui.font.size", null);
        if (null == fontSize || fontSize.isBlank()) {
            fontSize = "14";
            UserPrefs.prefs.setProperty("ui.font.size", fontSize);
        }
        fontSize = "-fx-font-size:".concat(fontSize).concat(";");

        String fontName = UserPrefs.prefs.getString("ui.font.name", null);
        if (fontName == null || fontName.isBlank()) {
            final String osName = System.getProperty("os.name").toLowerCase();
            if (osName.contains("windows")) {
                fontName = "Microsoft YaHei";
            } else if (osName.contains("mac") || osName.contains("osx")) {
                fontName = "PingFang SC";
            } else if (osName.contains("linux") || osName.contains("ubuntu")) {
                fontName = "WenQuanYi Micro Hei";
            } else {
                fontName = "SYSTEM";
            }
            UserPrefs.prefs.setProperty("ui.font.name", fontName);
        }
        fontName = "-fx-font-family: \"".concat(fontName).concat("\";");

        return fontSize.concat(fontName);
    }

    public final void updatePrimaryFontStyle() {
        final String updatedFontStyle = this.createPrimaryFontStyle();
        primaryScene.getRoot().setStyle(primaryScene.getRoot().getStyle().replace(primaryFontStyle, updatedFontStyle));
        this.primaryFontStyle = updatedFontStyle;
    }
}
