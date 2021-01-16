package org.appxi.javafx.helper;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.ColorAdjust;

public interface FxHelper {
    class DisabledEffectsListener implements ChangeListener<Boolean> {
        private final Node node;
        private final ColorAdjust effects = new ColorAdjust();

        public DisabledEffectsListener(Node node) {
            this.node = node;
            effects.setInput(new BoxBlur());
            effects.setBrightness(-0.5);
        }

        @Override
        public void changed(ObservableValue<? extends Boolean> o, Boolean ov, Boolean nv) {
            node.setEffect(nv ? effects : null);
        }
    }

    static <T extends Node> T setDisabledEffects(T node) {
        DisabledEffectsListener listener = (DisabledEffectsListener) node.getProperties().get(DisabledEffectsListener.class);
        if (null == listener)
            node.getProperties().put(DisabledEffectsListener.class, listener = new DisabledEffectsListener(node));
        node.disabledProperty().removeListener(listener);
        node.disabledProperty().addListener(listener);
        return node;
    }
}
