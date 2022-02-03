package org.appxi.javafx.workbench;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import org.appxi.javafx.control.TabPaneEx;
import org.appxi.javafx.control.ToolBarEx;
import org.appxi.javafx.helper.FxHelper;
import org.appxi.javafx.workbench.views.WorkbenchMainViewController;
import org.appxi.javafx.workbench.views.WorkbenchSideToolController;
import org.appxi.javafx.workbench.views.WorkbenchSideViewController;
import org.appxi.util.ext.Attributes;

import java.util.List;
import java.util.Objects;

public class WorkbenchPane extends BorderPane {
    private static final Object AK_DEFAULT = new Object();
    private static final Object AK_VISITED = new Object();
    private static final Object AK_CLOSING = new Object();

    private final ToggleGroup sideToolsGroup = new ToggleGroup();
    protected final ToolBarEx sideTools;
    protected final SplitPane rootViews;
    protected final BorderPane sideViews;
    public final TabPaneEx mainViews;

    private double lastRootViewsDividerPosition = 0.2;

    public final WorkbenchApp application;

    public WorkbenchPane(WorkbenchApp application) {
        super();
        this.application = application;
        this.getStyleClass().add("workbench");
        //
        sideTools = new ToolBarEx(Orientation.VERTICAL);
        sideTools.getStyleClass().add("side-tools");

        rootViews = new SplitPane();
        rootViews.getStyleClass().add("root-views");
        rootViews.setDividerPosition(0, 0.2);

        sideViews = new BorderPane();
        sideViews.getStyleClass().add("side-views");
        SplitPane.setResizableWithParent(sideViews, false);

        mainViews = new TabPaneEx();
        mainViews.getStyleClass().add("main-views");
        mainViews.setTabClosingPolicy(TabClosingPolicy.ALL_TABS);
        rootViews.getItems().add(mainViews);

        this.setLeft(sideTools);
        this.setCenter(rootViews);
        //
        sideToolsGroup.selectedToggleProperty().addListener((observable, oldToggle, newToggle) -> {
            // 如果sideViews在显示时再次点击已选中的按钮则隐藏sideViews
            if (null == newToggle) {
                // 由选中变成未选中时需要确保sideViews已隐藏
                lastRootViewsDividerPosition = rootViews.getDividerPositions()[0];
                this.rootViews.getItems().remove(this.sideViews);
            }
            // 正常切换sideViews显示时会触发的操作
            if (null != oldToggle && oldToggle.getUserData() instanceof WorkbenchSideViewController controller) {
                controller.onViewportHiding();
            }
            if (null != newToggle && newToggle.getUserData() instanceof WorkbenchViewController controller) {
                // 由未选中变成选中时需要确保sideViews已显示
                if (null == oldToggle) {
                    this.rootViews.getItems().add(0, this.sideViews);
                    Platform.runLater(() -> this.rootViews.setDividerPosition(0, lastRootViewsDividerPosition));
                }
                // 显示已选中的视图
                this.sideViews.setCenter(controller.getViewport());
                // 并触发事件
                controller.onViewportShowing(ensureFirstTime(controller));
            }
        });
        //
        mainViews.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
            // update app title
            if (application.title.isBound())
                application.title.unbind();
            if (null == newTab) {
                application.title.set(null);
            } else if (newTab.getUserData() instanceof WorkbenchMainViewController controller) {
                application.title.bind(controller.appTitle);
            } else {
                application.title.set(newTab.getText());
            }

            // update old tab
            if (null != oldTab && oldTab.getUserData() instanceof WorkbenchMainViewController controller) {
                if (controller.hasAttr(AK_VISITED) && !controller.hasAttr(AK_CLOSING))
                    controller.onViewportHiding();
            }

            // update new tab
            if (null != newTab && newTab.getUserData() instanceof WorkbenchMainViewController controller) {
                if (controller.hasAttr(AK_DEFAULT)) {
                    controller.removeAttr(AK_DEFAULT);
                } else {
                    if (newTab.getContent() == null) // always lazy init
                        newTab.setContent(controller.getViewport());
                    controller.onViewportShowing(ensureFirstTime(controller));
                }
            }
        });
    }

    public void initialize(List<WorkbenchViewController> views) {
        final long st0 = System.currentTimeMillis();

        views.forEach(this::addWorkbenchView);
        views.forEach(WorkbenchViewController::initialize);

        application.logger.warn("load views used time: " + (System.currentTimeMillis() - st0));
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
        if (controller instanceof WorkbenchSideToolController sideTool) {
            this.addWorkbenchViewAsSideTool(sideTool);
        } else if (controller instanceof WorkbenchSideViewController sideView) {
            this.addWorkbenchViewAsSideView(sideView);
        } else if (controller instanceof WorkbenchMainViewController mainView) {
            this.addWorkbenchViewAsMainView(mainView, false);
        }
    }

    public void addWorkbenchViewAsSideTool(WorkbenchSideToolController controller) {
        final Button tool = new Button();
        addSideTool(tool, controller, Pos.CENTER_RIGHT);
        tool.setOnAction(event -> FxHelper.runLater(() -> controller.onViewportShowing(ensureFirstTime(controller))));
    }

    public void addWorkbenchViewAsSideView(WorkbenchSideViewController controller) {
        final ToggleButton tool = new ToggleButton();
        addSideTool(tool, controller, Pos.CENTER_LEFT);
        tool.setToggleGroup(sideToolsGroup);
    }

    public void addWorkbenchViewAsMainView(WorkbenchMainViewController controller, boolean addToEnd) {
        final Tab tool = new Tab();
        tool.idProperty().bind(controller.id);
        tool.setUserData(controller);
        tool.textProperty().bind(controller.title);

        final Tooltip tooltip = new Tooltip();
        tooltip.textProperty().bind(controller.tooltip);
        tool.setTooltip(tooltip);

        tool.graphicProperty().bind(controller.graphic);

        tool.setOnCloseRequest(event -> {
            if (event.getTarget() instanceof Tab t && t.getUserData() instanceof WorkbenchMainViewController c && c.hasAttr(AK_VISITED)) {
                c.onViewportClosing(event, t.isSelected());
                if (!event.isConsumed())
                    c.attr(AK_CLOSING, true);
            }
        });
        if (this.mainViews.getTabs().isEmpty())
            controller.attr(AK_DEFAULT, true);

        final int addToIdx = addToEnd ? this.mainViews.getTabs().size() : this.mainViews.getSelectionModel().getSelectedIndex() + 1;
        this.mainViews.getTabs().add(addToIdx, tool);
    }

    private void addSideTool(ButtonBase tool, WorkbenchViewController controller, Pos pos) {
        tool.idProperty().bind(controller.id);
        tool.setUserData(controller);
        tool.setContentDisplay(ContentDisplay.TOP);
        // FIXME 是否通过配置允许显示文字标签？
//        tool.textProperty().bind(controller.viewTitle);

        final Tooltip tooltip = new Tooltip();
        tooltip.textProperty().bind(controller.tooltip);
        tool.setTooltip(tooltip);

        tool.graphicProperty().bind(controller.graphic);

        if (controller.hasAttr(Pos.class))
            pos = controller.attr(Pos.class);
        this.sideTools.addAligned(pos, tool);
    }

    private boolean ensureFirstTime(Attributes attrs) {
        if (attrs.hasAttr(AK_VISITED))
            return false;
        attrs.attr(AK_VISITED, true);
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

    public final WorkbenchSideViewController getSelectedSideViewController() {
        final ToggleButton tool = this.getSelectedSideTool();
        return null != tool ? (WorkbenchSideViewController) tool.getUserData() : null;
    }

    public Tab getSelectedMainViewTab() {
        return this.mainViews.getSelectionModel().getSelectedItem();
    }

    public Node getSelectedMainViewNode() {
        final Tab tab = this.mainViews.getSelectionModel().getSelectedItem();
        return null != tab ? tab.getContent() : null;
    }

    public final WorkbenchMainViewController getSelectedMainViewController() {
        final Tab tool = this.getSelectedMainViewTab();
        return null != tool ? (WorkbenchMainViewController) tool.getUserData() : null;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void selectSideTool(String id) {
        final ObservableList<Node> items = this.sideTools.getAlignedItems();
        if (items.isEmpty()) return;

        ButtonBase tool = null == id ? null : findSideToolHandler(id);
        if (null == tool && items.get(0) instanceof ButtonBase button) tool = button;
        if (null == tool) return;
        if (tool == sideToolsGroup.getSelectedToggle()) return; // already selected
        tool.fire();
    }

    public void selectMainView(String id) {
        Tab tab = null == id ? this.mainViews.getSelectionModel().getSelectedItem() : findMainViewHandler(id);
        if (null == tab) return;
        if (tab.getUserData() instanceof WorkbenchMainViewController controller) {
            if (controller.hasAttr(AK_DEFAULT)) controller.removeAttr(AK_DEFAULT);
            if (tab.isSelected() && (!controller.hasAttr(AK_VISITED) || tab.getContent() == null))
                this.mainViews.getSelectionModel().clearSelection();
        }
        this.mainViews.getSelectionModel().select(tab);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public boolean existsSideView(String id) {
        return null != findSideToolHandler(id);
    }

    public boolean existsMainView(String id) {
        return null != findMainViewHandler(id);
    }

    public ButtonBase findSideToolHandler(String id) {
        final FilteredList<Node> list = this.sideTools.getAlignedItems().filtered(v -> Objects.equals(v.getId(), id));
        return !list.isEmpty() ? (ButtonBase) list.get(0) : null;
    }

    public final WorkbenchSideViewController findSideViewController(String id) {
        final ButtonBase tool = findSideToolHandler(id);
        return null != tool && tool.getUserData() instanceof WorkbenchSideViewController controller ? controller : null;
    }

    public Tab findMainViewHandler(String id) {
        return this.mainViews.findById(id);
    }

    public final WorkbenchMainViewController findMainViewController(String id) {
        final Tab tool = this.findMainViewHandler(id);
        return null != tool && tool.getUserData() instanceof WorkbenchMainViewController controller ? controller : null;
    }
}
