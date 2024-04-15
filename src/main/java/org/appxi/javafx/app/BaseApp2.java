package org.appxi.javafx.app;

import org.appxi.javafx.visual.VisualProvider;

import java.nio.file.Path;

public abstract class BaseApp2 extends BaseApp {
    private final VisualProvider visualProvider = new VisualProvider(this);

    public BaseApp2(Path workspace) {
        super(workspace);
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
