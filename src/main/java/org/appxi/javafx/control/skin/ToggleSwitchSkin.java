package org.appxi.javafx.control.skin;

import javafx.animation.TranslateTransition;
import javafx.scene.control.SkinBase;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.Region;
import javafx.util.Duration;

public class ToggleSwitchSkin extends SkinBase<ToggleButton> {
    private final Region thumb = new Region();
    private final Region track = new Region();
    private double thumbWidth;
    private double trackWidth;
    private final ToggleButton control;
    private final TranslateTransition thumbTransition;

    public ToggleSwitchSkin(ToggleButton button) {
        super(button);
        this.thumbTransition = new TranslateTransition(Duration.millis(1.0D), this.thumb);
        this.control = button;
        this.initialize();
    }

    private void initialize() {
        this.thumbTransition.setByX(1.0D);
        this.thumbTransition.setCycleCount(1);
        this.thumbTransition.setAutoReverse(false);
        this.thumb.getStyleClass().setAll("thumb");
        this.thumb.setOnMouseReleased((e) -> this.control.setSelected(!this.control.isSelected()));
        this.getSkinnable().selectedProperty().addListener((selected) -> this.positionThumb(true));
        this.track.getStyleClass().setAll("track");
        this.track.setOnMouseReleased((e) -> this.control.setSelected(e.getX() >= this.control.getPrefWidth() / 2.0D));
        this.getChildren().clear();
        this.getChildren().addAll(this.track, this.thumb);
    }

    protected void layoutChildren(double x, double y, double w, double h) {
        this.thumbWidth = this.snapSize(this.thumb.prefWidth(-1.0D));
        double thumbHeight = this.snapSize(this.thumb.prefHeight(-1.0D));
        this.thumb.resizeRelocate(0.0D, this.snapPosition(y + (h - thumbHeight) / 2.0D), this.thumbWidth, thumbHeight);
        this.trackWidth = this.snapSize(this.track.prefWidth(-1.0D));
        double trackHeight = this.snapSize(this.track.prefHeight(-1.0D));
        this.track.resizeRelocate(0.0D, this.snapPosition(y + (h - trackHeight) / 2.0D), this.trackWidth, trackHeight);
        this.positionThumb(false);
    }

    private void positionThumb(boolean animate) {
        this.thumbTransition.setDuration(Duration.millis(animate ? 150.0D : 1.0D));
        this.thumbTransition.setToX(this.control.isSelected() ? this.snapPosition(this.trackWidth - this.thumbWidth) : 0.0D);
        this.thumbTransition.play();
    }
}
