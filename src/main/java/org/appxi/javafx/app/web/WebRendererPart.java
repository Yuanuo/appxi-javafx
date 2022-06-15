package org.appxi.javafx.app.web;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import org.appxi.javafx.workbench.WorkbenchPane;
import org.appxi.javafx.workbench.WorkbenchPart;

public abstract class WebRendererPart extends WebRenderer implements WorkbenchPart {
    private static final Object AK_INITIALIZED = new Object();

    public final StringProperty id = new SimpleStringProperty();
    public final StringProperty title = new SimpleStringProperty();
    public final StringProperty tooltip = new SimpleStringProperty();
    public final ObjectProperty<Node> graphic = new SimpleObjectProperty<>();

    public WebRendererPart(WorkbenchPane workbench, StackPane viewport) {
        super(workbench, viewport);
    }

    @Override
    public final StringProperty id() {
        return id;
    }

    @Override
    public final StringProperty title() {
        return title;
    }

    @Override
    public final StringProperty tooltip() {
        return tooltip;
    }

    @Override
    public final ObjectProperty<Node> graphic() {
        return graphic;
    }

    public static abstract class SideView extends WebRendererPart implements WorkbenchPart.SideView {
        public SideView(WorkbenchPane workbench, StackPane viewport) {
            super(workbench, viewport);
        }

        @Override
        public final StackPane getViewport() {
            return viewport;
        }

        @Override
        public void activeViewport(boolean firstTime) {
            if (firstTime) {
                if (!viewport.getProperties().containsKey(AK_INITIALIZED)) {
                    viewport.getProperties().put(AK_INITIALIZED, true);
                    initialize();
                }
                navigate(null);
            }
        }
    }

    public static abstract class MainView extends WebRendererPart implements WorkbenchPart.MainView {
        public final StringProperty appTitle = new SimpleStringProperty();

        public MainView(WorkbenchPane workbench, StackPane viewport) {
            super(workbench, viewport);
        }

        @Override
        public final StringProperty appTitle() {
            return appTitle;
        }

        @Override
        public final StackPane getViewport() {
            return viewport;
        }

        @Override
        public void activeViewport(boolean firstTime) {
            if (firstTime) {
                if (!viewport.getProperties().containsKey(AK_INITIALIZED)) {
                    viewport.getProperties().put(AK_INITIALIZED, true);
                    initialize();
                }
                navigate(null);
            }
        }

        @Override
        public void inactiveViewport(boolean closing) {
            if (closing) {
                deinitialize();
            }
        }
    }
}
