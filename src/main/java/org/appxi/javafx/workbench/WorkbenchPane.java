package org.appxi.javafx.workbench;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.appxi.javafx.control.ToolBarEx;
import org.appxi.javafx.control.StackPaneEx;
import org.appxi.javafx.control.TabPaneEx;
import org.appxi.util.StringHelper;
import org.appxi.util.ext.Attributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class WorkbenchPane extends StackPaneEx {
    private static final Object AK_FIRST_TIME = new Object();

    private final ToggleGroup sideToolsGroup = new ToggleGroup();
    protected final ToolBarEx sideTools;
    protected final SplitPane rootViews;
    protected final StackPane sideViews;
    protected final TabPane mainViews;

    private double lastRootViewsDividerPosition = 0.2;

    public WorkbenchPane() {
        super();
        this.getStyleClass().add("workbench");
        //
        sideTools = new ToolBarEx(Orientation.VERTICAL);
        sideTools.getStyleClass().add("side-tools");

        rootViews = new SplitPane();
        rootViews.getStyleClass().add("root");
        HBox.setHgrow(rootViews, Priority.ALWAYS);

        sideViews = new StackPane();
        sideViews.getStyleClass().add("side-views");

        mainViews = new TabPaneEx();
        mainViews.getStyleClass().add("main-views");
        mainViews.setTabClosingPolicy(TabClosingPolicy.ALL_TABS);
        rootViews.getItems().add(mainViews);

        final HBox mainContainer = new HBox(sideTools, rootViews);
        VBox.setVgrow(mainContainer, Priority.ALWAYS);
        //
        this.getChildren().add(mainContainer);

        sideToolsGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            final ToggleButton lastSideTool = (ToggleButton) oldValue;
            final ToggleButton currSideTool = (ToggleButton) newValue;

            // 如果sideViews在显示时再次点击已选中的按钮则隐藏sideViews
            if (null == currSideTool) {
                // 由选中变成未选中时需要确保sideViews已隐藏
                lastRootViewsDividerPosition = rootViews.getDividerPositions()[0];
                this.rootViews.getItems().remove(this.sideViews);
            }
            // 正常切换sideViews显示时会触发的操作
            if (null != lastSideTool) {
                final WorkbenchViewController lastController = (WorkbenchViewController) lastSideTool.getUserData();
                lastController.onViewportHide(true);
            }
            if (null != currSideTool) {
                final WorkbenchViewController currController = (WorkbenchViewController) currSideTool.getUserData();
                // 由未选中变成选中时需要确保sideViews已显示
                if (null == lastSideTool) {
                    this.rootViews.getItems().add(0, this.sideViews);
                    this.rootViews.setDividerPosition(0, lastRootViewsDividerPosition);
                }
                // 显示已选中的视图
                this.sideViews.getChildren().setAll(currController.<Node>getViewport());
                // 并触发事件
                currController.onViewportShow(ensureFirstTime(currController));
            }
        });
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public final boolean isSideViewsVisible() {
        return rootViews.getItems().contains(sideViews);
    }

    public final double getRootViewsDividerPosition() {
        return this.isSideViewsVisible() ? rootViews.getDividerPositions()[0] : this.lastRootViewsDividerPosition;
    }

    public final void setRootViewsDividerPosition(double dividerPosition) {
        if (this.isSideViewsVisible())
            rootViews.setDividerPosition(0, dividerPosition);
        else this.lastRootViewsDividerPosition = dividerPosition;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void addWorkbenchView(WorkbenchViewController controller) {
        final Boolean placeInSideViews = controller.isPlaceInSideViews();
        if (null == placeInSideViews) {
            addWorkbenchViewAsSideTool(controller);
        } else if (placeInSideViews) {
            addWorkbenchViewAsSideView(controller);
        } else {
            addWorkbenchViewAsMainView(controller, false);
        }
    }

    public void addWorkbenchViewAsSideTool(WorkbenchViewController controller) {
        final Button tool = new Button();
        addSideTool(tool, controller, Pos.CENTER_RIGHT);
        tool.setOnAction(event -> Platform.runLater(() -> controller.onViewportShow(ensureFirstTime(controller))));
    }

    public void addWorkbenchViewAsSideView(WorkbenchViewController controller) {
        final ToggleButton tool = new ToggleButton();
        addSideTool(tool, controller, Pos.CENTER_LEFT);
        tool.setToggleGroup(sideToolsGroup);
    }

    public void addWorkbenchViewAsMainView(WorkbenchViewController controller, boolean addToEnd) {
        final Tab tool = new Tab();
        tool.setId(controller.viewId);
        tool.setUserData(controller);
        if (StringHelper.isNotBlank(controller.viewName)) {
            tool.setText(StringHelper.trimBytes(controller.viewName, 24));
            tool.setTooltip(new Tooltip(controller.viewName));
        }

        final Node iconGraphic = controller.createToolIconGraphic(false);
        if (null != iconGraphic)
            tool.setGraphic(iconGraphic);

        if (addToEnd) {
            this.mainViews.getTabs().add(tool);
        } else {
            final int addIdx = this.mainViews.getSelectionModel().getSelectedIndex() + 1;
            this.mainViews.getTabs().add(addIdx, tool);
        }
        // 先添加到tabs后再绑定事件，避免添加时触发第一次加载事件
        tool.setOnClosed(event -> {
            // 只有已经触发过第一次加载事件的视图才处理此操作
            if (controller.hasAttr(AK_FIRST_TIME))
                controller.onViewportHide(false);
        });
        tool.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                Platform.runLater(() -> {
                    final boolean firstTime = ensureFirstTime(controller);
                    if (firstTime) // always lazy init
                        tool.setContent(controller.getViewport());
                    controller.onViewportShow(firstTime);
                });
            } else if (oldValue && controller.hasAttr(AK_FIRST_TIME)) {
                controller.onViewportHide(true);
            }
        });
    }

    public void addWorkbenchViewAsMainViewWithSideTool(WorkbenchViewController controller) {
        final Button tool = new Button();
        addSideTool(tool, controller, Pos.CENTER_RIGHT);
        tool.setOnAction(event -> {
            final Tab tab = findMainViewTab(controller.viewId);
            if (null != tab) {
                selectMainView(tab);
                return;
            }
            addWorkbenchViewAsMainView(controller, false);
            selectMainView(controller.viewId);
        });
    }

    private void addSideTool(ButtonBase tool, WorkbenchViewController controller, Pos pos) {
        tool.setId(controller.viewId);
        // FIXME 是否通过配置允许显示文字标签？
//        tool.setText(controller.viewName);
        tool.setUserData(controller);
        tool.setContentDisplay(ContentDisplay.TOP);

        if (StringHelper.isNotBlank(controller.viewName))
            tool.setTooltip(new Tooltip(controller.viewName));

        final Node iconGraphic = controller.createToolIconGraphic(false);
        if (null != iconGraphic)
            tool.setGraphic(iconGraphic);

        this.sideTools.addAligned(pos, tool);
    }

    private boolean ensureFirstTime(Attributes attrs) {
        if (attrs.hasAttr(AK_FIRST_TIME))
            return false;
        attrs.attr(AK_FIRST_TIME, true);
        return true;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public ToggleButton getSelectedSideTool() {
        return !isSideViewsVisible() ? null : (ToggleButton) sideToolsGroup.getSelectedToggle();
    }

    public Node getSelectedSideView() {
        final ToggleButton sideTool = getSelectedSideTool();
        if (null == sideTool)
            return null;

        final WorkbenchViewController controller = (WorkbenchViewController) sideTool.getUserData();
        final Object viewObj = controller.getViewport();
        return viewObj instanceof Node ? (Node) viewObj : null;
    }

    public final WorkbenchViewController getSelectedSideViewController() {
        final ToggleButton tool = this.getSelectedSideTool();
        return null != tool ? (WorkbenchViewController) tool.getUserData() : null;
    }

    public Tab getSelectedMainViewTab() {
        return this.mainViews.getSelectionModel().getSelectedItem();
    }

    public Node getSelectedMainViewNode() {
        final Tab tab = this.mainViews.getSelectionModel().getSelectedItem();
        return null != tab ? tab.getContent() : null;
    }

    public final WorkbenchViewController getSelectedMainViewController() {
        final Tab tool = this.getSelectedMainViewTab();
        return null != tool ? (WorkbenchViewController) tool.getUserData() : null;
    }

    public List<Tab> getMainViewsTabs() {
        return mainViews.getTabs();
    }

    public List<Node> getMainViewsNodes() {
        final List<Node> result = new ArrayList<>();
        mainViews.getTabs().forEach(tab -> result.add(tab.getContent()));
        return result;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void selectSideTool(String id) {
        final ObservableList<Node> items = this.sideTools.getAlignedItems();
        if (items.isEmpty())
            return;

        ButtonBase tool = null == id ? null : findSideTool(id);
        if (null == tool)
            tool = (ButtonBase) items.get(0);
        if (null == tool)
            return;
        if (tool == sideToolsGroup.getSelectedToggle())
            return;// already selected
        tool.fire();
    }

    public void selectMainView(String id) {
        this.selectMainView(findMainViewTab(id));
    }

    public void selectMainView(Tab tab) {
        if (null == tab)
            return;
        if (tab.isSelected()) {
            final WorkbenchViewController controller = (WorkbenchViewController) tab.getUserData();
            if (!controller.hasAttr(AK_FIRST_TIME)) {
                Platform.runLater(() -> {
                    final boolean firstTime = ensureFirstTime(controller);
                    if (firstTime) // always lazy init
                        tab.setContent(controller.getViewport());
                    controller.onViewportShow(firstTime);
                });
            }
            return;
        }
        this.mainViews.getSelectionModel().select(tab);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public boolean existsSideView(String id) {
        return null != findSideTool(id);
    }

    public boolean existsMainView(String id) {
        return null != findMainViewTab(id);
    }

    public ButtonBase findSideTool(String id) {
        final FilteredList<Node> list = this.sideTools.getAlignedItems().filtered(v -> Objects.equals(v.getId(), id));
        return !list.isEmpty() ? (ButtonBase) list.get(0) : null;
    }

    public Node findSideView(String id) {
        final WorkbenchViewController controller = this.findSideViewController(id);
        return null != controller ? controller.getViewport() : null;
    }

    public final WorkbenchViewController findSideViewController(String id) {
        final Object sideTool = findSideTool(id);
        if (null == sideTool)
            return null;
        if (sideTool instanceof ToggleButton) {
            final ToggleButton btn = (ToggleButton) sideTool;
            return (WorkbenchViewController) btn.getUserData();
        }
        return null;
    }

    public Tab findMainViewTab(String id) {
        final FilteredList<Tab> list = this.mainViews.getTabs().filtered((v -> Objects.equals(v.getId(), id)));
        return !list.isEmpty() ? list.get(0) : null;
    }

    public Node findMainViewNode(String id) {
        final Tab tool = findMainViewTab(id);
        return null != tool ? tool.getContent() : null;
    }

    public final WorkbenchViewController findMainViewController(String id) {
        final Tab tool = this.findMainViewTab(id);
        return null != tool ? (WorkbenchViewController) tool.getUserData() : null;
    }
}
