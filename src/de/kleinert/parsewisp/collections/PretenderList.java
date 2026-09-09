package de.kleinert.parsewisp.collections;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * An interface which pretends to be a list. Every method of {@link List} is defined to throw an {@link UnsupportedOperationException}.
 *
 * @param <T> The generic type.
 */
public interface PretenderList<T> extends List<T> {
    @Override
    default int size() {
        throw new UnsupportedOperationException();
    }

    @Override
    default boolean isEmpty() {
        throw new UnsupportedOperationException();
    }

    @Override
    default boolean contains(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    default @NotNull Iterator<T> iterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    default @NotNull T @NotNull [] toArray() {
        throw new UnsupportedOperationException();
    }

    @Override
    default @NotNull <T1> T1 @NotNull [] toArray(@NotNull T1 @NotNull [] ts) {
        throw new UnsupportedOperationException();
    }

    @Override
    default boolean add(T o) {
        throw new UnsupportedOperationException();
    }

    @Override
    default boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    default boolean containsAll(@NotNull Collection<?> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    default boolean addAll(@NotNull Collection<? extends T> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    default boolean addAll(int i, @NotNull Collection<? extends T> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    default boolean removeAll(@NotNull Collection<?> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    default boolean retainAll(@NotNull Collection<?> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    default void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    default T get(int i) {
        throw new UnsupportedOperationException();
    }

    @Override
    default T set(int i, T o) {
        throw new UnsupportedOperationException();
    }

    @Override
    default void add(int i, T o) {
        throw new UnsupportedOperationException();
    }

    @Override
    default T remove(int i) {
        throw new UnsupportedOperationException();
    }

    @Override
    default int indexOf(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    default int lastIndexOf(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    default @NotNull ListIterator<T> listIterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    default @NotNull ListIterator<T> listIterator(int i) {
        throw new UnsupportedOperationException();
    }

    @Override
    default @NotNull List<T> subList(int i, int i1) {
        throw new UnsupportedOperationException();
    }
}
