package org.appxi.javafx.control;

import javafx.scene.control.Dialog;
import javafx.stage.Stage;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class DialogEx<R> extends Dialog<R> {
    public final Stage window;

    public DialogEx() {
        super();

        try {
            Field dialogField = Dialog.class.getDeclaredField("dialog");
            dialogField.setAccessible(true);
            Object dialog = dialogField.get(this);
            Method getWindow = dialog.getClass().getDeclaredMethod("getWindow");
            getWindow.setAccessible(true);
            this.window = (Stage) getWindow.invoke(dialog);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
