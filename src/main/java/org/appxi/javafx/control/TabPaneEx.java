package org.appxi.javafx.control;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.appxi.javafx.helper.FxHelper;
import org.appxi.javafx.visual.MaterialIcon;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

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

        this.addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED, this::handleOnContextMenuRequested);
    }

    private void handleOnContextMenuRequested(ContextMenuEvent event) {
        final ObservableList<Tab> tabs = getTabs();
        if (tabs.isEmpty()) {
            setContextMenu(null);
            return;
        }

        final List<MenuItem> menuItems = new ArrayList<>(4);

        Tab targetTab = null;
        final Optional<Node> tabNode = FxHelper.filterParent(event.getPickResult().getIntersectedNode(), "tab");
        if (tabNode.isPresent() && getTabClosingPolicy() != TabClosingPolicy.UNAVAILABLE) {
            targetTab = findById(tabNode.get().getId());
            //
            final Tab clickedTab = targetTab;
            final int clickedTabIdx = tabs.indexOf(targetTab);
            //
            final MenuItem close = new MenuItem("关闭");
            close.setGraphic(MaterialIcon.CLOSE.graphic());
            close.setOnAction(e -> closeTabs(List.of(clickedTab)));
            menuItems.add(close);
            //
            final MenuItem closeAll = new MenuItem("关闭全部");
            closeAll.setGraphic(MaterialIcon.CLOSE.graphic());
            closeAll.setOnAction(e -> closeTabs(tabs));
            menuItems.add(closeAll);
            //
            final MenuItem closeOthers = new MenuItem("关闭其他");
            closeOthers.setGraphic(MaterialIcon.REMOVE.graphic());
            closeOthers.setDisable(tabs.size() == 1);
            closeOthers.setOnAction(e -> closeTabs(tabs.filtered(tab -> tab != clickedTab)));
            menuItems.add(closeOthers);
            //
            final MenuItem closeAllToLeft = new MenuItem("关闭左侧");
            closeAllToLeft.setGraphic(MaterialIcon.REMOVE.graphic());
            closeAllToLeft.setDisable(clickedTabIdx == 0);
            closeAllToLeft.setOnAction(e -> closeTabs(tabs.subList(0, clickedTabIdx)));
            menuItems.add(closeAllToLeft);
            //
            final MenuItem closeAllToRight = new MenuItem("关闭右侧");
            closeAllToRight.setGraphic(MaterialIcon.REMOVE.graphic());
            closeAllToRight.setDisable(clickedTabIdx == tabs.size() - 1);
            closeAllToRight.setOnAction(e -> closeTabs(tabs.subList(clickedTabIdx + 1, tabs.size())));
            menuItems.add(closeAllToRight);
        }

        final List<MenuItem> extMenuItems = handleOnContextMenuRequested(targetTab);
        if (null != extMenuItems && !extMenuItems.isEmpty()) {
            // ---
            menuItems.add(new SeparatorMenuItem());
            //
            menuItems.addAll(extMenuItems);
        }

        if (menuItems.isEmpty()) {
            setContextMenu(null);
            return;
        }
        setContextMenu(new ContextMenu(menuItems.toArray(new MenuItem[0])));
    }

    protected List<MenuItem> handleOnContextMenuRequested(Tab targetTab) {
        return List.of();
    }

    public final Tab findById(String tabId) {
        return this.getTabs().stream().filter(tab -> Objects.equals(tabId, tab.getId())).findFirst().orElse(null);
    }

    public final boolean canCloseTab(Tab tab) {
        if (!tab.isClosable()) return false;
        Event event = new Event(tab, tab, Tab.TAB_CLOSE_REQUEST_EVENT);
        Event.fireEvent(tab, event);
        return !event.isConsumed();
    }

    private void doCloseTab(Tab tab) {
        // only switch to another tab if the selected tab is the one we're closing
        int index = getTabs().indexOf(tab);
        if (index != -1) getTabs().remove(index);
        if (tab.getOnClosed() != null) Event.fireEvent(tab, new Event(Tab.CLOSED_EVENT));
    }

    public final void closeTabs(Tab... tabs) {
        this.closeTabs(List.of(tabs));
    }

    public final void closeTabs(List<Tab> tabs) {
        new ArrayList<>(tabs).stream().filter(this::canCloseTab).forEach(this::doCloseTab);
    }

    public final void closeTabs(Predicate<Tab> predicate) {
        closeTabs(getTabs().filtered(predicate));
    }

    public final void removeTabs(Tab... tabs) {
        getTabs().removeAll(tabs);
    }

    public final void removeTabs(Predicate<Tab> predicate) {
        getTabs().removeAll(getTabs().filtered(predicate));
    }
}
