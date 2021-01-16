package org.appxi.javafx.workbench;

import javafx.scene.Node;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.ToggleButton;
import org.appxi.javafx.control.WorkbenchPane;
import org.appxi.javafx.workbench.views.WorkbenchOpenpartController;
import org.appxi.javafx.workbench.views.WorkbenchWorkpartController;
import org.appxi.javafx.workbench.views.WorkbenchWorktoolController;

public class WorkbenchPaneEx extends WorkbenchPane {

    public void addWorkpart(WorkbenchWorkpartController controller) {
        final Label viewinfo = controller.getViewpartInfo();
        final Node viewport = controller.getViewport();

        viewinfo.setUserData(controller);
        this.addWorkview(controller.viewId, viewinfo, viewport, controller);
    }

    public void addWorktool(WorkbenchWorktoolController controller) {
        final Label viewinfo = controller.getViewpartInfo();

        viewinfo.setUserData(controller);
        this.addWorktool(controller.viewId, viewinfo, controller);
    }

    public void addOpenpart(WorkbenchOpenpartController controller) {
        final Label viewinfo = controller.getViewpartInfo();
        final Node viewport = controller.getViewport();

        viewinfo.setUserData(controller);
        if (controller.isWorktoolSupport()) {
            this.addOpenviewWithWorktool(controller.viewId, viewinfo, viewport, controller);
        } else {
            this.addOpenview(controller.viewId, viewinfo, viewport, controller);
        }
    }

    public final WorkbenchWorkpartController selectedWorkpart() {
        final ToggleButton tool = this.selectedWorktool();
        return null != tool ? (WorkbenchWorkpartController) tool.getUserData() : null;
    }

    public final WorkbenchOpenpartController selectedOpenpart() {
        final Tab tool = this.selectedOpentool();
        return null != tool ? (WorkbenchOpenpartController) tool.getUserData() : null;
    }

    public final void selectWorkpart(String id) {
        this.selectWorktool(id);
    }

    public final void selectOpenpart(String id) {
        this.selectOpenview(id);
    }

    public final WorkbenchWorkpartController findWorkpart(String id) {
        final ButtonBase tool = this.findWorktool(id);
        return null != tool ? (WorkbenchWorkpartController) tool.getUserData() : null;
    }

    public final WorkbenchOpenpartController findOpenpart(String id) {
        final Tab tool = this.findOpentool(id);
        return null != tool ? (WorkbenchOpenpartController) tool.getUserData() : null;
    }
}
