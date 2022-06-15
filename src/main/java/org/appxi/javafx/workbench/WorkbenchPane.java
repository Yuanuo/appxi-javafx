package org.appxi.javafx.workbench;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.HPos;
import javafx.geometry.Orientation;
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
import javafx.scene.layout.Pane;
import org.appxi.holder.RawHolder;
import org.appxi.javafx.control.TabPaneEx;
import org.appxi.javafx.control.ToolBarEx;
import org.appxi.javafx.helper.FxHelper;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class WorkbenchPane extends BorderPane {
    private final Set<String> cachedVisitedPart = new HashSet<>();
    private final RawHolder<WorkbenchPart> markedDefaultPart = new RawHolder<>();
    private final RawHolder<WorkbenchPart> markedClosingPart = new RawHolder<>();

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
            if (null != oldToggle && oldToggle.getUserData() instanceof WorkbenchPart.SideView part) {
                part.inactiveViewport();
            }
            if (null != newToggle && newToggle.getUserData() instanceof WorkbenchPart.SideView part) {
                // 由未选中变成选中时需要确保sideViews已显示
                if (null == oldToggle) {
                    this.rootViews.getItems().add(0, this.sideViews);
                    Platform.runLater(() -> this.rootViews.setDividerPosition(0, lastRootViewsDividerPosition));
                }
                // 显示已选中的视图
                this.sideViews.setCenter(part.getViewport());
                // 并触发事件
                part.activeViewport(ensureFirstTime(part));
            }
        });
        //
        mainViews.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
            // update app title
            if (application.title.isBound())
                application.title.unbind();
            if (null == newTab) {
                application.title.set(null);
            } else if (newTab.getUserData() instanceof WorkbenchPart.MainView part) {
                application.title.bind(part.appTitle());
            } else {
                application.title.set(newTab.getText());
            }

            // update old tab
            if (null != oldTab && oldTab.getUserData() instanceof WorkbenchPart.MainView part) {
                if (cachedVisitedPart.contains(part.id().get()) && markedClosingPart.value != part) {
                    part.inactiveViewport(false);
                } else {
                    markedClosingPart.value = null;
                    cachedVisitedPart.remove(part.id().get());
                }
            }

            // update new tab
            if (null != newTab && newTab.getUserData() instanceof WorkbenchPart.MainView part) {
                if (markedDefaultPart.value == part) {
                    markedDefaultPart.value = null;
                } else {
                    if (newTab.getContent() == null) // always lazy init
                        newTab.setContent(part.getViewport());
                    part.activeViewport(ensureFirstTime(part));
                }
            }
        });
    }

    public void initialize(List<WorkbenchPart> views) {
        final long st0 = System.currentTimeMillis();

        views.forEach(this::addWorkbenchPart);
        views.forEach(WorkbenchPart::postConstruct);

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

    public void addWorkbenchPart(WorkbenchPart part) {
        if (part instanceof WorkbenchPart.SideTool sideTool) {
            this.addWorkbenchPartAsSideTool(sideTool);
        } else if (part instanceof WorkbenchPart.SideView sideView) {
            this.addWorkbenchPartAsSideView(sideView);
        } else if (part instanceof WorkbenchPart.MainView mainView) {
            this.addWorkbenchPartAsMainView(mainView, false);
        }
    }

    public void addWorkbenchPartAsSideTool(WorkbenchPart.SideTool part) {
        final Button tool = new Button();
        addSideTool(tool, part);
        tool.setOnAction(event -> FxHelper.runLater(() -> part.activeViewport(ensureFirstTime(part))));
    }

    public void addWorkbenchPartAsSideView(WorkbenchPart.SideView part) {
        final ToggleButton tool = new ToggleButton();
        addSideTool(tool, part);
        tool.setToggleGroup(sideToolsGroup);
    }

    public void addWorkbenchPartAsMainView(WorkbenchPart.MainView part, boolean addToEnd) {
        final Tab tool = new Tab();
        tool.idProperty().bind(part.id());
        tool.setUserData(part);
        tool.textProperty().bind(part.title());

        final Tooltip tooltip = new Tooltip();
        tooltip.textProperty().bind(part.tooltip());
        tool.setTooltip(tooltip);

        tool.graphicProperty().bind(part.graphic());

        tool.setOnCloseRequest(event -> {
            if (event.getTarget() instanceof Tab t
                    && t.getUserData() instanceof WorkbenchPart.MainView c
                    && cachedVisitedPart.contains(c.id().get())) {
                try {
                    c.inactiveViewport(true);
                    markedClosingPart.value = part;
                } catch (Exception ignore) {
                    event.consume();
                }
            }
        });
        if (this.mainViews.getTabs().isEmpty()) {
            markedDefaultPart.value = part;
        }

        final int addToIdx = addToEnd ? this.mainViews.getTabs().size() : this.mainViews.getSelectionModel().getSelectedIndex() + 1;
        this.mainViews.getTabs().add(addToIdx, tool);
    }

    private void addSideTool(ButtonBase tool, WorkbenchPart part) {
        tool.idProperty().bind(part.id());
        tool.setUserData(part);
        tool.setContentDisplay(ContentDisplay.TOP);
        // FIXME 是否通过配置允许显示文字标签？
//        tool.textProperty().bind(part.viewTitle);

        final Tooltip tooltip = new Tooltip();
        tooltip.textProperty().bind(part.tooltip());
        tool.setTooltip(tooltip);

        tool.graphicProperty().bind(part.graphic());

        boolean alignTop = true;
        if (part instanceof WorkbenchPart.SideTool sideTool) {
            alignTop = sideTool.sideToolAlignTop();
        } else if (part instanceof WorkbenchPart.SideView sideView) {
            alignTop = sideView.sideToolAlignTop();
        }
        this.sideTools.addAligned(alignTop ? HPos.LEFT : HPos.RIGHT, tool);
    }

    private boolean ensureFirstTime(WorkbenchPart part) {
        if (cachedVisitedPart.contains(part.id().get()))
            return false;
        cachedVisitedPart.add(part.id().get());
        return true;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public ToggleButton getSelectedSideTool() {
        return !isSideViewsVisible() ? null : (ToggleButton) sideToolsGroup.getSelectedToggle();
    }

    public Pane getSelectedSideView() {
        final ToggleButton sideTool = getSelectedSideTool();
        if (null == sideTool)
            return null;

        final WorkbenchPart.SideView part = (WorkbenchPart.SideView) sideTool.getUserData();
        return part.getViewport();
    }

    public final WorkbenchPart.SideView getSelectedSideViewPart() {
        final ToggleButton tool = this.getSelectedSideTool();
        return null != tool ? (WorkbenchPart.SideView) tool.getUserData() : null;
    }

    public Tab getSelectedMainViewTab() {
        return this.mainViews.getSelectionModel().getSelectedItem();
    }

    public Node getSelectedMainViewNode() {
        final Tab tab = this.mainViews.getSelectionModel().getSelectedItem();
        return null != tab ? tab.getContent() : null;
    }

    public final WorkbenchPart.MainView getSelectedMainViewPart() {
        final Tab tool = this.getSelectedMainViewTab();
        return null != tool ? (WorkbenchPart.MainView) tool.getUserData() : null;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void selectSideTool(String id) {
        final ObservableList<Node> items = this.sideTools.getAlignedItems();
        if (items.isEmpty()) return;

        ButtonBase tool = null == id ? null : findSideTool(id);
        if (null == tool && items.get(0) instanceof ButtonBase button) tool = button;
        if (null == tool) return;
        if (tool == sideToolsGroup.getSelectedToggle()) return; // already selected
        tool.fire();
    }

    public void selectMainView(String id) {
        Tab tab = null == id ? this.mainViews.getSelectionModel().getSelectedItem() : findMainView(id);
        if (null == tab) return;
        if (tab.getUserData() instanceof WorkbenchPart.MainView part) {
            if (markedDefaultPart.value == part) {
                markedDefaultPart.value = null;
            }
            if (tab.isSelected() && (!cachedVisitedPart.contains(part.id().get()) || tab.getContent() == null)) {
                this.mainViews.getSelectionModel().clearSelection();
            }
        }
        this.mainViews.getSelectionModel().select(tab);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public boolean existsSideView(String id) {
        return null != findSideTool(id);
    }

    public boolean existsMainView(String id) {
        return null != findMainView(id);
    }

    public ButtonBase findSideTool(String id) {
        final FilteredList<Node> list = this.sideTools.getAlignedItems().filtered(v -> Objects.equals(v.getId(), id));
        return !list.isEmpty() ? (ButtonBase) list.get(0) : null;
    }

    public final WorkbenchPart.SideView findSideViewPart(String id) {
        final ButtonBase tool = findSideTool(id);
        return null != tool && tool.getUserData() instanceof WorkbenchPart.SideView part ? part : null;
    }

    public Tab findMainView(String id) {
        return this.mainViews.findById(id);
    }

    public final WorkbenchPart.MainView findMainViewPart(String id) {
        final Tab tool = this.findMainView(id);
        return null != tool && tool.getUserData() instanceof WorkbenchPart.MainView part ? part : null;
    }
}
