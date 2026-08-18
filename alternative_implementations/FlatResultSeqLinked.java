//package alphaparse.collections;
//
//import org.jetbrains.annotations.NotNull;
//import org.jetbrains.annotations.Nullable;
//
//import java.util.Arrays;
//import java.util.List;
//import java.util.stream.Stream;
//
//public class FlatResultSeq {
//    private static FlatResultSeq EMPTY = null;
//
//    private final @Nullable FlatResultSeq head;
//    private final @NotNull Object nodeContent;
//    private final int size;
//
//    public FlatResultSeq(@Nullable FlatResultSeq head, @NotNull Object nodeContent) {
//        this.head = head;
//        this.nodeContent = nodeContent;
//
//        int siz = 0;
//        if (head != null) siz = head.size;
//        siz += (nodeContent instanceof FlatResultSeq)
//                ? ((FlatResultSeq) nodeContent).size
//                : 1;
//        this.size = siz;
//    }
//
//    public FlatResultSeq() {
//        this.head = null;
//        this.nodeContent = -1; // Meaningless value
//        this.size = 0;
//    }
//
//    /**
//     * Instances of this type always start empty. This method simply returns an empty sequence.
//     *
//     * @return The empty instance.
//     */
//    public static @NotNull FlatResultSeq make() {
//        if (EMPTY == null) EMPTY = new FlatResultSeq();
//        return EMPTY;
//    }
//
//    public boolean isEmpty() {
//        return size <= 0;
//    }
//
//    public int size() {
//        return size;
//    }
//
//    private int toList(Object[] list, int backwardsIndex) {
//        if (size <= 0)
//            return backwardsIndex;
//
//        if (nodeContent instanceof FlatResultSeq) {
//            var fsl = (FlatResultSeq) nodeContent;
//            backwardsIndex = fsl.toList(list, backwardsIndex);
//        } else {
//            list[backwardsIndex] = nodeContent;
//            backwardsIndex = backwardsIndex - 1;
//        }
//        return head == null ? backwardsIndex : head.toList(list, backwardsIndex);
//    }
//
//    private Object[] cached;
//
//    public @NotNull Stream<Object> stream() {
//        if (cached == null) {
//            cached = new Object[size];
//            toList(cached, size - 1);
//        }
//        return Arrays.stream(cached);
//    }
//
//    public @NotNull FlatResultSeq appendOrConcat(final Object obj) {
//        if (obj == null) return this;
//        return new FlatResultSeq(this, obj);
//    }
//}
//
