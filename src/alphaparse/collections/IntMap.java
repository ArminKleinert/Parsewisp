package alphaparse.collections;

import alphaparse.trampoline.Tramp;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

/**
 * A sorted collection which uses primitive ints as keys. It is made specifically for use in {@link Tramp}.
 *
 * @param <V> Value type.
 */
public final class IntMap<V> {
    private int[] keys;
    private Object[] values;
    private int size;

    /**
     * The constructor.
     *
     * @param capacity Initial capacity.
     */
    public IntMap(final int capacity) {
        keys = new int[capacity];
        values = new Object[capacity];
    }


    private int findIndex(final int key) {
        return Arrays.binarySearch(keys, 0, size, key);
    }

    private void ensureCapacity(final int needed) {
        if (needed <= keys.length) return;
        int newCap = Math.max(needed, keys.length * 2 + 1);
        keys = Arrays.copyOf(keys, newCap);
        values = Arrays.copyOf(values, newCap);
    }

    /**
     * Adds a new entry or replaces an old one.
     *
     * @param key   The key.
     * @param value The value.
     */
    public void put(final int key, final V value) {
        int idx = findIndex(key);

        if (idx >= 0) {
            values[idx] = value;
            return;
        }

        int insertAt = -idx - 1;
        ensureCapacity(size + 1);

        System.arraycopy(keys, insertAt, keys, insertAt + 1, size - insertAt);
        System.arraycopy(values, insertAt, values, insertAt + 1, size - insertAt);

        keys[insertAt] = key;
        values[insertAt] = value;
        size++;
    }

    /**
     * Removes and returns the last value. If the map is empty, return null.
     *
     * @return The last value or null.
     */
    public @Nullable V intMapPoll() {
        if (size == 0)
            return null;
        var last = values[size - 1];
        size--;
        //noinspection unchecked
        return (V) last;
    }
}
