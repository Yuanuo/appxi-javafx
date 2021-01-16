package org.appxi.javafx.helper;

import javafx.fxml.FXMLLoader;
import org.appxi.javafx.views.FxmlController;

import java.net.URL;

public interface FxmlHelper {

    static Object[] load(URL fxmlLocation) {
        try {
            final FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(fxmlLocation);
            fxmlLoader.load();
            return new Object[]{fxmlLoader.getController(), fxmlLoader.getRoot()};
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    static <T> T loadController(URL fxmlLocation) {
        final Object[] loaded = load(fxmlLocation);
        return null == loaded ? null : (T) loaded[0];
    }

    static Object[] load(FxmlController fxmlController) {
        final URL fxmlLocation = fxmlController.getFxmlLocation();
        if (null == fxmlLocation)
            return new Object[]{fxmlController, null};
        try {
            final FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(fxmlLocation);
            fxmlLoader.load();
            final Object controllerObj = fxmlLoader.getController();
            if (null != controllerObj)
                return new Object[]{controllerObj, fxmlLoader.getRoot()};
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            final FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(fxmlLocation);
            fxmlLoader.setController(fxmlController);
            fxmlLoader.load();
            return new Object[]{fxmlLoader.getController(), fxmlLoader.getRoot()};
        } catch (Exception e) {
        }
        return null;
    }

    static <T> T loadController(FxmlController fxmlController) {
        final Object[] loaded = load(fxmlController);
        return null == loaded ? null : (T) loaded[0];
    }
}
