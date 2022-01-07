package org.appxi.javafx.control;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.appxi.javafx.control.skin.ListViewSkinEx;

import java.util.function.BiConsumer;

public class ListViewEx<T> extends ListView<T> {
    private BiConsumer<InputEvent, T> enterOrDoubleClickAction;

    public ListViewEx() {
        this(null);
    }

    public ListViewEx(BiConsumer<InputEvent, T> enterOrDoubleClickAction) {
        super();
        this.enterOrDoubleClickAction = enterOrDoubleClickAction;
        this.setOnKeyReleased(this::handleOnKeyReleased);
        this.setOnMouseReleased(this::handleOnMouseReleased);
    }

    @Override
    protected final ListViewSkinEx<T> createDefaultSkin() {
        return new ListViewSkinEx<>(this);
    }

    protected final ListViewSkinEx<T> getSkinEx() {
        return (ListViewSkinEx<T>) getSkin();
    }

    public boolean isRowVisible(int rowIndex) {
        final ListViewSkinEx<T> skinEx = getSkinEx();
        if (null == skinEx) return false;
        final ListCell<T> firstCell = skinEx.getVirtualFlowEx().getFirstVisibleCell();
        final ListCell<T> lastCell = skinEx.getVirtualFlowEx().getLastVisibleCell();
        if (null == firstCell || null == lastCell)
            return false;
        return rowIndex > firstCell.getIndex() && rowIndex < lastCell.getIndex();
    }

    public void scrollToIfNotVisible(int rowIndex) {
        if (this.isRowVisible(rowIndex))
            return;
        this.scrollTo(rowIndex);
    }


    public ListViewEx<T> setEnterOrDoubleClickAction(BiConsumer<InputEvent, T> enterOrDoubleClickAction) {
        this.enterOrDoubleClickAction = enterOrDoubleClickAction;
        return this;
    }

    public BiConsumer<InputEvent, T> enterOrDoubleClickAction() {
        return enterOrDoubleClickAction;
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
