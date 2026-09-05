package alphaparse.collections;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.stream.Stream;

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

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    private int flatten(Object[] list, int backwardsIndex) {
        if (isEmpty())
            return backwardsIndex;

        for (var h = this; h.size > 0; h = h.head) {
            if (h.nodeContent instanceof FlatResultSeq) {
                var fsl = (FlatResultSeq) h.nodeContent;
                backwardsIndex = fsl.flatten(list, backwardsIndex);
            } else {
                list[backwardsIndex] = h.nodeContent;
                backwardsIndex--;
            }
        }

        return backwardsIndex;
    }

    public @NotNull Stream<Object> stream() {
        if (cached == null) {
            cached = new Object[size];
            flatten(cached, size - 1);
        }
        return Arrays.stream(cached);
    }

    public @NotNull FlatResultSeq appendOrConcat(final Object obj) {
        if (obj == null) return this;
        return new FlatResultSeq(this, obj);
    }
}
