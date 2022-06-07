package org.appxi.javafx.web;

import java.util.function.Function;

public class WebFindByMark extends WebFinder {
    private boolean scrollToVisible = true;
    private Function<String, String> inputConvertor;
    protected String searchedText;

    public WebFindByMark(WebPane webPane) {
        super();

        prev.setOnAction(event -> webPane.executeScript("markFinder.findPrev()"));
        next.setOnAction(event -> webPane.executeScript("markFinder.findNext()"));
        clear.setOnAction(event -> input.setText(""));
        input.textProperty().addListener((o, ov, nv) -> {
            searchedText = null == nv ? "" : nv.strip();
            if (null != inputConvertor) searchedText = inputConvertor.apply(searchedText);
            if (null == searchedText || searchedText.isBlank()) {
                webPane.executeScript("markFinder.clear()");
                return;
            }

            webPane.executeScript("markFinder.find('".concat(searchedText).concat("', " + scrollToVisible + ")"));
        });
    }

    public final WebFindByMark setInputConvertor(Function<String, String> inputConvertor) {
        this.inputConvertor = inputConvertor;
        return this;
    }

    public void mark(String text) {
        scrollToVisible = false;
        input.setText(text);
        scrollToVisible = true;
    }
}
