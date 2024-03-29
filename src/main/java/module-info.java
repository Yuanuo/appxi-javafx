module appxi.javafx {
    requires transitive javafx.base;
    requires transitive javafx.controls;
    requires transitive javafx.graphics;
    requires transitive javafx.web;
    requires transitive jdk.jsobject;

    requires transitive appxi.shared;
    requires transitive appxi.smartcn.convert;
    requires transitive appxi.smartcn.pinyin;

    exports org.appxi.javafx.app;
    exports org.appxi.javafx.app.search;
    exports org.appxi.javafx.app.web;
    exports org.appxi.javafx.control;
    exports org.appxi.javafx.control.skin;
    exports org.appxi.javafx.helper;
    exports org.appxi.javafx.settings;
    exports org.appxi.javafx.visual;
    exports org.appxi.javafx.web;
    exports org.appxi.javafx.workbench;

    opens org.appxi.javafx.visual;
}