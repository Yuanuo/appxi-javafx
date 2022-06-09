package org.appxi.javafx.workbench;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import org.appxi.javafx.control.HBoxEx;
import org.appxi.util.ext.Attributes;

public abstract class WorkbenchPartController extends Attributes implements WorkbenchPart {
    public final StringProperty id = new SimpleStringProperty();
    public final StringProperty title = new SimpleStringProperty();
    public final StringProperty tooltip = new SimpleStringProperty();
    public final ObjectProperty<Node> graphic = new SimpleObjectProperty<>();

    public final WorkbenchApp app;
    public final WorkbenchPane workbench;

    public WorkbenchPartController(WorkbenchPane workbench) {
        this.app = workbench.application;
        this.workbench = workbench;
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

    public static abstract class SideView extends WorkbenchPartController implements WorkbenchPart.SideView {
        private BorderPane viewport;
        protected HBoxEx topBar;

        public SideView(WorkbenchPane workbench) {
            super(workbench);
        }

        @Override
        public final BorderPane getViewport() {
            if (null == viewport) {
                createViewport(this.viewport = new BorderPane());
            }
            return viewport;
        }

        protected void createViewport(BorderPane viewport) {
            this.topBar = new HBoxEx();
            this.topBar.getStyleClass().addAll("compact", "bob-line");
            this.viewport.setTop(this.topBar);

            final Label title = new Label();
            title.getStyleClass().addAll("font-bold", "title");
            title.textProperty().bind(this.title);
            HBox.setHgrow(title, Priority.ALWAYS);
            this.topBar.addLeft(title);
        }
    }

    public static abstract class MainView extends WorkbenchPartController implements WorkbenchPart.MainView {
        public final StringProperty appTitle = new SimpleStringProperty();
        private StackPane viewport;

        public MainView(WorkbenchPane workbench) {
            super(workbench);
        }

        @Override
        public final StringProperty appTitle() {
            return appTitle;
        }

        @Override
        public final StackPane getViewport() {
            if (null == viewport) {
                createViewport(this.viewport = new StackPane());
            }
            return viewport;
        }

        protected void createViewport(StackPane viewport) {
        }
    }
}
