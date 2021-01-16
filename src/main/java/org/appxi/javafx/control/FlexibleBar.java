package org.appxi.javafx.control;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ToolBar;
import javafx.scene.input.MouseButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FlexibleBar extends ToolBar {
    private static final Object KEY_TOOL = new Object();
    private static final Object KEY_GROUP = new Object();
    private static final Object KEY_TOOLS = new Object();
    private static final Object KEY_TOOL_STATE = new Object();

    public FlexibleBar() {
        super();
        this.getStyleClass().add("flexible-bar");
    }

    public FlexibleBar addTool(Node tool) {
        tool.getStyleClass().add("flexible-tool");
        tool.getProperties().put(KEY_TOOL, true);
        final int idx = findLastToolIndex();
        getItems().add(idx + 1, tool);
        return this;
    }

    public FlexibleBar addTools(Node tool, Node... tools) {
        tool.getProperties().put(KEY_GROUP, new Group());
        tool.getProperties().put(KEY_TOOL_STATE, false);
        this.addTool(tool);
        this.setTools(tool, tools);
        //
        if (tool instanceof ButtonBase btn)
            btn.setOnAction(event -> this.handleToolExpansion((Node) event.getSource()));
        else tool.setOnMouseReleased(event -> {
            if (event.getButton() != MouseButton.PRIMARY)
                return;
            if (event.getSource() != event.getPickResult().getIntersectedNode())
                return;
            this.handleToolExpansion((Node) event.getSource());
        });
        return this;
    }

    public boolean isToolExpanded(Node tool) {
        return (boolean) tool.getProperties().get(KEY_TOOL_STATE);
    }

    private void handleToolExpansion(Node tool) {
        if (isToolExpanded(tool)) {
            getItems().removeAll(getTools(tool));
            tool.getProperties().put(KEY_TOOL_STATE, false);
        } else {
            updateToolExpansion(tool, null);
            tool.getProperties().put(KEY_TOOL_STATE, true);
        }
    }

    private void updateToolExpansion(Node tool, List<Node> exists) {
        int idx = -1;
        final List<Node> items = getItems();
        if (null != exists && !exists.isEmpty()) {
            idx = items.indexOf(exists.get(0));
            getItems().removeAll(exists);
        }
        if (idx == -1) idx = items.size();

        final List<Node> tools = getTools(tool);
        items.addAll(idx, tools);
    }

    public List<Node> getTools(Node tool) {
        final Node[] tools = (Node[]) tool.getProperties().get(KEY_TOOLS);
        return null == tools ? new ArrayList<>() : new ArrayList<>(Arrays.asList(tools));
    }

    public FlexibleBar setTools(Node tool, Node... tools) {
        final Group groupMarker = (Group) tool.getProperties().get(KEY_GROUP);
        if (null == groupMarker)
            return this;
        final List<Node> exists = getTools(tool);

        for (Node t : tools) {
            t.getProperties().put(KEY_GROUP, groupMarker);
            t.getStyleClass().add("flexible-tools");
        }
        tool.getProperties().put(KEY_TOOLS, tools);

        if (isToolExpanded(tool)) {
            updateToolExpansion(tool, exists);
        }
        return this;
    }

    private int findLastToolIndex() {
        final List<Node> items = getItems();
        int index = -1;
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getProperties().containsKey(KEY_TOOL))
                index = i;
        }
        return index;
    }
}
