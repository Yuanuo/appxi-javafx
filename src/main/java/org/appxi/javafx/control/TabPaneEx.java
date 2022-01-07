package org.appxi.javafx.control;

import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

public class TabPaneEx extends TabPane {
    private static final Object AK_HACKED = new Object();
    private static final Object AK_HANDLE = new Object();
    private final ListChangeListener<Tab> tabsChangeListener;

    public TabPaneEx() {
        // 此处代码用于Hack默认的关闭触发事件
        // 由于默认时只要在关闭按钮/图标上按下鼠标（Pressed）即关闭，而非松开鼠标（Released），
        // 为防止误触，此处将变成只有在关闭按钮/图标上松开鼠标才响应关闭事件，否则不做任何操作。
        tabsChangeListener = change -> {
            if (null == change || change.next() && change.wasAdded()) {
                this.lookupAll(".tab-header-area .headers-region .tab .tab-container .tab-close-button").forEach(btn -> {
                    if (btn.getProperties().containsKey(AK_HACKED)) return;

                    final EventHandler<? super MouseEvent> handler = btn.getOnMousePressed();
                    if (null == handler)
                        return;
                    btn.getProperties().put(AK_HACKED, true);
                    if (!btn.getProperties().containsKey(AK_HANDLE))
                        btn.getProperties().put(AK_HANDLE, handler);

                    btn.setOnMousePressed(evt -> {
                        if (evt.getButton() != MouseButton.PRIMARY) return;
                        if (btn != evt.getPickResult().getIntersectedNode()) return;
                        evt.consume();
                    });
                    btn.setOnMouseReleased(evt -> {
                        if (evt.getButton() != MouseButton.PRIMARY) return;
                        if (btn != evt.getPickResult().getIntersectedNode()) return;
                        Object originHandle = btn.getProperties().get(AK_HANDLE);
                        if (null != originHandle)
                            ((EventHandler<? super MouseEvent>) originHandle).handle(evt);
                    });
                });
            }
        };
        this.skinProperty().addListener((o, ov, nv) -> {
            this.getTabs().removeListener(tabsChangeListener);
            this.getTabs().addListener(tabsChangeListener);
            tabsChangeListener.onChanged(null);
        });
    }
}
