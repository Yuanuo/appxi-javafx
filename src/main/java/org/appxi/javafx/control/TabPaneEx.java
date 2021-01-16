package org.appxi.javafx.control;

import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

public class TabPaneEx extends TabPane {
    private final ListChangeListener<Tab> tabsChangeListener;

    public TabPaneEx() {
        tabsChangeListener = c -> this.lookupAll(".tab-header-area .headers-region .tab .tab-container .tab-close-button")
                .forEach(btn -> {
                    final EventHandler<? super MouseEvent> handler = btn.getOnMousePressed();
                    if (null == handler) return;
                    btn.setOnMousePressed(null);
                    btn.setOnMouseReleased(msevt -> {
                        if (msevt.getButton() != MouseButton.PRIMARY) return;
                        if (btn == msevt.getPickResult().getIntersectedNode())
                            handler.handle(msevt);
                    });
                });
        this.skinProperty().addListener((o, ov, nv) -> {
            this.getTabs().removeListener(tabsChangeListener);
            this.getTabs().addListener(tabsChangeListener);
            tabsChangeListener.onChanged(null);
        });
    }
}
