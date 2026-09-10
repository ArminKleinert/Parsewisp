package de.kleinert.parsewisp.result;

import de.kleinert.parsewisp.Sym;
import de.kleinert.parsewisp.collections.FlatResultSeq;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * This class represents parse trees. Throughout the documentation, trees are typically notated as lists.
 */
public final class ParseTree implements List<@NotNull Node>, ParseResult {
    /**
     * A technically invalid tag. It is used to mark trees "without" a tag. Such trees should always be subtrees of other parse trees and are "flattened" when used. See {@link #create(Node.NodeTreeTag, List, int, int)}
     */
    public static @NotNull Node.NodeTreeTag NULL_TAG = new Node.NodeTreeTag(Sym.sym("\0\0\0\0"));

    private final @NotNull Node.NodeTreeTag tag;
    private final @NotNull List<@NotNull Node> content;
    private int hashCode = 0;

    private final int spanStart;
    private final int spanEndExclusive;

    private ParseTree(final @NotNull Node.NodeTreeTag tag,
                      final @NotNull List<@NotNull Node> content,
                      final int spanStart,
                      final int spanEndExclusive) {
        this.tag = tag;
        this.content = content;

        if ((spanStart == -1 && spanEndExclusive != -1) || (spanStart != -1 && spanStart > spanEndExclusive))
            throw new IllegalArgumentException("Invalid span " + spanStart + " to " + spanEndExclusive + ".");

        this.spanStart = spanStart;
        this.spanEndExclusive = spanEndExclusive;
    }

    /**
     * The head of the tree. That is the name of the production used to generate the tree.
     * <p>
     * E.g. for the tree {@code [:S, "a", "b", "c"]}, the tag is {@code :S}.
     *
     * <pre>
     * {@code
     *   var pt = Parsewisp.parser("S := 'a' 'b' 'c'").parse("abc");
     *   println(pt); // [:S, a, b, c]
     *   println(pt.castToParseSuccess().getTag().content()); // :S
     * }
     * </pre>
     *
     * @return The tag of the tree.
     */
    public @NotNull Node.NodeTreeTag getTag() {
        return tag;
    }

    /**
     * The content of the tree, without the tag.
     *
     * <pre>
     * {@code
     *   var pt = Parsewisp.parser("S := 'a' 'b' 'c'").parse("abc");
     *   println(pt); // [:S, a, b, c]
     *   println(pt.castToParseSuccess().getContent()); // [a, b, c]
     * }
     * </pre>
     *
     * @return The content of the tree.
     */
    public @NotNull List<@NotNull Node> getContent() {
        return content;
    }

    /**
     * Equivalent to {@code getContent().get(index)}.
     *
     * @param index The index in content.
     * @return A node.
     */
    public @NotNull Node getNode(final int index) {
        return getContent().get(index);
    }

    /**
     * Returns the tag ({@link #getTag()} and content ({@link #getContent()}) into a single list.
     * This method is not recursive, ie does not apply to subtrees.
     *
     * @return The tree as a list of nodes.
     */
    public @NotNull List<@NotNull Node> toList() {
        final @NotNull List<@NotNull Node> alist = new ArrayList<>();
        if (!tag.equals(NULL_TAG)) alist.add(tag);
        alist.addAll(content);
        return Collections.unmodifiableList(alist);
    }

    /**
     * Creates a parse tree from a tag and content.
     * <p>
     * If {@param content} is null, the content list of the tree will be empty. If {@param content} is a {@link FlatResultSeq}, it becomes the content of the tree. Otherwise, {@param content} becomes a singleton list.
     *
     * @param tag     The tag as a node.
     * @param content The content as a node.
     * @return A new parse tree.
     */
    public static @NotNull ParseTree create(final @NotNull Sym tag,
                                            final @Nullable Object content) {
        return create(tag, content, -1, -1);
    }

    /**
     * Creates a parse tree from a tag and content.
     * <p>
     * If {@param content} is null, the content list of the tree will be empty. If {@param content} is a {@link FlatResultSeq}, it becomes the content of the tree. Otherwise, {@param content} becomes a singleton list.
     *
     * @param tag       The tag as a node.
     * @param content   The content as a node.
     * @param spanStart Starting index in the input (inclusive).
     * @param spanEnd   End index in the input (exclusive).
     * @return A new parse tree.
     */
    public static @NotNull ParseTree create(final @NotNull Sym tag,
                                            final @Nullable Object content,
                                            final int spanStart,
                                            final int spanEnd) {
        final @NotNull List<Node> afs;
        if (content == null) {
            afs = List.of();
        } else if (content instanceof FlatResultSeq) {
            afs = ((FlatResultSeq) content).stream().map(Node::of).toList();
        } else if (content instanceof String) {
            afs = List.of(Node.of(content));
        } else if (content instanceof TotalParsesFailureNode) {
            afs = List.of(Node.of(content));
        } else if (content instanceof ParseFailureNode) {
            afs = List.of(Node.of(content));
        } else if (content instanceof ParseTree) {
            afs = List.of(Node.of(content));
        } else if (content instanceof List<?>) {
            afs = ((List<?>) content).stream().map(Node::of).toList();
        } else {
            throw new IllegalArgumentException(content.getClass().toString());
        }
        return create(new Node.NodeTreeTag(tag), afs, spanStart, spanEnd);
    }

