package org.appxi.javafx.control;

import javafx.scene.layout.StackPane;

import java.util.ArrayList;
import java.util.Collection;

public abstract class LookupViewEx<T> extends LookupView<T> {
    public LookupViewEx(StackPane owner) {
        super(owner);
    }

    @Override
    protected final Collection<T> search(String inputText, int resultLimit) {
        String searchText = inputText.replaceAll("[,，]$", "").strip();
        if (!searchText.isEmpty() && searchText.charAt(0) == '#') {
            String[] searchTerms = searchText.substring(1).split("[;；]");
            Collection<T> result = new ArrayList<>(searchTerms.length);
            for (String searchTerm : searchTerms) {
                convertSearchTermToCommands(searchTerm.strip(), result);
            }
            return result;
        } else {
            searchText = prepareSearchText(searchText);
        }
        String[] searchWords = searchText.split("[,，]");
        if (searchWords.length == 1)
            searchWords = null;
        return search(searchText, searchWords, resultLimit);
    }

    protected abstract String prepareSearchText(String searchText);

    protected abstract Collection<T> search(String searchText, String[] searchWords, int resultLimit);

    protected abstract void convertSearchTermToCommands(String searchTerm, Collection<T> result);
}
