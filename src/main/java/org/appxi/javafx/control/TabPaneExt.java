package org.appxi.javafx.control;

import javafx.collections.ListChangeListener;
import javafx.geometry.Side;
import javafx.scene.control.Tab;

public class TabPaneExt extends TabPaneEx {

    public TabPaneExt() {
        super();
        this.getStyleClass().add("stretched");
        setUpChangeListeners();
    }

    private void setUpChangeListeners() {
        widthProperty().addListener((value, oldWidth, newWidth) -> {
            Side side = getSide();
            int numTabs = getTabs().size();
            if ((side == Side.BOTTOM || side == Side.TOP) && numTabs != 0) {
                setTabMinWidth(newWidth.intValue() / numTabs);
                setTabMaxWidth(newWidth.intValue() / numTabs);
            }
        });

        heightProperty().addListener((value, oldHeight, newHeight) -> {
            Side side = getSide();
            int numTabs = getTabs().size();
            if ((side == Side.LEFT || side == Side.RIGHT) && numTabs != 0) {
                setTabMinWidth(newHeight.intValue() / numTabs);
                setTabMaxWidth(newHeight.intValue() / numTabs);
            }
        });

        getTabs().addListener((ListChangeListener<Tab>) change -> {
            Side side = getSide();
            int numTabs = getTabs().size();
            if (numTabs != 0) {
                if (side == Side.LEFT || side == Side.RIGHT) {
                    setTabMinWidth(heightProperty().intValue() / numTabs);
                    setTabMaxWidth(heightProperty().intValue() / numTabs);
                }
                if (side == Side.BOTTOM || side == Side.TOP) {
                    setTabMinWidth(widthProperty().intValue() / numTabs);
                    setTabMaxWidth(widthProperty().intValue() / numTabs);
                }
            }
        });
    }
}