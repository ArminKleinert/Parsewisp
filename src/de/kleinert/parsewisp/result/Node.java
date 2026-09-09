package de.kleinert.parsewisp.result;

import de.kleinert.parsewisp.Sym;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This type is used for the parts of a {@link ParseTree}. An instance of this class wraps an object that can be visible inside a parse tree. Namely, these types are:
 * <ul>
 *     <li>{@link NodeString} wraps String.</li>
 *     <li>{@link NodeParseTree} wraps {@link ParseTree}.</li>
 *     <li>{@link NodeFail} wraps {@link ParseFailureNode}. This happens only if failures are embedded in parse trees.</li>
 *     <li>{@link NodeTreeTag} wraps {@link String} (or anything else that represents text) and must be the head of a parse tree.</li>
 * </ul>
 */
public sealed interface Node permits Node.NodeFail, Node.NodeParseTree, Node.NodeString, Node.NodeTreeTag {
    /**
     * Takes an object and returns a node representing that object.
     *
     * @param o The object.
     * @return A node.
     */
    static @NotNull Node of(final @Nullable Object o) {
        if (o == null) throw new IllegalArgumentException("Input cannot be null.");
        if (o instanceof ParseTree) return new NodeParseTree((ParseTree) o);
        if (o instanceof String) return new NodeString((String) o);
        if (o instanceof ParseFailureNode) return new NodeFail((ParseFailureNode) o);
        if (o instanceof Node) return (Node) o;
        if (o instanceof Sym)
            throw new IllegalArgumentException("Node.of should not be used on Symbols. Use NodeTreeTag explicitly.");
        throw new IllegalArgumentException(o.getClass().toString());
    }

    /**
     * Takes an object and returns a node representing that object.
     *
     * @param o The object.
     * @return A node.
     */
    static @NotNull Node of(final @NotNull ParseTree o) {
        return new NodeParseTree(o);
    }

    /**
     * Takes an object and returns a node representing that object.
     *
     * @param o The object.
     * @return A node.
     */
    static @NotNull Node of(final @NotNull String o) {
        return new NodeString(o);
    }

    /**
     * Takes an object and returns a node representing that object.
     *
     * @param o The object.
     * @return A node.
     */
    static @NotNull Node of(final @NotNull ParseFailureNode o) {
        return new NodeFail(o);
    }

    /**
     * The inner object.
     *
     * @return The inner object.
     */
    @NotNull Object content();

    /**
     * Casts the node to a {@link NodeString} and returns its content, treated as a String.
     *
     * @return The content as a string.
     * @throws ClassCastException If the node is not a {@link NodeString}.
     */
    default @NotNull String string() {
        return ((NodeString) this).content();
    }

    /**
     * Casts the node to a {@link NodeParseTree} and returns its content, treated as a {@link ParseTree}.
     *
     * @return The content as a {@link ParseTree}.
     * @throws ClassCastException If the node is not a {@link NodeParseTree}.
     */
    default @NotNull ParseTree tree() {
        return ((NodeParseTree) this).content();
    }

    /**
     * Represents the tag of a tree. This is the left hand side of a production. A tag can not exist without a tree and a tree can not exist without a tag.
     *
     * @param content The inner object.
     */
    record NodeTreeTag(@NotNull Sym content) implements Node {
        @Override
        public @NotNull String toString() {
            return content().toString();
        }
    }

    /**
     * Wraps a {@link ParseTree}.
     *
     * @param content The inner object.
     */
    record NodeParseTree(@NotNull ParseTree content) implements Node {
        @Override
        public @NotNull String toString() {
            return content().toString();
        }
    }

    /**
     * Wraps a {@link String}.
     *
     * @param content The inner object.
     */
    record NodeString(@NotNull String content) implements Node {
        @Override
        public @NotNull String toString() {
            return '"' + content() + '"';
        }
    }

    /**
     * Wraps a {@link ParseFailureNode} and is only used to embed failures into the tree.
     *
     * @param content The inner object.
     */
    record NodeFail(@NotNull ParseFailureNode content) implements Node {
        @Override
        public @NotNull String toString() {
            return content().toString();
        }
    }
}
