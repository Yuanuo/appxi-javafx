package org.appxi.javafx.app.web;

import org.appxi.javafx.app.BaseApp;
import org.appxi.javafx.helper.BaseAction;
import org.appxi.javafx.web.WebPane;

public abstract class WebAction extends BaseAction {
    public final WebViewer webViewer;
    public final BaseApp app;
    public final WebPane webPane;

    public WebAction(WebViewer webViewer) {
        this.webViewer = webViewer;
        this.app = webViewer.app;
        this.webPane = webViewer.webPane;
    }
}
