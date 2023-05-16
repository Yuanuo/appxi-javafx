package org.appxi.javafx.app.web;

import org.appxi.javafx.app.BaseApp;
import org.appxi.javafx.web.WebFindByMarks;
import org.appxi.smartcn.pinyin.PinyinHelper;

public class WebToolFinder extends WebTool {
    public final WebFindByMarks webFinder;

    public WebToolFinder(BaseApp app, WebViewer webViewer) {
        super(webViewer);
        //
        webFinder = new WebFindByMarks(app, this.webPane, webViewer.viewport);
        webFinder.setAsciiConvertor(PinyinHelper::pinyin);
        webPane.getTopBar().addRight(webFinder);
    }
}
