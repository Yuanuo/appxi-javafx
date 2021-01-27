package org.appxi.javafx.workbench;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.appxi.javafx.control.AlignedBar;
import org.appxi.javafx.control.StackPaneEx;
import org.appxi.javafx.control.TabPaneEx;
import org.appxi.util.StringHelper;
import org.appxi.util.ext.Attributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class WorkbenchPane extends StackPaneEx {
    private static final Object AK_FIRST_TIME = new Object();

    private final ToggleGroup controlsGroup = new ToggleGroup();
    protected final AlignedBar sideTools;
    protected final SplitPane rootViews;
    protected final StackPane sideViews;
    protected final TabPane mainViews;

    public WorkbenchPane() {
        super();
        this.getStyleClass().add("workbench");
        //
        sideTools = new AlignedBar(Orientation.VERTICAL);
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
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public final boolean isSideViewsVisible() {
        return rootViews.getItems().contains(sideViews);
    }

    public final double getRootViewsDividerPosition() {
        return this.isSideViewsVisible() ? rootViews.getDividerPositions()[0] : this.lastRootViewsDivider;
    }

    public final void setRootViewsDividerPosition(double dividerPosition) {
        if (this.isSideViewsVisible())
            rootViews.setDividerPosition(0, dividerPosition);
        else this.lastRootViewsDivider = dividerPosition;
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
        tool.setOnAction(event -> Platform.runLater(() -> controller.showViewport(ensureFirstTime(controller))));
    }

    public void addWorkbenchViewAsSideView(WorkbenchViewController controller) {
        final ToggleButton tool = new ToggleButton();
        addSideTool(tool, controller, Pos.CENTER_LEFT);
        tool.setToggleGroup(controlsGroup);
        tool.setOnAction(this::handleSideToolAction);
        tool.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                Platform.runLater(() -> controller.showViewport(ensureFirstTime(controller)));
            } else if (oldValue) {
                controller.hideViewport(true);
            }
        });
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
        // bind event handles after first added to don't call showViewport at this time
        tool.setOnClosed(event -> controller.hideViewport(false));
        tool.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                Platform.runLater(() -> {
                    final boolean firstTime = ensureFirstTime(controller);
                    if (firstTime) // always lazy init
                        tool.setContent(controller.getViewport());
                    controller.showViewport(firstTime);
                });
            } else if (oldValue && controller.hasAttr(AK_FIRST_TIME)) {
                controller.hideViewport(true);
            }
        });
    }

    public void addWorkbenchViewAsMainViewAndSideTool(WorkbenchViewController controller) {
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

    private ToggleButton lastSideTool;
    private double lastRootViewsDivider = 0.2;

    private void handleSideToolAction(ActionEvent event) {
        final ToggleButton currSideTool = (ToggleButton) event.getSource();
        final boolean sideViewsVisible = isSideViewsVisible();
        if (sideViewsVisible && lastSideTool == currSideTool) {
            currSideTool.setSelected(false);
            lastRootViewsDivider = rootViews.getDividerPositions()[0];
            this.rootViews.getItems().remove(this.sideViews);
            return;
        }

        if (!sideViewsVisible) {
            this.rootViews.getItems().add(0, this.sideViews);
            this.rootViews.setDividerPosition(0, lastRootViewsDivider);
        }
        if (lastSideTool == currSideTool)
            return;

        WorkbenchViewController controller = (WorkbenchViewController) currSideTool.getUserData();
        final Node view = controller.getViewport();
        this.sideViews.getChildren().setAll(view);

        // keep it for next action
        lastSideTool = currSideTool;
        lastSideTool.setSelected(true);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public ToggleButton getSelectedSideTool() {
        return !isSideViewsVisible() ? null : lastSideTool;
    }

    public Node getSelectedSideView() {
        if (!isSideViewsVisible())
            return null;
        if (null == lastSideTool)
            return null;

        final WorkbenchViewController controller = (WorkbenchViewController) lastSideTool.getUserData();
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
        if (tool instanceof Button) {
            tool.fire();
        } else if (tool instanceof ToggleButton) {
            final ToggleButton toggle = (ToggleButton) tool;
            toggle.setSelected(true);
            handleSideToolAction(new ActionEvent(toggle, null));
        }
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
                    controller.showViewport(firstTime);
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