    /**
     * Creates a parse tree from a tag and content.
     * <p>
     * This method also "flattens" nested trees if necessary. Trees need to be flattened if the content includes any subtrees that have the {@link ParseTree#NULL_TAG}.
     * <p>
     * E.g. {@code [:S, "A", [:S, "A"]]} is flat, but {@code [:S, [NULL_TAG, "A"], [:S, "A"]]} is not.
     * This happens when hide-tags are used, for example in the grammar {@code "S := A S | EPS \n <A> := 'A'"}.
     *
     * @param tag       The tag as a node.
     * @param content   The content as a node.
     * @param spanStart Starting index in the input (inclusive).
     * @param spanEnd   End index in the input (exclusive).
     * @return A new parse tree.
     */
    public static @NotNull ParseTree create(final @NotNull Node.NodeTreeTag tag,
                                            final @NotNull List<Node> content,
                                            final int spanStart,
                                            final int spanEnd) {
        // If the content contains a ParseTree which has the NULL_TAG, it has to be "flattened".
        boolean isFlat = true;
        for (Node node : content) {
            if (node instanceof Node.NodeParseTree
                    && ((Node.NodeParseTree) node).content().tag.equals(NULL_TAG)) {
                // Subtree with NULL_TAG found. Must merge.
                isFlat = false;
                break;
            }
        }
        if (isFlat) {
            return new ParseTree(tag, content, spanStart, spanEnd);
        }

        // The rest of this method handles the case that there is an unflattened subtree.

        final @NotNull var entries = new ArrayList<@NotNull Node>();
        for (@NotNull var e : content) {
            if (!(e instanceof Node.NodeParseTree)) {
                entries.add(e);
                continue;
            }

            final @NotNull var subTree = ((Node.NodeParseTree) e).content();

            if (subTree.tag.equals(NULL_TAG)) {
                entries.addAll(subTree.getContent());
            } else {
                entries.add(Node.of(subTree));
            }
        }

        return new ParseTree(tag, entries, spanStart, spanEnd);
    }

    /**
     * Converts the tree into a nested list. Unlike {@link #toList()}, this method is recursive.
     *
     * @return A nested list of Objects.
     */
    public @NotNull List<@NotNull Object> toRawList() {
        int i = 0;

        final @NotNull Object[] l;
        if (tag.equals(NULL_TAG)) {
            l = new Object[content.size()];
        } else {
            l = new Object[content.size() + 1];
            l[i++] = tag.content();
        }

        for (@NotNull Node node : content) {
            if (node instanceof Node.NodeParseTree) {
                l[i++] = ((Node.NodeParseTree) node).content().toRawList();
            } else if (node instanceof Node.NodeTreeTag || node instanceof Node.NodeString || node instanceof Node.NodeFail) {
                l[i++] = node.content();
            } else {
                throw new IllegalArgumentException(node.getClass().toString());
            }
        }

        return Arrays.asList(l);
    }

    /**
     * The starting index in the input string of the parse (inclusive).
     * <pre>
     * {@code
     *         var p = Parsewisp.parser("S = 'b' A 'n'\nA = 'A'");
     *         var tree = p.parse("bAn").castToParseSuccess();
     *         System.out.println(tree.toString());            // [:S, b, [:A, A], n]
     *         System.out.println(tree.getSpanStart());        // 0
     *         System.out.println(tree.getSpanEndExclusive()); // 3
     * }
     * </pre>
     *
     * @return The starting index in the input string of the parse (inclusive).
     */
    public int getSpanStart() {
        return spanStart;
    }

    /**
     * The end index in the input string of the parse (exclusive).
     * <pre>
     * {@code
     *         var p = Parsewisp.parser("S = 'b' A 'n'\nA = 'A'");
     *         var tree = p.parse("bAn").castToParseSuccess();
     *         System.out.println(tree.toString());            // [:S, b, [:A, A], n]
     *         System.out.println(tree.getSpanStart());        // 0
     *         System.out.println(tree.getSpanEndExclusive()); // 3
     * }
     * </pre>
     *
     * @return The end index in the input string of the parse (exclusive).
     */
    public int getSpanEndExclusive() {
        return spanEndExclusive;
    }

