package org.appxi.javafx.control;

import org.appxi.javafx.helper.FontFaceHelper;
import javafx.beans.binding.Bindings;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class TextLabel extends TextFlow {
    private boolean wrapText;

    public TextLabel(String text) {
        super();
        this.setText(text);
        parentProperty().addListener((o, ov, nv) -> ensureAutoWrapText());
    }

    public void setText(String text) {
        if (null == text) {
            this.getChildren().clear();
            return;
        }
        if (text.isBlank()) {
            this.getChildren().setAll(new Text(text));
            return;
        }
        this.getChildren().setAll(FontFaceHelper.wrap(text));
    }

    public void setWrapText(boolean wrapText) {
        this.wrapText = wrapText;
        ensureAutoWrapText();
    }

    public boolean isWrapText() {
        return wrapText;
    }

    private void ensureAutoWrapText() {
        if (this.wrapText && getParent() instanceof Region region) {
            maxWidthProperty().bind(Bindings.createDoubleBinding(
                    () -> region.getWidth() - region.getPadding().getLeft() - region.getPadding().getRight() - 2,
                    region.widthProperty(), region.paddingProperty()));
        } else maxWidthProperty().unbind();
    }
}
