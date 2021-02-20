package org.appxi.javafx.control.skin;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.skin.ListViewSkin;
import javafx.scene.control.skin.VirtualFlow;

public class ListViewSkinEx<T> extends ListViewSkin<T> {
    /**
     * Creates a new ListViewSkin instance, installing the necessary child
     * nodes into the Control {@link Control#getChildren() children} list, as
     * well as the necessary input mappings for handling key, mouse, etc events.
     *
     * @param control The control that this skin should be installed onto.
     */
    public ListViewSkinEx(ListView<T> control) {
        super(control);
    }

    /**
     * Get the virtualized container.
     * Subclasses can invoke this method to get the VirtualFlow instance.
     *
     * @return the virtualized container
     * @since 10
     */
    public final VirtualFlow<ListCell<T>> getVirtualFlowEx() {
        return super.getVirtualFlow();
    }
}
