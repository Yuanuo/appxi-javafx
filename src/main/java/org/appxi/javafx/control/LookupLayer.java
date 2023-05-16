package org.appxi.javafx.control;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import org.appxi.javafx.helper.FxHelper;

public abstract class LookupLayer<T> extends LookupPane<T> {
    private final StackPane glassPane;

    private DialogLayer dialogLayer;

    public LookupLayer(StackPane glassPane) {
        this.glassPane = glassPane;
    }

    public void hide() {
        if (null != dialogLayer && dialogLayer.isShowing()) {
            dialogLayer.hide();
        }
    }

    public void show(String searchText) {
        if (null == dialogLayer) {
            final EventHandler<Event> handleEventToHide = evt -> {
                boolean handled = false;
                if (evt instanceof KeyEvent event) {
                    handled = dialogLayer.isShowing() && event.getCode() == KeyCode.ESCAPE;
                } else if (evt instanceof MouseEvent event) {
                    handled = dialogLayer.isShowing() && event.getButton() == MouseButton.PRIMARY;
                } else if (evt instanceof ActionEvent) {
                    handled = dialogLayer.isShowing();
                }
                if (handled) {
                    hide();
                    evt.consume();
                }
            };

            dialogLayer = new DialogLayer();
            dialogLayer.setOnKeyPressed(handleEventToHide);
            dialogLayer.setOnMousePressed(Event::consume);
            dialogLayer.opaqueLayer.setOnMousePressed(handleEventToHide);

            int pad = getPaddingSizeOfParent();
            dialogLayer.autoPadding(pad, pad);

            dialogLayer.setHeaderText(getHeaderText());

            dialogLayer.setContent(this);
        }
        if (!dialogLayer.isShowing()) {
            dialogLayer.show(glassPane);
        }
        FxHelper.runThread(30, textInput.input::requestFocus);
        search(null != searchText ? searchText : getSearchedText());
        textInput.input.selectAll();
    }

    protected int getPaddingSizeOfParent() {
        return 100;
    }

    protected abstract String getHeaderText();
}
