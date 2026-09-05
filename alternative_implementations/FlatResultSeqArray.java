package alphaparse.collections;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * A list-like type of generic elements. It is used to differentiate from other List types.
 * Elements can be added and iterated upon. Each addition creates a new instance.
 */
@Unmodifiable
public final class FlatResultSeq {
    private static FlatResultSeq EMPTY = null;

    private final Object[] v;
    private int hashCode = 0;

    /**
     * Instances of this type always start empty. This method simply returns an empty sequence.
     *
     * @return The empty instance.
     */
    public static @NotNull FlatResultSeq make() {
        if (EMPTY == null) EMPTY = new FlatResultSeq(new Object[0]);
        return EMPTY;
    }

    private FlatResultSeq(final @NotNull Object @NotNull [] v) {
        this.v = v;
    }

    /**
     * A flat stream of all elements.
     *
     * @return A flat stream of all elements.
     */
    public Stream<Object> stream() {
        return Arrays.stream(v);
    }

    /**
     * Return the size of the collection.
     *
     * @return The size as an int.
     */
    public int size() {
        return v.length;
    }

    /**
     * Equivalent to {@code size() == 0}
     *
     * @return true if {@code size() == 0}, false otherwise.
     */
    public boolean isEmpty() {
        return v.length == 0;
    }

    @Override
    public @NotNull String toString() {
        return Arrays.toString(v);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof FlatResultSeq c)) {
            return false;
        }
        return Arrays.equals(v, c.v);
    }

    @Override
    public int hashCode() {
        if (hashCode != 0)
            return hashCode;
        int hc = Arrays.hashCode(v);
        hashCode = hc;
        return hc;
    }

    /**
     * Appends the input to the sequence. If the input is a {@link FlatResultSeq}, it is inserted into the instance flattened.
     * <p>
     * {@code null} inputs are ignored, as are empty {@link FlatResultSeq} inputs.
     *
     * @param obj Input.
     * @return A new instance.
     */
    public @NotNull FlatResultSeq appendOrConcat(final Object obj) {
        if (obj == null)
            return this;

        if (obj instanceof FlatResultSeq frs) {
            if (size() == 0)
                return frs;
            if (frs.isEmpty())
                return this;
            var otherArray = frs.v;
            final @NotNull Object[] newV = Arrays.copyOf(v, v.length + otherArray.length);
            System.arraycopy(otherArray, 0, newV, v.length, otherArray.length);

            return new FlatResultSeq(newV);
        } else {
            final @NotNull Object[] newV = Arrays.copyOf(v, v.length + 1);
            newV[newV.length - 1] = obj;

            return new FlatResultSeq(newV);
        }
    }
}
