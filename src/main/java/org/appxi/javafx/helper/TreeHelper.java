package org.appxi.javafx.helper;

import javafx.scene.control.TreeItem;
import org.appxi.holder.RawHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public interface TreeHelper {
    static <T> void walkParents(TreeItem<T> treeItem, Predicate<TreeItem<T>> predicate) {
        if (null == treeItem)
            return;
        if (predicate.test(treeItem))
            return;
        walkParents(treeItem.getParent(), predicate);
    }

    static <T> boolean walk(TreeItem<T> treeItem, Predicate<TreeItem<T>> predicate) {
        if (null == treeItem)
            return true;
        if (predicate.test(treeItem))
            return true;
        for (TreeItem<T> child : treeItem.getChildren()) {
            if (walk(child, predicate))
                return true;
        }
        return false;
    }

    static <T> boolean walkLeafs(TreeItem<T> treeItem, Predicate<TreeItem<T>> predicate) {
        if (null == treeItem)
            return true;
        if (treeItem.isLeaf() && predicate.test(treeItem))
            return true;
        for (TreeItem<T> child : treeItem.getChildren()) {
            if (walkLeafs(child, predicate))
                return true;
        }
        return false;
    }

    static <T> TreeItem<T> findFirstParent(TreeItem<T> treeItem, Predicate<TreeItem<T>> predicate) {
        final RawHolder<TreeItem<T>> holder = new RawHolder<>();
        walkParents(treeItem, item -> {
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
        walk(treeItem, item -> {
            if (predicate.test(item)) {
                holder.value = item;
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
        walk(treeItem, item -> {
            if (breakcheck.test(result))
                return true; // break
            if (predicate.test(item.getValue()))
                result.add(item.getValue());
            return false; // continue
        });
        return result;
    }


}
