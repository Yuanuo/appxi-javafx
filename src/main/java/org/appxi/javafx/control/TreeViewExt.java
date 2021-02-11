package org.appxi.javafx.control;

import javafx.scene.Node;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.input.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.function.BiConsumer;

public class TreeViewExt<T> extends TreeViewEx<T> {
    private BiConsumer<InputEvent, TreeItem<T>> enterOrDoubleClickAction;

    public TreeViewExt() {
        this(null);
    }

    public TreeViewExt(BiConsumer<InputEvent, TreeItem<T>> enterOrDoubleClickAction) {
        super();
        this.enterOrDoubleClickAction = enterOrDoubleClickAction;

        VBox.setVgrow(this, Priority.ALWAYS);
        this.setShowRoot(false);
        this.setOnKeyReleased(this::handleOnKeyReleased);
        this.setOnMouseReleased(this::handleOnMouseReleased);
    }

    public TreeViewExt<T> setEnterOrDoubleClickAction(BiConsumer<InputEvent, TreeItem<T>> enterOrDoubleClickAction) {
        this.enterOrDoubleClickAction = enterOrDoubleClickAction;
        return this;
    }

    private void handleOnKeyReleased(KeyEvent event) {
        if (event.getCode() != KeyCode.ENTER)
            return;
        final TreeItem<T> treeItem = this.getSelectionModel().getSelectedItem();
        if (null == treeItem)
            return;
        if (!treeItem.isLeaf()) {
            treeItem.setExpanded(!treeItem.isExpanded());
            return;
        }

        handleOnKeyReleasedImpl(event, treeItem);
    }

    private void handleOnMouseReleased(MouseEvent event) {
        if (event.getButton() != MouseButton.PRIMARY)
            return;

        TreeItem<T> treeItem = null;
        final Node pickedNode = event.getPickResult().getIntersectedNode();
        if (pickedNode instanceof TreeCell cell)
            treeItem = cell.getTreeItem();
        else if (pickedNode.getParent() instanceof TreeCell cell)
            treeItem = cell.getTreeItem();
        else if (pickedNode.getParent().getParent() instanceof TreeCell cell)
            treeItem = cell.getTreeItem();

        if (null == treeItem)
            return;

        if (!treeItem.isLeaf()) {
            final boolean expanded = !treeItem.isExpanded();
            treeItem.setExpanded(expanded);
            this.layout();
            this.scrollToIfNotVisible(treeItem);
            return;
        }

        handleOnMouseReleasedImpl(event, treeItem);
    }

    protected void handleOnKeyReleasedImpl(KeyEvent event, TreeItem<T> treeItem) {
        if (event.getCode() == KeyCode.ENTER) {
            handleOnEnterOrDoubleClicked(event, treeItem);
            event.consume();
        }
    }

    protected void handleOnMouseReleasedImpl(MouseEvent event, TreeItem<T> treeItem) {
        if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() > 1) {
            handleOnEnterOrDoubleClicked(event, treeItem);
            event.consume();
        }
    }

    protected void handleOnEnterOrDoubleClicked(InputEvent event, TreeItem<T> treeItem) {
        if (null != enterOrDoubleClickAction)
            enterOrDoubleClickAction.accept(event, treeItem);
        else throw new UnsupportedOperationException("Not impl");
    }
}
