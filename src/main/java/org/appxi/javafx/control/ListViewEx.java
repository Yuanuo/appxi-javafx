package org.appxi.javafx.control;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import org.appxi.javafx.control.skin.ListViewSkinEx;

public abstract class ListViewEx<T> extends ListView<T> {
    public ListViewEx() {
        super();
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
}
