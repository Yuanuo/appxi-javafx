package org.appxi.javafx.control;

import javafx.beans.binding.Bindings;
import javafx.scene.Node;
import javafx.scene.control.DialogPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

public class DialogLayer extends DialogPane {
    private final boolean enableButtonBar;
    public final OpaqueLayer opaqueLayer;

    public DialogLayer() {
        this(false);
    }

    public DialogLayer(boolean enableButtonBar) {
        super();
        this.getStyleClass().add("dialog-layer");
        this.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        this.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        this.enableButtonBar = enableButtonBar;
        this.opaqueLayer = new OpaqueLayer(this);
    }

    @Override
    protected final Node createButtonBar() {
        return enableButtonBar ? super.createButtonBar() : null;
    }

    public final void autoPadding(int widthPad, int heightPad) {
        this.prefWidthProperty().bind(Bindings.createDoubleBinding(
                () -> opaqueLayer.getWidth() - widthPad,
                opaqueLayer.widthProperty(), opaqueLayer.paddingProperty()));
        this.prefHeightProperty().bind(Bindings.createDoubleBinding(
                () -> opaqueLayer.getHeight() - heightPad / 2,
                opaqueLayer.heightProperty(), opaqueLayer.paddingProperty()));
    }

    public final boolean isShowing() {
        return this.opaqueLayer.isShowing();
    }

    public final void show(StackPane glassPane) {
        if (this.opaqueLayer.getCenter() != this) this.opaqueLayer.setCenter(this);
        this.opaqueLayer.show(glassPane);
    }

    public final void hide() {
        this.opaqueLayer.hide();
    }
}
