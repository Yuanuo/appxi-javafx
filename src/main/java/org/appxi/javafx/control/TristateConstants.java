package org.appxi.javafx.control;

public interface TristateConstants {
    Object CHECKED = new Object();
    Object UNCHECK = new Object();

    static boolean isSelected(Object state) {
        return state != null && (Boolean.TRUE.equals(state) || state == CHECKED);
    }

    static Object state(Object obj) {
        return Boolean.TRUE.equals(obj) ? CHECKED : UNCHECK;
    }
}
