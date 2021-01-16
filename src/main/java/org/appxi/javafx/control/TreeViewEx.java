package org.appxi.javafx.control;

import javafx.event.EventDispatcher;
import javafx.scene.Node;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import org.appxi.javafx.control.skin.TreeViewSkinEx;

public class TreeViewEx<T> extends TreeView<T> {
    private final EventDispatcher originalDispatcher;

    public TreeViewEx() {
        super();
        this.originalDispatcher = super.getEventDispatcher();
        this.setEventDispatcher((event, tail) -> {
            if (event instanceof MouseEvent msEvt) {
                if (msEvt.getEventType() == MouseEvent.MOUSE_PRESSED) {
                    final Node pickedNode = msEvt.getPickResult().getIntersectedNode();
                    if (pickedNode instanceof StackPane
                            && (pickedNode.getParent() instanceof TreeCell
                            || pickedNode.getParent().getParent() instanceof TreeCell))
                        event.consume();
                }
            }
            return originalDispatcher.dispatchEvent(event, tail);
        });
    }

    @Override
    protected final TreeViewSkinEx<T> createDefaultSkin() {
        return new TreeViewSkinEx<>(this);
    }

    protected final TreeViewSkinEx<T> getSkinEx() {
        return (TreeViewSkinEx<T>) super.getSkin();
    }

    public boolean isRowVisible(TreeItem<T> item) {
        return this.isRowVisible(getRow(item));
    }

    public boolean isRowVisible(int rowIndex) {
        final TreeCell<T> firstCell = getSkinEx().getVirtualFlowEx().getFirstVisibleCell();
        final TreeCell<T> lastCell = getSkinEx().getVirtualFlowEx().getLastVisibleCell();
        if (null == firstCell || null == lastCell)
            return false;
        return firstCell.getIndex() <= rowIndex && lastCell.getIndex() >= rowIndex;
    }

    public void scrollTo(TreeItem<T> item) {
        final int index = getRow(item);
        if (index != -1)
            this.scrollTo(index);
    }

    public void scrollToIfNotVisible(TreeItem<T> item) {
        this.scrollToIfNotVisible(getRow(item));
    }

    public void scrollToIfNotVisible(int rowIndex) {
        if (this.isRowVisible(rowIndex))
            return;
        this.scrollTo(rowIndex);
    }
}
