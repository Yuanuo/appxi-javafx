package org.appxi.javafx.workbench;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.HPos;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

public interface WorkbenchPart {
    StringProperty id();

    StringProperty title();

    StringProperty tooltip();

    ObjectProperty<Node> graphic();

    void initialize();

    interface SideTool extends WorkbenchPart {
        default HPos sideToolAlignment() {
            return HPos.RIGHT;
        }

        void activeViewport(boolean firstTime);
    }

    interface SideView extends WorkbenchPart {
        default HPos sideToolAlignment() {
            return HPos.LEFT;
        }

        Pane getViewport();

        void activeViewport(boolean firstTime);

        default void inactiveViewport() {
        }
    }

    interface MainView extends WorkbenchPart {
        StringProperty appTitle();

        Pane getViewport();

        void activeViewport(boolean firstTime);

        void inactiveViewport(boolean closing);
    }
}
