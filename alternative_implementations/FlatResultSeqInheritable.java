package alphaparse.collections;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;

/**
 * A list-like type of generic elements. It is used to differentiate from other List types.
 * Elements can be added and iterated upon. Each addition creates a new instance.
 * <p>
 * These lists are outwardly purely functional. They are also intended to be intermediate structures and thus do not need to override {@link Object#equals(Object)} ()}, {@link Object#hashCode()}, or {@link Object#toString()}.
 * <p>
 * It is advised that users of this library do not use this type directly.
 */
public class FlatResultSeq {
    private @Nullable FlatResultSeq inheritor;
    private final int size;
    private final ArrayList<Object> arr;

    private FlatResultSeq(ArrayList<Object> arr, int size) {
        this.arr = arr;
        this.size = size;
    }

    /**
     * Instances of this type always start empty. This method simply returns an empty sequence.
     *
     * @return The empty instance.
     */
    public static @NotNull FlatResultSeq make() {
        return new FlatResultSeq(new ArrayList<>(), 0);
    }

    /**
     * Checks whether this sequence is empty.
     *
     * @return true if size==0
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Returns the size of this sequence.
     *
     * @return The size.
     */
    public int size() {
        return size;
    }

    /**
     * Returns a stream for this sequence. The result may be cached.
     *
     * @return A stream for this sequence.
     */
    public @NotNull Stream<Object> stream() {
        return arr.stream().limit(size);
    }

    /**
     * Adds an element or multiple elements to this sequence.
     * <ul>
     *     <li>If the input is {@code null}, do nothing.</li>
     *     <li>If the input is a {@link FlatResultSeq}, add all elements flattened, as if by {@link Collection#addAll(Collection)}</li>
     *     <li>Otherwise, add the input as a single element, as if by {@link Collection#add(Object)}.</li>
     * </ul>
     *
     * @param obj The input.
     * @return A new instance or {@code this}, if nothing is added.
     */
    public @NotNull FlatResultSeq appendOrConcat(final Object obj) {
        if (obj == null) return this;

        if (inheritor == null) {
            if (obj instanceof FlatResultSeq) {
                arr.addAll(((FlatResultSeq) obj).arr.subList(0, ((FlatResultSeq) obj).size));
            } else {
                arr.add(obj);
            }

            var newSeq = new FlatResultSeq(arr, arr.size());
            inheritor = newSeq;
            return newSeq;
        }

        var newArr = new ArrayList<>(arr.subList(0, size));
        var newSize = size;
        if (obj instanceof FlatResultSeq) {
            newArr.addAll(((FlatResultSeq) obj).arr.subList(0, ((FlatResultSeq) obj).size));
            newSize += ((FlatResultSeq) obj).size;
        } else {
            newArr.add(obj);
            newSize++;
        }

        return new FlatResultSeq(newArr, newSize);
    }
}
