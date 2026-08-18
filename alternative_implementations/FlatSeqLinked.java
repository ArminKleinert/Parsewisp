package de.kleinert.flatlink;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public class FlatSeqLinked implements Iterable<Object> {
    private static FlatSeqLinked EMPTY = null;

    private List<Object> cached;
    private final @Nullable FlatSeqLinked head;
    private final @NotNull Object nodeContent;
    private final int size;

    public FlatSeqLinked(@Nullable FlatSeqLinked head, @NotNull Object nodeContent) {
        this.head = head;
        this.nodeContent = nodeContent;

        int siz = 0;
        if (head != null) siz = head.size;
        siz += (nodeContent instanceof FlatSeqLinked)
                ? ((FlatSeqLinked) nodeContent).size
                : 1;
        this.size = siz;
    }

    public FlatSeqLinked() {
        this.head = null;
        this.nodeContent = -1; // Meaningless value
        this.size = -1;
    }

    /**
     * Instances of this type always start empty. This method simply returns an empty sequence.
     *
     * @return The empty instance.
     */
    public static @NotNull FlatSeqLinked make() {
        if (EMPTY == null) EMPTY = new FlatSeqLinked();
        return EMPTY;
    }

    @Override
    public @NotNull Iterator<Object> iterator() {
        return toList().iterator();
    }

    private int toList(Object[] list, int backwardsIndex) {
        if (size <= 0)
            return backwardsIndex;

        if (nodeContent instanceof FlatSeqLinked) {
            var fsl = (FlatSeqLinked) nodeContent;
            backwardsIndex = fsl.toList(list, backwardsIndex);
        } else {
            list[backwardsIndex] = nodeContent;
            backwardsIndex = backwardsIndex - 1;
        }
        return head == null ? backwardsIndex : head.toList(list, backwardsIndex);
    }

    public @NotNull List<Object> toList() {
        if (cached == null) {
            var cachedElements = new Object[size];
            toList(cachedElements, size - 1);
            cached = Arrays.asList(cachedElements);
        }
        return cached;
    }

    public @NotNull Stream<Object> stream() {
            var cachedElements = new Object[size];
            toList(cachedElements, size - 1);
            return Arrays.stream(cachedElements);
    }

    public @NotNull FlatSeqLinked appendOrConcat(final Object obj) {
        if (obj == null) return this;
        return new FlatSeqLinked(this, obj);
    }
}
