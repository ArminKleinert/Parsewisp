package de.kleinert.parsewisp.collections;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.IntFunction;

/**
 * Takes a function and lazily executes it repeatedly when requested.
 * The results are buffered in a {@link List} for fast access.
 * A maximum number of results can be specified.
 * The function takes an {@code int} as input.
 *
 * @param <T> The result type for the function.
 */
public class LazySupplierList<T> implements List<@Nullable T> {
    private final @NotNull ArrayList<@NotNull T> evaluatedPart;
    private final int maxResults;
    private @Nullable IntFunction<@Nullable T> nextFn;
    private boolean fullyEvaluated = false;

    /**
     * Instantiates a new instance.
     *
     * @param nextFn     The function.
     * @param maxResults Maximum number of results after which generation of results stops.
     */
    public LazySupplierList(final @NotNull IntFunction<@Nullable T> nextFn, final int maxResults) {
        this.evaluatedPart = new ArrayList<>();
        this.nextFn = nextFn;
        this.maxResults = maxResults;
    }

    private @Nullable T evalutePart(int i) {
        // Already calculated.
        if (i < evaluatedPart.size())
            return evaluatedPart.get(i);

        // End reached.
        if (evaluatedPart.size() >= maxResults) {
            fullyEvaluated = true;
            nextFn = null; // Discard the function.
        }

        // The list is fully evaluated. There is no element at index i.
        if (fullyEvaluated)
            return null;

        // Fully evaluated but the function is gone? Impossible!
        if (nextFn == null)
            throw new IllegalStateException();

        // Calculate the next element.
        final var next = nextFn.apply(evaluatedPart.size());

        // End reached?
        if (next == null) {
            fullyEvaluated = true;
            nextFn = null; // Discard the function.
            return null;
        }

        // End not reached yet: Buffer the element and return it.
        evaluatedPart.add(next);
        return next;
    }

    /**
     * Fully evaluates the list.
     */
    public void evaluate() {
        @Nullable T ep;
        do {
            ep = evalutePart(evaluatedPart.size());
        } while (ep != null);
    }

    /**
     * True if the list is fully evaluated, false otherwise.
     *
     * @return true or false.
     */
    public boolean isFullyEvaluated() {
        return fullyEvaluated;
    }

    @Override
    public String toString() {
        evaluate();
        return evaluatedPart.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof List<?> c)) {
            return false;
        }

        if (o == this)
            return true;

        return Arrays.equals(
                toArray(),
                c.toArray()
        );
    }

    @Override
    public int hashCode() {
        int hc = 1;
        for (final T e : this)
            hc = hc * 31 + Objects.hashCode(e);
        return hc;
    }

    @Override
    public int size() {
        evaluate();
        return evaluatedPart.size();
    }

    @Override
    public boolean isEmpty() {
        return evaluatedPart.isEmpty() && evalutePart(0) == null;
    }

    @Override
    public boolean contains(final Object o) {
        for (T t : this) {
            if (Objects.equals(t, o))
                return true;
        }
        return false;
    }

    /**
     * Implemented to allow the use of {@link List#stream()} operations.
     *
     * @return A Spliterator for this list.
     */
    @Override
    public @NotNull Spliterator<@Nullable T> spliterator() {
        return Spliterators.spliteratorUnknownSize(iterator(), Spliterator.ORDERED);
    }

    @Override
    public @NotNull Iterator<T> iterator() {
        return new Iterator<>() {
            private int cursor = 0;

            @Override
            public boolean hasNext() {
                return getOrNull(cursor) != null;
            }

            @Override
            public T next() {
                int i = cursor;
                ++cursor;
                return get(i);
            }
        };
    }

    @Override
    public @NotNull Object @NotNull [] toArray() {
        return toArray(new Object[0]);
    }

    @Override
    public <T1> T1 @NotNull [] toArray(T1 @NotNull [] t1s) {
        evaluate();
        return evaluatedPart.toArray(t1s);
    }

    @Override
    public boolean containsAll(final @NotNull Collection<?> collection) {
        for (Object o : collection) {
            if (!contains(o))
                return false;
        }
        return true;
    }

    @Override
    public T get(final int i) {
        final var at = getOrNull(i);
        if (at == null)
            throw new IndexOutOfBoundsException("Cannot access index " + i + " because size is " + size());
        return at;
    }

    public T getFirst() {
        if (!evaluatedPart.isEmpty())
            return evaluatedPart.get(0);
        evalutePart(0);
        return evaluatedPart.get(0);
    }

    /**
     * Returns the element at index i. If the list has not been calculated up to that point yet, evaluate until the index is reached.
     * This function runs in amortized O(1) time.
     * If the list is fully evaluated and the index is too big, return null;
     *
     * @param i The index.
     * @return The element at the provided index or null.
     */
    public T getOrNull(final int i) {
        // If already calculated, return.
        if (i < evaluatedPart.size())
            return evaluatedPart.get(i);

        // Not calculated yet: Do work and return.
        int cursor = evaluatedPart.size();
        while (!fullyEvaluated) {
            final T next = evalutePart(i);
            if (cursor == i) return next; // Yay. Found it.
            cursor++;
        }
        return null; // List too short. null :(
    }

    @Override
    public int indexOf(final Object o) {
        int i = 0;
        for (T t : this) {
            if (Objects.equals(o, t))
                return i;
            i++;
        }
        return -1;
    }

    @Override
    public int lastIndexOf(final Object o) {
        int i = 0;
        int last = -1;
        for (T t : this) {
            if (Objects.equals(o, t))
                last = i;
            i++;
        }
        return last;
    }

    @Override
    public @NotNull List<T> subList(final int i, final int i1) {
        List<T> res = new ArrayList<>();
        int cursor = 0;
        for (var t : this) {
            if (cursor >= i && cursor < i1) res.add(t);
            else if (cursor >= i1) break;
        }
        return Collections.unmodifiableList(res);
    }

    @Override
    public @NotNull ListIterator<T> listIterator() {
        return listIterator(0);
    }

    @Override
    public @NotNull ListIterator<T> listIterator(final int i) {
        return List.copyOf(this).listIterator(i);
    }

    @Override
    public boolean add(T t) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(final @NotNull Collection<? extends T> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(final int i, final @NotNull Collection<? extends T> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(final @NotNull Collection<?> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(final @NotNull Collection<?> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public T set(final int i, final T t) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(final int i, final T t) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T remove(final int i) {
        throw new UnsupportedOperationException();
    }
}
