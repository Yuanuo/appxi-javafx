package org.appxi.javafx.control;

import javafx.event.Event;
import javafx.event.EventDispatcher;
import javafx.scene.Node;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import org.appxi.javafx.control.skin.TreeViewSkinEx;

import java.util.function.BiConsumer;

public class TreeViewEx<T> extends TreeView<T> {
    private final EventDispatcher originalEventDispatcher;
    private BiConsumer<InputEvent, TreeItem<T>> enterOrDoubleClickAction;

    /**
     * 默认禁用左侧三角按钮事件
     *
     * @see #TreeViewEx(boolean)
     */
    public TreeViewEx() {
        this(true);
    }

    /**
     * 允许TreeView扩展控件在构造时指定是否禁用左侧三角按钮事件
     *
     * @param disableArrowAction 指定是否禁用左侧三角按钮事件
     */
    public TreeViewEx(boolean disableArrowAction) {
        super();
        originalEventDispatcher = super.getEventDispatcher();
        if (disableArrowAction) {
            this.setEventDispatcher((event, tail) -> {
                if (event instanceof MouseEvent msEvt) {
                    if (msEvt.getEventType() == MouseEvent.MOUSE_PRESSED) {
                        final Node pickedNode = msEvt.getPickResult().getIntersectedNode();
                        if (pickedNode instanceof StackPane && (
                                pickedNode.getParent() instanceof TreeCell
                                || pickedNode.getParent().getParent() instanceof TreeCell)
                        )
                            event.consume();
                    }
                }
                return originalEventDispatcher.dispatchEvent(event, tail);
            });
        }

        this.setShowRoot(false);
        this.setOnKeyReleased(this::handleOnKeyReleased);
        this.setOnMousePressed(Event::consume);
        this.setOnMouseReleased(this::handleOnMouseReleased);

        this.addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED, event -> handleOnContextMenuRequested());
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

    public int fetchNextInvisibleRow(TreeCell<T> cell) {
        final TreeCell<T> firstCell = getSkinEx().getVirtualFlowEx().getFirstVisibleCell();
        final TreeCell<T> lastCell = getSkinEx().getVirtualFlowEx().getLastVisibleCell();
        if (null == firstCell || null == lastCell) return -1;

        int nextRowIndex = -1;
        final int cellIndex = cell.getIndex();
        final int firstCellIndex = firstCell.getIndex();
        if (cellIndex == firstCellIndex || cellIndex == firstCellIndex + 1) {
            nextRowIndex = firstCellIndex - (firstCellIndex > 1 ? 2 : 1);
            if (nextRowIndex < 0) return -1;
        } else {
            final int lastCellIndex = lastCell.getIndex();
            if (cellIndex == lastCellIndex || cellIndex == lastCellIndex - 1) {
                nextRowIndex = lastCellIndex + 1;
                if (nextRowIndex >= getSkinEx().getVirtualFlowEx().getCellCount()) return -1;
                nextRowIndex = firstCellIndex + 2;
            }
        }
        return nextRowIndex;
    }

    public TreeViewEx<T> setEnterOrDoubleClickAction(BiConsumer<InputEvent, TreeItem<T>> enterOrDoubleClickAction) {
        this.enterOrDoubleClickAction = enterOrDoubleClickAction;
        return this;
    }

    private void handleOnKeyReleased(KeyEvent event) {
        if (event.getCode() == KeyCode.ESCAPE) {
            this.getSelectionModel().clearSelection();
            event.consume();
            return;
        }
//        if (event.getCode() != KeyCode.ENTER)
//            return;
//        final TreeItem<T> treeItem = this.getSelectionModel().getSelectedItem();
//        if (null == treeItem)
//            return;
//        if (!treeItem.isLeaf()) {
//            treeItem.setExpanded(!treeItem.isExpanded());
//            event.consume();
//            return;
//        }
//
//        handleOnKeyReleasedImpl(event, treeItem);
    }

//    protected void handleOnKeyReleasedImpl(KeyEvent event, TreeItem<T> treeItem) {
//        if (event.getCode() == KeyCode.ENTER) {
//            handleOnEnterOrDoubleClicked(event, treeItem);
//            event.consume();
//        }
//    }

    private void handleOnMouseReleased(MouseEvent event) {
        if (event.getButton() == MouseButton.MIDDLE) {
            this.getSelectionModel().clearSelection();
            event.consume();
            return;
        }
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

    protected void handleOnMouseReleasedImpl(MouseEvent event, TreeItem<T> treeItem) {
        if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() > 1) {
            handleOnEnterOrDoubleClicked(event, treeItem);
            event.consume();
        }
    }

    protected void handleOnEnterOrDoubleClicked(InputEvent event, TreeItem<T> treeItem) {
        if (null != enterOrDoubleClickAction)
            enterOrDoubleClickAction.accept(event, treeItem);
    }

    protected void handleOnContextMenuRequested() {
    }
}
