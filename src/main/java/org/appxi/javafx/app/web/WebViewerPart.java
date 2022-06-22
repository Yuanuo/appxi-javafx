package org.appxi.javafx.app.web;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import org.appxi.javafx.workbench.WorkbenchPane;
import org.appxi.javafx.workbench.WorkbenchPart;

public abstract class WebViewerPart extends WebViewer implements WorkbenchPart {
    private static final Object AK_INITIALIZED = new Object();

    public final StringProperty id = new SimpleStringProperty();
    public final StringProperty title = new SimpleStringProperty();
    public final StringProperty tooltip = new SimpleStringProperty();
    public final ObjectProperty<Node> graphic = new SimpleObjectProperty<>();

    public WebViewerPart(WorkbenchPane workbench) {
        super(workbench);
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

    public static abstract class SideView extends WebViewerPart implements WorkbenchPart.SideView {
        public SideView(WorkbenchPane workbench) {
            super(workbench);
        }

        @Override
        public final StackPane getViewport() {
            return viewport;
        }

        @Override
        public void activeViewport(boolean firstTime) {
            // 确保initialize函数被调用
            if (!viewport.getProperties().containsKey(AK_INITIALIZED)) {
                initialize();
                viewport.getProperties().put(AK_INITIALIZED, true);
            }
            // 此处默认实现中仅在首次调用时触发
            if (firstTime) {
                navigate(null);
            }
        }

        @Override
        public void inactiveViewport() {
            if (!viewport.getProperties().containsKey(AK_INITIALIZED)) {
                return;
            }
            saveUserData();
        }
    }

    public static abstract class MainView extends WebViewerPart implements WorkbenchPart.MainView {
        public final StringProperty appTitle = new SimpleStringProperty();

        public MainView(WorkbenchPane workbench) {
            super(workbench);
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
            // 确保initialize函数被调用
            if (!viewport.getProperties().containsKey(AK_INITIALIZED)) {
                initialize();
                viewport.getProperties().put(AK_INITIALIZED, true);
            }
            // 此处默认实现中仅在首次调用时触发
            if (firstTime) {
                navigate(null);
            }
        }

        @Override
        public void inactiveViewport(boolean closing) {
            // 尚未真实显示过此视图时，不须更多操作
            if (!viewport.getProperties().containsKey(AK_INITIALIZED)) {
                return;
            }
            if (closing) {
                deinitialize();
            } else {
                saveUserData();
            }
        }
    }
}
