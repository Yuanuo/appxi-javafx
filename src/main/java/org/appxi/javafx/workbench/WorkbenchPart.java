package org.appxi.javafx.workbench;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

public interface WorkbenchPart {
    StringProperty id();

    StringProperty title();

    StringProperty tooltip();

    ObjectProperty<Node> graphic();

    /**
     * 此函数是为了解决简单的依赖问题。
     * 在实例构造之后，在真正触发之前执行。
     */
    void postConstruct();

    interface SideTool extends WorkbenchPart {
        default boolean sideToolAlignTop() {
            return false;
        }

        void activeViewport(boolean firstTime);
    }

    interface SideView extends WorkbenchPart {
        default boolean sideToolAlignTop() {
            return true;
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