    /**
     * Given a string (hopefully the one this tree was parsed from), will output the substring which is covered by this tree.
     * <pre>
     * {@code
     *         var p = Parsewisp.parser("S = 'b' A 'n'\nA = 'Aa'");
     *         var tree = p.parse("bAan").castToParseSuccess();
     *         var subTree = (ParseTree) tree.getContent().get(1).content();
     *         System.out.println(tree);                            // [:S, b, [:A, Aa], n]
     *         System.out.println(subTree);                         // [:A, Aa]
     *         System.out.println(subTree.getSpanStart());          // 1
     *         System.out.println(subTree.getSpanEndExclusive());   // 3
     *         System.out.println(subTree.containedString("bAan")); // Optional[Aa]
     * }
     * </pre>
     *
     * @param s Input string.
     * @return An {@link Optional} containing the string if present or an empty {@link Optional} if the string is not covered by this tree.
     * @throws IllegalArgumentException if this tree has no span specified.
     */
    public @NotNull Optional<@NotNull String> containedString(String s) {
        if (spanEndExclusive == -1) return Optional.empty();
        if (s.length() < spanEndExclusive) throw new IllegalArgumentException();
        return Optional.of(s.substring(spanStart, spanEndExclusive));
    }

    @Override
    public @NotNull String toString() {
        return toList().toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof List<?> c)) {
            return false;
        }
        if (o instanceof ParseTree that) {
            return Objects.equals(getTag(), that.getTag()) && Objects.equals(getContent(), that.getContent());
        }
        final @NotNull var otherIter = c.iterator();
        if (!otherIter.hasNext()) return false;

        for (var thisNext : this) {
            if (!otherIter.hasNext()) return false;
            final @NotNull var otherNext = otherIter.next();
            if (!Objects.equals(thisNext, otherNext)) return false;
        }

        return !otherIter.hasNext();
    }

    /* SECTION: Methods that allow treating the tree like a list. */

    @Override
    public int size() {
        return content.size() + 1;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean contains(Object o) {
        return Objects.equals(tag, o) || content.contains(o);
    }

    @Override
    public @NotNull Iterator<@NotNull Node> iterator() {
        return new Iterator<>() {
            Iterator<@NotNull Node> delegate = null;

            @Override
            public boolean hasNext() {
                return delegate == null || delegate.hasNext();
            }

            @Override
            public Node next() {
                if (delegate == null) {
                    delegate = content.iterator();
                    return tag;
                } else {
                    return delegate.next();
                }
            }
        };
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> collection) {
        return new HashSet<>(content).containsAll(collection);
    }

    @Override
    public @NotNull Object @NotNull [] toArray() {
        return toList().toArray();
    }

    @Override
    public @NotNull <T> T @NotNull [] toArray(@NotNull T @NotNull [] ts) {
        return toList().toArray(ts);
    }

    @Override
    public int hashCode() {
        if (hashCode != 0)
            return hashCode;
        int hc = 1;
        for (final var e : this)
            hc = hc * 31 + Objects.hashCode(e);
        hashCode = hc;
        return hc;
    }

    @Override
    public Node get(final int i) {
        if (i == 0) return tag;
        return content.get(i - 1);
    }

    @Override
    public int indexOf(final Object o) {
        if (Objects.equals(tag, o)) return 0;
        int i = content.indexOf(o);
        if (i < 0) return i;
        return i + 1;
    }

    @Override
    public int lastIndexOf(final Object o) {
        int i = content.lastIndexOf(o);
        if (i >= 0) return i + 1;
        if (Objects.equals(tag, o)) return 0;
        return -1;
    }

    @Override
    public @NotNull ListIterator<@NotNull Node> listIterator() {
        return toList().listIterator();
    }

    @Override
    public @NotNull ListIterator<@NotNull Node> listIterator(final int i) {
        return toList().listIterator();
    }

    @Override
    public @NotNull List<@NotNull Node> subList(final int i, final int i1) {
        if (i == 1 && i1 == content.size() + 1) return content;
        return toList().subList(i, i1);
    }

    /* SECTION: Methods that always throw UnsupportedOperationException. */

    @Override
    public boolean add(Node node) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends Node> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(int i, @NotNull Collection<? extends Node> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Node set(final int i, final @NotNull Node node) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(final int i, final @NotNull Node node) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Node remove(final int i) {
        throw new UnsupportedOperationException();
    }
}
