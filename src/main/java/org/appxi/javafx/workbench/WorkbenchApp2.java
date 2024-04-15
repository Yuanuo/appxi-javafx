package org.appxi.javafx.workbench;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.appxi.javafx.app.BaseApp;
import org.appxi.javafx.visual.VisualProvider;

import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class WorkbenchApp2 extends WorkbenchApp {
    public final StringProperty title2 = new SimpleStringProperty();

    public final BaseApp app;

    public WorkbenchApp2(Path workspace, BaseApp app) {
        super(workspace);
        this.app = app;
        this.init();
    }

    @Override
    public String getAppName() {
        return app.getAppName();
    }

    @Override
    public List<URL> getAppIcons() {
        return app.getAppIcons();
    }

    @Override
    public VisualProvider visualProvider() {
        return app.visualProvider();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setOnHiding(wEvt -> stop());
        super.start(primaryStage);
    }

    @Override
    protected void starting(Scene primaryScene) {
        final StringProperty titleProperty = getPrimaryStage().titleProperty();
        if (titleProperty.isBound()) {
            titleProperty.unbind();
            titleProperty.bind(Bindings.createStringBinding(
                    () -> Stream.of(title.get(), title2.get(), getAppName())
                            .filter(s -> null != s && !s.isEmpty()).collect(Collectors.joining("   -   ")),
                    this.title, this.title2));
        }
        //
        super.starting(primaryScene);
    }
}
