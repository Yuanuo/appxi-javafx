package org.appxi.javafx.app;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.appxi.event.EventBus;
import org.appxi.javafx.visual.VisualProvider;
import org.appxi.prefs.PreferencesInProperties;
import org.appxi.prefs.UserPrefs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public abstract class BaseApp extends javafx.application.Application {
    public final Logger logger = LoggerFactory.getLogger(BaseApp.class);
    public final EventBus eventBus = new EventBus();
    public final VisualProvider visualProvider = new VisualProvider(this.eventBus, this::getPrimaryScene);
    public final StringProperty title = new SimpleStringProperty();

    private Stage primaryStage;
    private Scene primaryScene;
    private StackPane primaryGlass;


    public final Stage getPrimaryStage() {
        return primaryStage;
    }

    public final Scene getPrimaryScene() {
        return primaryScene;
    }

    public final StackPane getPrimaryGlass() {
        return primaryGlass;
    }


    public abstract String getAppName();

    protected abstract List<URL> getAppIcons();


    public final long startTime = System.currentTimeMillis();

    protected void handleUncaughtException(Thread thread, Throwable throwable) {
        logger.error("<UNCAUGHT>", throwable);
    }

    @Override
    public void init() throws Exception {
        // 1, init user prefs
//        UserPrefs.prefs = new PreferencesInProperties(UserPrefs.confDir().resolve(".prefs"));
        UserPrefs.recents = new PreferencesInProperties(UserPrefs.confDir().resolve(".recents"));
        UserPrefs.favorites = new PreferencesInProperties(UserPrefs.confDir().resolve(".favorites"));
        Thread.setDefaultUncaughtExceptionHandler(this::handleUncaughtException);
    }

    @Override
    public final void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        this.primaryGlass = new StackPane();
        this.primaryScene = new Scene(this.primaryGlass,
                UserPrefs.prefs.getDouble("ui.scene.width", -1),
                UserPrefs.prefs.getDouble("ui.scene.height", -1)
        );
        primaryStage.setScene(this.primaryScene);

        CompletableFuture.runAsync(() -> {
            this.primaryStage.titleProperty().bind(Bindings.createStringBinding(() -> {
                final String title = this.title.get();
                return null == title || title.isBlank() ? getAppName() : title.concat("   -   ").concat(getAppName());
            }, this.title));

            final List<Image> icons = new ArrayList<>();
            for (URL iconRes : getAppIcons()) {
                try (InputStream iconStream = iconRes.openStream()) {
                    icons.add(new Image(iconStream));
                } catch (Exception ignore) {
                }
            }
            primaryStage.getIcons().setAll(icons);

            starting(primaryScene);

            Platform.runLater(() -> showing(primaryStage));

            started(primaryStage);

            // for debug only
            logger.warn("App startup after: %d".formatted(System.currentTimeMillis() - startTime));
        }).whenComplete((ret, err) -> {
            if (null != err) err.printStackTrace();
        });
    }

    protected void starting(Scene primaryScene) {
        // for debug only
        logger.warn("starting after: %d".formatted(System.currentTimeMillis() - startTime));
        eventBus.fireEvent(new AppEvent(AppEvent.STARTING));
        // for debug only
        logger.warn("post init after: %d".formatted(System.currentTimeMillis() - startTime));
    }

    protected void showing(Stage primaryStage) {
        // for debug only
        logger.warn("primaryStage showing after: %d".formatted(System.currentTimeMillis() - startTime));
        primaryStage.show();
        // for debug only
        logger.warn("primaryStage shown after: %d".formatted(System.currentTimeMillis() - startTime));
        //
        visualProvider.initialize();
        // for debug only
        logger.warn("visual initialize after: %d".formatted(System.currentTimeMillis() - startTime));
    }

    protected void started(Stage primaryStage) {
        eventBus.fireEvent(new AppEvent(AppEvent.STARTED));
    }

    @Override
    public final void stop() {
        try {
            eventBus.fireEvent(new AppEvent(AppEvent.STOPPING));
        } catch (Throwable ignored) {
        }
        try {
            UserPrefs.recents.save();
            UserPrefs.favorites.save();
        } catch (Throwable ignored) {
        }
        try {
            UserPrefs.prefs.save();
        } catch (Throwable ignored) {
        }
        System.exit(0);
    }
}
