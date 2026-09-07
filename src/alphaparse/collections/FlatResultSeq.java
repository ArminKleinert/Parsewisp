package alphaparse.collections;

import org.jetbrains.annotations.NotNull;

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
    private static FlatResultSeq EMPTY = null;

    private final @NotNull FlatResultSeq head;
    private final @NotNull Object nodeContent;
    private final int size;

    private Object[] cached;

    private FlatResultSeq(@NotNull FlatResultSeq head, @NotNull Object nodeContent) {
        this.head = head;
        this.nodeContent = nodeContent;

        int siz = head.size;
        siz += (nodeContent instanceof FlatResultSeq)
                ? ((FlatResultSeq) nodeContent).size
                : 1;
        this.size = siz;
    }

    private FlatResultSeq() {
        this.head = this;
        this.nodeContent = 0; // Meaningless value
        this.size = 0;
    }

    /**
     * Instances of this type always start empty. This method simply returns an empty sequence.
     *
     * @return The empty instance.
     */
    public static @NotNull FlatResultSeq make() {
        if (EMPTY == null) EMPTY = new FlatResultSeq();
        return EMPTY;
    }

    /**
     * Checks whether this sequence is empty.
     * @return true if size==0
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Returns the size of this sequence.
     * @return The size.
     */
    public int size() {
        return size;
    }

    /**
     * This method flattens the sequence into an array.
     * <p>
     * Implementation note: {@link FlatResultSeq} is built as a reversed singly-linked list.
     *
     * <pre>
     * {@code
     * var frs12 = FlatResultSeq.make().appendOrConcat(1).appendOrConcat(2); // (content=2, head=(content=1, head=EMPTY))
     * var frs1212 = frs12.appendOrConcat(frs12); // (content=frs12, head=frs12)
     * var frs12123 = frs12.appendOrConcat(frs12).appendOrConcat(3); // (content=3, head=(content=frs12, head=frs12))
     * }
     * </pre>
     *
     * Flattening the sequence iterates backwards.
     * <p>
     * <b>Example: {@code frs12123.stream} is called.</b>
     * <p>
     * First, an array is created: {@code new Object[frs12123.size()]}<br/>
     * The index is set to {@code frs12123.size()-1}
     * <p>
     * Now, the list is iterated backwards:<br/>
     * Iteration 1: {@code array=[null,null,null,null,null], index=4, seq=(content=3, head=(content=(content=2, head=(content=1, head=EMPTY)), head=(content=2, head=(content=1, head=EMPTY))))}
     * <p>
     * Add {@code content} at the index to the array, decrement index, continue with {@code head}.<br/>
     * Iteration 2: {@code array=[null,null,null,null,3], index=3, seq=(content=(content=2, head=(content=1, head=EMPTY)), head=(content=2, head=(content=1, head=EMPTY)))}
     * <p>
     * {@code content} is a {@link FlatResultSeq}, so do a recursive call. This adds {@code 2} at index 3 and {@code 1} at index 2. The index is decremented by 2 because two elements were added.<br/>
     * Iteration 3: {@code array=[null,null,1,2,3], index=1, seq=(content=2, head=(content=1, head=EMPTY))}
     * <p>
     * Do another recursive call on the last two elements.<br/>
     * Iteration 4: {@code array=[1,2,1,2,3], index=-1, seq=EMPTY}
     * <p>
     * The sequence is now empty, the array is filled. So the index ({@code -1}) is returned and the array is saved.<br/>
     *
     * @param list Destination array.
     * @param backwardsIndex Current index.
     * @return The next index.
     */
    private int flatten(Object[] list, int backwardsIndex) {
        if (isEmpty())
            return backwardsIndex;

        for (var h = this; h.size > 0; h = h.head) {
            if (h.nodeContent instanceof FlatResultSeq) {
                var frs = (FlatResultSeq) h.nodeContent;
                backwardsIndex = frs.flatten(list, backwardsIndex); // recursive call
            } else {
                list[backwardsIndex] = h.nodeContent;
                backwardsIndex--;
            }
        }

        return backwardsIndex;
    }

    /**
     * Returns a stream for this sequence. The result may be cached.
     *
     * @return A stream for this sequence.
     */
    public @NotNull Stream<Object> stream() {
        if (cached == null) {
            cached = new Object[size];
            flatten(cached, size - 1);
        }
        return Arrays.stream(cached);
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
        return new FlatResultSeq(this, obj);
    }
}
