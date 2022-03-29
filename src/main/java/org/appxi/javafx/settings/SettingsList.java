package org.appxi.javafx.settings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public abstract class SettingsList {
    private static final List<Supplier<Option<?>>> settings = new ArrayList<>();

    public static void add(Supplier<Option<?>> optionSupplier) {
        settings.remove(optionSupplier);
        settings.add(optionSupplier);
    }

    public static List<Supplier<Option<?>>> get() {
        return Collections.unmodifiableList(settings);
    }

    private SettingsList() {
    }
}
