module appxi.javafx {
    requires transitive javafx.base;
    requires transitive javafx.controls;
    requires transitive javafx.graphics;
    requires transitive javafx.web;

    requires transitive appxi.shared;

    exports org.appxi.javafx.app;
    exports org.appxi.javafx.control;
    exports org.appxi.javafx.control.skin;
    exports org.appxi.javafx.helper;
    exports org.appxi.javafx.settings;
    exports org.appxi.javafx.visual;
    exports org.appxi.javafx.workbench;
    exports org.appxi.javafx.workbench.views;

    opens org.appxi.javafx.visual;
}