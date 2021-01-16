package org.appxi.javafx.control.skin;

import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.scene.control.skin.TreeViewSkin;
import javafx.scene.control.skin.VirtualFlow;

public class TreeViewSkinEx<T> extends TreeViewSkin<T> {
    /**
     * Creates a new TreeViewSkin instance, installing the necessary child
     * nodes into the Control {@link Control#getChildren() children} list, as
     * well as the necessary input mappings for handling key, mouse, etc events.
     *
     * @param control The control that this skin should be installed onto.
     */
    public TreeViewSkinEx(TreeView<T> control) {
        super(control);
    }

    /**
     * Get the virtualized container.
     * Subclasses can invoke this method to get the VirtualFlow instance.
     *
     * @return the virtualized container
     * @since 10
     */
    public final VirtualFlow<TreeCell<T>> getVirtualFlowEx() {
        return super.getVirtualFlow();
    }
}
