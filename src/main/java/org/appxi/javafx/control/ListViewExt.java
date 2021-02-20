package org.appxi.javafx.control;

import javafx.scene.input.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.function.BiConsumer;

public class ListViewExt<T> extends ListViewEx<T> {
    private BiConsumer<InputEvent, T> enterOrDoubleClickAction;

    public ListViewExt() {
        this(null);
    }

    public ListViewExt(BiConsumer<InputEvent, T> enterOrDoubleClickAction) {
        super();
        this.enterOrDoubleClickAction = enterOrDoubleClickAction;

        VBox.setVgrow(this, Priority.ALWAYS);
        this.setOnKeyReleased(this::handleOnKeyReleased);
        this.setOnMouseReleased(this::handleOnMouseReleased);
    }

    public ListViewExt<T> setEnterOrDoubleClickAction(BiConsumer<InputEvent, T> enterOrDoubleClickAction) {
        this.enterOrDoubleClickAction = enterOrDoubleClickAction;
        return this;
    }

    private void handleOnKeyReleased(KeyEvent event) {
        if (event.getCode() != KeyCode.ENTER)
            return;
        final T listItem = this.getSelectionModel().getSelectedItem();
        if (null == listItem)
            return;
        handleOnKeyReleasedImpl(event, listItem);
    }

    private void handleOnMouseReleased(MouseEvent event) {
        if (event.getButton() != MouseButton.PRIMARY)
            return;
        final T listItem = this.getSelectionModel().getSelectedItem();
        if (null == listItem)
            return;
        handleOnMouseReleasedImpl(event, listItem);
    }

    protected void handleOnKeyReleasedImpl(KeyEvent event, T listItem) {
        if (event.getCode() == KeyCode.ENTER) {
            handleTreeViewOnEnterOrDoubleClicked(event, listItem);
            event.consume();
        }
    }

    protected void handleOnMouseReleasedImpl(MouseEvent event, T listItem) {
        if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() > 1) {
            handleTreeViewOnEnterOrDoubleClicked(event, listItem);
            event.consume();
        }
    }

    protected void handleTreeViewOnEnterOrDoubleClicked(InputEvent event, T listItem) {
        if (null != enterOrDoubleClickAction)
            enterOrDoubleClickAction.accept(event, listItem);
        else throw new UnsupportedOperationException("Not impl");
    }
}
