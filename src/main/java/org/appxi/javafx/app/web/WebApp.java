package org.appxi.javafx.app.web;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public interface WebApp {
    default Supplier<List<String>> webIncludesSupplier() {
        return null;
    }

    default Function<String, String> htmlDocumentWrapper() {
        return null;
    }
}
