package org.appxi.javafx.app.web;

import org.appxi.javafx.app.BaseApp;
import org.appxi.javafx.web.WebPane;

public abstract class WebTool {
    public final WebViewer webViewer;
    public final BaseApp app;
    public final WebPane webPane;

    public WebTool(WebViewer webViewer) {
        this.webViewer = webViewer;
        this.app = webViewer.app;
        this.webPane = webViewer.webPane;
    }
}
