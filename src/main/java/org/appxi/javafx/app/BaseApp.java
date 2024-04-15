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
import org.appxi.javafx.control.Notifications;
import org.appxi.javafx.helper.FxHelper;
import org.appxi.javafx.settings.Option;
import org.appxi.javafx.visual.VisualProvider;
import org.appxi.prefs.Preferences;
import org.appxi.prefs.PreferencesInProperties;
import org.appxi.util.StringHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class BaseApp extends javafx.application.Application {
    public static final Logger logger = LoggerFactory.getLogger(BaseApp.class);

    public final EventBus eventBus = new EventBus();
    public final List<Supplier<Option<?>>> options = new ArrayList<>();
    public final StringProperty title = new SimpleStringProperty();
    public final Path workspace;
    public final Preferences config, recents, favorites;

    private Stage primaryStage;
    private Scene primaryScene;
    private StackPane primaryGlass;

    public BaseApp(Path workspace) {
        this.workspace = workspace;
        this.config = new PreferencesInProperties(workspace.resolve(".conf"));
        this.recents = new PreferencesInProperties(workspace.resolve(".recents"));
        this.favorites = new PreferencesInProperties(workspace.resolve(".favorites"));
        Thread.setDefaultUncaughtExceptionHandler(this::handleUncaughtException);
    }

    protected void handleUncaughtException(Thread thread, Throwable throwable) {
        logger.error("<UNCAUGHT>", throwable);
        toastError(throwable.getClass().getName().concat(": ").concat(throwable.getMessage()));
    }

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

    public abstract List<URL> getAppIcons();

    public abstract VisualProvider visualProvider();

    public final long startTime = System.currentTimeMillis();

    @Override
    public void init() {
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryGlass = new StackPane();
        this.primaryScene = new Scene(this.primaryGlass,
                config.getDouble("ui.scene.width", -1),
                config.getDouble("ui.scene.height", -1)
        );
        this.primaryStage = primaryStage;
        this.primaryStage.setScene(this.primaryScene);

        CompletableFuture.runAsync(() -> {
            this.primaryStage.titleProperty().bind(Bindings.createStringBinding(
                    () -> Stream.of(title.get(), getAppName())
                            .filter(s -> null != s && !s.isEmpty()).collect(Collectors.joining("   -   ")),
                    this.title));

            final List<Image> icons = new ArrayList<>();
            for (URL iconRes : getAppIcons()) {
                try (InputStream iconStream = iconRes.openStream()) {
                    icons.add(new Image(iconStream));
                } catch (Exception ignore) {
                }
            }
            this.primaryStage.getIcons().setAll(icons);

            starting(primaryScene);

            Platform.runLater(() -> showing(this.primaryStage));

            started(this.primaryStage);

            // for debug only
            logger.info("App startup after: %d".formatted(System.currentTimeMillis() - startTime));
        }).whenComplete((ret, err) -> {
            if (null != err) err.printStackTrace();
        });
    }

    protected void starting(Scene primaryScene) {
        FxHelper.loadStageInfo(config, getPrimaryStage());
        getPrimaryStage().setMinWidth(960);
        getPrimaryStage().setMinHeight(480);
        eventBus.addEventHandler(AppEvent.STOPPING, e -> FxHelper.saveStageInfo(config, getPrimaryStage()));

        // for debug only
        logger.info("starting after: %d".formatted(System.currentTimeMillis() - startTime));
        eventBus.fireEvent(new AppEvent(AppEvent.STARTING));
        // for debug only
        logger.info("post init after: %d".formatted(System.currentTimeMillis() - startTime));
    }

    protected void showing(Stage primaryStage) {
        // for debug only
        logger.info("primaryStage showing after: %d".formatted(System.currentTimeMillis() - startTime));
        primaryStage.show();
        // for debug only
        logger.info("primaryStage shown after: %d".formatted(System.currentTimeMillis() - startTime));
        //
        visualProvider().apply(primaryStage);
        // for debug only
        logger.info("visual initialize after: %d".formatted(System.currentTimeMillis() - startTime));
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
            recents.save();
            favorites.save();
        } catch (Throwable ignored) {
        }
        try {
            config.save();
        } catch (Throwable ignored) {
        }
        stopped();
    }

    protected void stopped() {
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
}
