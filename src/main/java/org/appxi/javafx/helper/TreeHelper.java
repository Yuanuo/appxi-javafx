package org.appxi.javafx.helper;

import javafx.scene.control.TreeItem;
import org.appxi.holder.RawHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface TreeHelper {
    static <T> String path(TreeItem<T> treeItem) {
        return TreeHelper.paths(treeItem).stream()
                .filter(n1 -> n1.getValue() != null)
                .map(n1 -> n1.getValue().toString())
                .collect(Collectors.joining("/"));
    }

    static <T> String parent(TreeItem<T> treeItem) {
        Stream<String> stream = TreeHelper.paths(treeItem).stream()
                .filter(n1 -> n1.getValue() != null)
                .map(n1 -> n1.getValue().toString());
        return stream.limit(Math.max(0, stream.count() - 1)).collect(Collectors.joining("/"));
    }

    static <T> void filterParents(TreeItem<T> treeItem, Predicate<TreeItem<T>> predicate) {
        if (null == treeItem)
            return;
        if (predicate.test(treeItem))
            return;
        filterParents(treeItem.getParent(), predicate);
    }

    static <T> List<TreeItem<T>> paths(TreeItem<T> treeItem) {
        final List<TreeItem<T>> result = new ArrayList<>();
        filterParents(treeItem, v -> {
            if (v.getValue() == null) return true;
            result.add(0, v);
            return false;
        });
        return result;
    }

    static <T> boolean filter(TreeItem<T> treeItem, BiPredicate<TreeItem<T>, T> predicate) {
        if (null == treeItem)
            return true;
        if (predicate.test(treeItem, treeItem.getValue()))
            return true;
        for (TreeItem<T> child : treeItem.getChildren()) {
            if (filter(child, predicate))
                return true;
        }
        return false;
    }

    static <T> boolean filterLeafs(TreeItem<T> treeItem, BiPredicate<TreeItem<T>, T> predicate) {
        if (null == treeItem)
            return true;
        if (treeItem.isLeaf() && predicate.test(treeItem, treeItem.getValue()))
            return true;
        for (TreeItem<T> child : treeItem.getChildren()) {
            if (filterLeafs(child, predicate))
                return true;
        }
        return false;
    }

    static <T> TreeItem<T> findFirstParent(TreeItem<T> treeItem, Predicate<TreeItem<T>> predicate) {
        final RawHolder<TreeItem<T>> holder = new RawHolder<>();
        filterParents(treeItem, item -> {
            if (predicate.test(item)) {
                holder.value = item;
                return true;
            }
            return false;
        });
        return holder.value;
    }

    static <T> TreeItem<T> findFirst(TreeItem<T> treeItem, Predicate<TreeItem<T>> predicate) {
        final RawHolder<TreeItem<T>> holder = new RawHolder<>();
        filter(treeItem, (treeItem1, itemValue) -> {
            if (predicate.test(treeItem1)) {
                holder.value = treeItem1;
                return true;
            }
            return false;
        });
        return holder.value;
    }

    static <T> TreeItem<T> findFirstChild(TreeItem<T> treeItem, Predicate<TreeItem<T>> predicate) {
        for (TreeItem<T> child : treeItem.getChildren()) {
            if (predicate.test(child))
                return child;
        }
        return null;
    }

    static <T> T findFirstParentValue(TreeItem<T> treeItem, Predicate<TreeItem<T>> predicate) {
        final TreeItem<T> first = findFirstParent(treeItem, predicate);
        return null == first ? null : first.getValue();
    }

    static <T> T findFirstValue(TreeItem<T> treeItem, Predicate<TreeItem<T>> predicate) {
        final TreeItem<T> first = findFirst(treeItem, predicate);
        return null == first ? null : first.getValue();
    }

    static <T> TreeItem<T> findFirstParentByValue(TreeItem<T> treeItem, T value) {
        return findFirstParent(treeItem, item -> Objects.equals(item.getValue(), value));
    }

    static <T> TreeItem<T> findFirstByValue(TreeItem<T> treeItem, T value) {
        return findFirst(treeItem, item -> Objects.equals(item.getValue(), value));
    }

    static <T> List<T> findValues(TreeItem<T> treeItem, Predicate<T> predicate, Predicate<List<T>> breakcheck) {
        final List<T> result = new ArrayList<>();
        filter(treeItem, (treeItem1, itemValue) -> {
            if (breakcheck.test(result))
                return true; // break
            if (predicate.test(itemValue))
                result.add(itemValue);
            return false; // continue
        });
        return result;
    }

    static <T> void walk(TreeItem<T> treeItem, BiConsumer<TreeItem<T>, T> consumer) {
        consumer.accept(treeItem, treeItem.getValue());
        for (TreeItem<T> child : treeItem.getChildren()) walk(child, consumer);
    }

    static <T> void walkLeafs(TreeItem<T> treeItem, BiConsumer<TreeItem<T>, T> consumer) {
        if (treeItem.isLeaf()) consumer.accept(treeItem, treeItem.getValue());
        else for (TreeItem<T> child : treeItem.getChildren()) walkLeafs(child, consumer);
    }

    static <T> void walkTree(TreeItem<T> treeItem, TreeWalker<T> treeWalker) {
        if (treeItem.isLeaf())
            treeWalker.visit(treeItem, treeItem.getValue());
        else {
            treeWalker.start(treeItem, treeItem.getValue());
            for (TreeItem<T> child : treeItem.getChildren()) {
                walkTree(child, treeWalker);
            }
            treeWalker.close(treeItem, treeItem.getValue());
        }
    }

    interface TreeWalker<T> {
        void start(TreeItem<T> treeItem, T itemValue);

        void visit(TreeItem<T> treeItem, T itemValue);

        void close(TreeItem<T> treeItem, T itemValue);
    }
}
