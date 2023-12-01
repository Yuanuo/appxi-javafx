package org.appxi.javafx.app;

import org.appxi.javafx.visual.VisualProvider;

import java.nio.file.Path;

public abstract class BootstrapApp extends BaseApp {
    private final VisualProvider visualProvider = new VisualProvider(this);

    public BootstrapApp(Path workspace) {
        super(workspace);
        Thread.setDefaultUncaughtExceptionHandler(this::handleUncaughtException);
    }

    protected void handleUncaughtException(Thread thread, Throwable throwable) {
        logger.error("<UNCAUGHT>", throwable);
        toastError(throwable.getClass().getName().concat(": ").concat(throwable.getMessage()));
    }

    @Override
    public VisualProvider visualProvider() {
        return visualProvider;
    }

    @Override
    protected void stopped() {
        super.stopped();
        System.exit(0);
    }
}
