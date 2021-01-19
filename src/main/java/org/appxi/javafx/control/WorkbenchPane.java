package org.appxi.javafx.control;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.appxi.util.StringHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class WorkbenchPane extends StackPaneEx {
    private static final Object KEY_VIEWPORT = new Object();
    private static final Object KEY_FIRSTIME = new Object();

    private final ToggleGroup worktoolsGroup = new ToggleGroup();
    private final SplitPane viewspane;
    private final StackPaneEx workviews;
    private final TabPane openviews;

    public final AlignedBar worktools;
    //    public final AlignedBox infotools;
    public final Label infoLabel;

    public WorkbenchPane() {
        super();

        workviews = new StackPaneEx();
        workviews.getStyleClass().add("workviews");

        openviews = new TabPaneEx();
        openviews.getStyleClass().add("openviews");
        openviews.setTabClosingPolicy(TabClosingPolicy.ALL_TABS);

        viewspane = new SplitPane(openviews);
        viewspane.getStyleClass().add("viewspane");
        HBox.setHgrow(viewspane, Priority.ALWAYS);

        worktools = new AlignedBar(Orientation.VERTICAL);
        worktools.getStyleClass().add("worktools");

        final HBox mainContainer = new HBox(worktools, viewspane);
        VBox.setVgrow(mainContainer, Priority.ALWAYS);

        //
        infoLabel = new Label("Ready");
        infoLabel.getStyleClass().add("info-label");

//        infotools = new AlignedBox();
//        infotools.getStyleClass().add("infotools");
//        infotools.addLeft(infoLabel);
        //
        this.getChildren().add(mainContainer);//new VBox() , infotools
        this.getStyleClass().add("workbench");
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public final boolean isWorkviewsVisible() {
        return viewspane.getItems().contains(workviews);
    }

    public final double getWorkviewsDividerPosition() {
        return this.isWorkviewsVisible() ? viewspane.getDividerPositions()[0] : this.lastWorkviewsDivider;
    }

    public final void setWorkviewsDividerPosition(double dividerPosition) {
        if (this.isWorkviewsVisible())
            viewspane.setDividerPosition(0, dividerPosition);
        else this.lastWorkviewsDivider = dividerPosition;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public Button addWorktool(String id, Label info, WorktoolListener listener) {
        final Button tool = new Button();
        addWorktool(tool, id, info, Pos.CENTER_RIGHT);
        if (null != listener)
            tool.setOnAction(event -> listener.onViewportSelected(ensureFirstTime(tool.getProperties())));
        return tool;
    }

    private void addWorktool(ButtonBase tool, String id, Label info, Pos defPos) {
        tool.setId(id);
        tool.setUserData(info.getUserData());
        tool.setContentDisplay(ContentDisplay.TOP);
//        if (StringHelper.isNotBlank(info.getText()))
//            tool.setText(StringHelper.trimBytes(info.getText(), 2, null));

        if (null != info.getTooltip())
            tool.setTooltip(info.getTooltip());
        else if (StringHelper.isNotBlank(info.getText()))
            tool.setTooltip(new Tooltip(info.getText()));

        if (null != info.getGraphic())
            tool.setGraphic(info.getGraphic());
        else tool.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.QUESTION));

        final Pos pos = info.getAlignment();
        this.worktools.addAligned(null == pos ? defPos : pos, tool);
    }

    public void addWorkview(String id, Label info, Node view, WorkpartListener listener) {
        final ToggleButton tool = new ToggleButton();
        addWorktool(tool, id, info, Pos.CENTER_LEFT);
        tool.setToggleGroup(worktoolsGroup);
        tool.setOnAction(this::handleWorktoolToggleAction);
        if (null != listener)
            tool.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue)
                    Platform.runLater(() -> listener.onViewportSelected(ensureFirstTime(tool.getProperties())));
                else if (oldValue) listener.onViewportUnselected();
            });
        tool.getProperties().put(KEY_VIEWPORT, view);
    }

    public void addOpenview(String id, Label info, Node view, OpenpartListener listener) {
        this.addOpenview(id, info, view, listener, false);
    }

    public void addOpenview(String id, Label info, Node view, OpenpartListener listener, boolean addToEnd) {
        final Tab tool = new Tab();
        tool.setId(id);
        tool.setContent(view);
        tool.setUserData(info.getUserData());
        tool.getProperties().put(OpenpartListener.class, listener);
        if (StringHelper.isNotBlank(info.getText()))
            tool.setText(StringHelper.trimBytes(info.getText(), 24));

        if (null != info.getTooltip())
            tool.setTooltip(info.getTooltip());
        else if (StringHelper.isNotBlank(info.getText()))
            tool.setTooltip(new Tooltip(info.getText()));

        if (null != info.getGraphic())
            tool.setGraphic(info.getGraphic());

        if (addToEnd)
            this.openviews.getTabs().add(tool);
        else {
            final int refIdx = this.openviews.getSelectionModel().getSelectedIndex();
            this.openviews.getTabs().add(refIdx + 1, tool);
        }

        if (null != listener) tool.setOnCloseRequest(listener::onViewportCloseRequest);
        if (null != listener) tool.setOnClosed(listener::onViewportClosed);
        if (null != listener)
            tool.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue)
                    Platform.runLater(() -> listener.onViewportSelected(ensureFirstTime(tool.getProperties())));
                else if (oldValue && tool.getProperties().containsKey(KEY_FIRSTIME)) listener.onViewportUnselected();
            });
    }

    public void addOpenviewWithWorktool(String id, Label info, Node view, OpenpartListener listener) {
        this.addOpenviewWithWorktool(id, info, view, listener, false);
    }

    public void addOpenviewWithWorktool(String id, Label info, Node view, OpenpartListener listener, boolean addToEnd) {
        final Button tool = addWorktool(id, info, null);
        tool.setOnAction(event -> {
            final Tab tab = findOpentool(id);
            if (null != tab) {
                selectOpenview(tab);
                return;
            }
            info.setGraphic(null);
            addOpenview(id, info, view, listener, addToEnd);
            selectOpenview(id);
        });
    }

    private boolean ensureFirstTime(Map<Object, Object> map) {
        if (map.containsKey(KEY_FIRSTIME))
            return false;
        map.put(KEY_FIRSTIME, true);
        return true;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private ToggleButton lastWorktoolToggle;
    private double lastWorkviewsDivider = 0.2;

    private void handleWorktoolToggleAction(ActionEvent event) {
        final ToggleButton currWorktoolToggle = (ToggleButton) event.getSource();
        final boolean workviewsVisible = isWorkviewsVisible();
        if (workviewsVisible && lastWorktoolToggle == currWorktoolToggle) {
            currWorktoolToggle.setSelected(false);
            lastWorkviewsDivider = viewspane.getDividerPositions()[0];
            this.viewspane.getItems().remove(this.workviews);
            return;
        }

        if (!workviewsVisible) {
            this.viewspane.getItems().add(0, this.workviews);
            this.viewspane.setDividerPosition(0, lastWorkviewsDivider);
        }

        if (lastWorktoolToggle == currWorktoolToggle)
            return;

        final Node view = (Node) currWorktoolToggle.getProperties().get(KEY_VIEWPORT);
        this.workviews.getChildren().setAll(view);

        // keep it for next action
        lastWorktoolToggle = currWorktoolToggle;
        lastWorktoolToggle.setSelected(true);
    }

    public ToggleButton selectedWorktool() {
        return !isWorkviewsVisible() ? null : lastWorktoolToggle;
    }

    public Node selectedWorkview() {
        if (!isWorkviewsVisible())
            return null;

        if (null == lastWorktoolToggle)
            return null;

        return (Node) lastWorktoolToggle.getProperties().get(KEY_VIEWPORT);
    }

    public Tab selectedOpentool() {
        return this.openviews.getSelectionModel().getSelectedItem();
    }

    public Node selectedOpenview() {
        final Tab tab = this.openviews.getSelectionModel().getSelectedItem();
        return null != tab ? tab.getContent() : null;
    }

    public void selectWorktool(String id) {
        final ObservableList<Node> items = this.worktools.getAlignedItems();
        if (items.isEmpty())
            return;

        ButtonBase tool = null == id ? null : findWorktool(id);
        if (null == tool)
            tool = (ButtonBase) items.get(0);
        if (null == tool)
            return;
        if (tool instanceof Button)
            tool.fire();
        else if (tool instanceof ToggleButton toggle) {
            toggle.setSelected(true);
            handleWorktoolToggleAction(new ActionEvent(toggle, null));
        }
    }

    public void selectOpenview(String id) {
        this.selectOpenview(findOpentool(id));
    }

    public void selectOpenview(Tab tab) {
        if (null == tab)
            return;
        if (tab.isSelected()) {
            if (!tab.getProperties().containsKey(KEY_FIRSTIME)) {
                final OpenpartListener listener = (OpenpartListener) tab.getProperties().get(OpenpartListener.class);
                final boolean firstTime = ensureFirstTime(tab.getProperties());
                if (null != listener) Platform.runLater(() -> listener.onViewportSelected(firstTime));
            }
            return;
        }
        this.openviews.getSelectionModel().select(tab);
    }

    public boolean existsWorkview(String id) {
        return null != findWorktool(id);
    }

    public boolean existsOpenview(String id) {
        return null != findOpentool(id);
    }

    public ButtonBase findWorktool(String id) {
        final FilteredList<Node> list = this.worktools.getAlignedItems().filtered(v -> Objects.equals(v.getId(), id));
        return !list.isEmpty() ? (ButtonBase) list.get(0) : null;
    }

    public Node findWorkview(String id) {
        final Object worktool = findWorktool(id);
        if (null == worktool)
            return null;
        if (worktool instanceof ToggleButton btn)
            return (Node) btn.getProperties().get(KEY_VIEWPORT);
        return null;
    }

    public Tab findOpentool(String id) {
        final FilteredList<Tab> list = this.openviews.getTabs().filtered((v -> Objects.equals(v.getId(), id)));
        return !list.isEmpty() ? list.get(0) : null;
    }

    public Node findOpenview(String id) {
        final Tab tool = findOpentool(id);
        return null != tool ? tool.getContent() : null;
    }

    public List<Tab> getOpentools() {
        return openviews.getTabs();
    }

    public List<Node> getOpenviews() {
        final List<Node> result = new ArrayList<>();
        openviews.getTabs().forEach(tab -> result.add(tab.getContent()));
        return result;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public interface WorktoolListener {
        default void onViewportSelected(boolean firstTime) {
        }
    }

    public interface WorkpartListener {
        default void onViewportSelected(boolean firstTime) {
        }

        default void onViewportUnselected() {
        }
    }

    public interface OpenpartListener {
        default void onViewportSelected(boolean firstTime) {
        }

        default void onViewportUnselected() {
        }

        default void onViewportCloseRequest(Event event) {
        }

        default void onViewportClosed(Event event) {
        }
    }
}
