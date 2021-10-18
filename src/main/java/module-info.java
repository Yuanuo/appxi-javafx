module appxi.javafx {
    requires transitive javafx.base;
    requires transitive javafx.controls;
    requires transitive javafx.graphics;
    requires javafx.fxml;
    requires javafx.web;

    requires transitive appxi.shared;

    exports org.appxi.javafx.control;
    exports org.appxi.javafx.control.skin;
    exports org.appxi.javafx.desktop;
    exports org.appxi.javafx.event;
    exports org.appxi.javafx.iconfont;
    exports org.appxi.javafx.helper;
    exports org.appxi.javafx.theme;
    exports org.appxi.javafx.views;
    exports org.appxi.javafx.workbench;
    exports org.appxi.javafx.workbench.views;

    opens appxi.javafx.themes;
}