//package alphaparse.collections;
//
//
//import org.jetbrains.annotations.NotNull;
//import org.jetbrains.annotations.Nullable;
//import org.jetbrains.annotations.Unmodifiable;
//
//import java.util.*;
//
///**
// * A list-like type of generic elements. It is used to differentiate from other List types.
// * Elements can be added and iterated upon. Each addition creates a new instance.
// * <p>
// * These lists function as reversed VLists. A comparison of ordinary lists to Flat sequences:
// * List: [1]; Flat: [[1]] (offset=0)
// * List: [1, 2]; Flat: [[, 2], [1]] (offset=1)
// * List: [1, 2, 3, 4, 5, 6, 7]; Flat: [[7, 6, 5, 4], [3, 2], [1]] (offset=0)
// */
//@Unmodifiable
//public final class FlatResultSeq implements Iterable<Object> {
//    private static FlatResultSeq EMPTY = null;
//
//    private static class Node {
//        private final Node next;
//        private final Object[] v;
//
//        public Node(Node next, Object[] v) {
//            this.next = next;
//            this.v = v;
//        }
//    }
//
//    private final Node base;
//    private int offset = 0;
//    private int hashCode = 0;
//
//    /**
//     * Instances of this type always start empty. This method simply returns an empty sequence.
//     *
//     * @return The empty instance.
//     */
//    public static @NotNull FlatResultSeq make() {
//        if (EMPTY == null) EMPTY = new FlatResultSeq(null, 0, 0);
//        return EMPTY;
//    }
//
//    private FlatResultSeq(final Node base, final int offset, final int hashCode) {
//        this.base = base;
//        this.offset = offset;
//        this.hashCode = hashCode;
//    }
//
//    @Override
//    public @NotNull Iterator<Object> iterator() {
//        return new Iterator<>() {
//            private Node node = base;
//            private int nodeOffset = offset;
//
//            public boolean hasNext() {
//                return node != null;
//            }
//
//            public Object next() {
//                if (node == null) throw new NoSuchElementException();
//                var temp = node.v[nodeOffset];
//                nodeOffset++;
//                if (nodeOffset == node.v.length) {node=node.next;nodeOffset=0;}
//                return temp;
//            }
//        };
//    }
//
//    /**
//     * Return the size of the collection.
//     *
//     * @return The size as an int.
//     */
//    public int size() {
//        return base == null ? 0 : base.v.length * 2 - 1 - offset;
//    }
//
//    /**
//     * Equivalent to {@code size() == 0}
//     *
//     * @return true if {@code size() == 0}, false otherwise.
//     */
//    public boolean isEmpty() {
//        return base == null;
//    }
//
//    @Override
//    public @NotNull String toString() {
//        var out = new ArrayList<>();
//        for (Object o : this) {
//            out.add(o);
//        }
//        Collections.reverse(out); return out.toString();
//    }
//
//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (!(o instanceof FlatResultSeq)) return false;
//
//        var nextNode = base;
//        var nextNodeO = ((FlatResultSeq) o).base;
//        while (nextNode != null && Arrays.equals(nextNode.v, nextNodeO.v)) {
//            nextNode = nextNode.next;
//            nextNodeO = nextNodeO.next;
//        }
//        return nextNode == nextNodeO; // Guaranteed to be the same as (nextNode==null && nextNodeO==null).
//    }
//
//    @Override
//    public int hashCode() {
//        return hashCode;
//    }
//
//    /**
//     * Appends the input to the sequence. If the input is a {@link FlatResultSeq}, it is inserted into the instance flattened.
//     * <p>
//     * {@code null} inputs are ignored, as are empty {@link FlatResultSeq} inputs.
//     *
//     * @param obj Input.
//     * @return A new instance.
//     */
//    public @NotNull FlatResultSeq appendOrConcat(final @Nullable Object obj) {
//        if (obj == null)
//            return this;
//
//        if (!(obj instanceof FlatResultSeq)) {
//            if (base == null)
//                return new FlatResultSeq(new Node(null, new Object[]{obj}), 0, obj.hashCode());
//
//            final Node newBase;
//            final int newBaseSize;
//            final int newOff;
//
//            if (offset == 0) {
//                newBaseSize = base.v.length << 1;
//                var baseV = new Object[newBaseSize];
//                newBase = new Node(base, baseV);
//                newOff = newBaseSize - 1;
//            } else {
//                newBaseSize = base.v.length;
//                var baseV = Arrays.copyOf(base.v, newBaseSize);
//                newBase = new Node(base.next, baseV);
//                newOff = offset - 1;
//            }
//            newBase.v[newOff] = obj;
//            var hash = hashCode << 5 + obj.hashCode();
//            return new FlatResultSeq(newBase, newOff, hash);
//        }
//
//        var frs = (FlatResultSeq) obj;
////        System.out.println("HERE");
////        System.out.println(this + " " + frs);
//
//if (offset > frs.size()) {
//    var newOff = offset;
//    var newV = Arrays.copyOf(base.v, base.v.length);
//    for (Object o : frs) {
////        System.out.println(Arrays.toString(base.v) + " "+base.v.length + " " + offset+" "+Arrays.toString(newV)+ " " + newV.length + " " + newOff);
//        newOff--;
//        newV[newOff] = o;
//    }
//    var newBase = new Node(base.next, newV);
//    var hash = hashCode << 5 + obj.hashCode();
//    return new FlatResultSeq(newBase, newOff, hash);
//} else {
//   return appendNode(frs.base, this);
//}
//    }
//    private FlatResultSeq appendNode(Node node, FlatResultSeq result) {
//        if (node == null) return result;
//        result = appendNode(node.next, result);
//        for (Object o : node.v) {
//            result=result.appendOrConcat(o);
//        }
//        return result;
//    }
//}
