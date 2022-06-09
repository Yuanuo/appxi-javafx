package org.appxi.javafx.app.web;

public class WebCallback {
    public final WebRenderer webRenderer;

    public WebCallback(WebRenderer webRenderer) {
        this.webRenderer = webRenderer;
    }

    public void onDocumentReady() {
    }

    public void log(String msg) {
        WebRenderer.logger.warn("javaApp.LOG : " + msg);
    }

    public void updateFinderState(int index, int count) {
        if (webRenderer instanceof WebViewer viewer && null != viewer.webFinder()) {
            viewer.webFinder().state(index, count);
        }
    }
}
