package alphaparse.result;

import org.jetbrains.annotations.NotNull;

/**
 * A class for the results of single parses. This typically covers {@link ParseTree} and {@link AlphaParseFailure}.
 */
public sealed interface AlphaParseResult
        permits ParseTree, AlphaParseFailure, ParseFailureNode {
    /**
     * Takes an object and returns an appropriate subtype associated with its type.
     *
     * @param o The input.
     * @return Output as {@link AlphaParseResult}.
     */
    static @NotNull AlphaParseResult make(final @NotNull Object o) {
        if (o instanceof ParseTree || o instanceof AlphaParseFailure) {
            return (AlphaParseResult) o;
        }
        throw new IllegalArgumentException(o.getClass().toString());
    }

    /**
     * True if this is the result of a successful parse.
     *
     * @return True if this is the result of a successful parse.
     */
    default boolean isSuccess() {
        return this instanceof ParseTree;
    }

    /**
     * True if this is the result of a failed parse.
     *
     * @return True if this is the result of a failed parse.
     */
    default boolean isFailure() {
        return !isSuccess();
    }

    /**
     * Casts the result to a {@link ParseTree}.
     *
     * @return A {@link ParseTree}.
     */
    default @NotNull ParseTree castToParseSuccess() {
        return (ParseTree) this;
    }

    /**
     * Casts the result to an instance of {@link AlphaParseFailure}.
     *
     * @return An instance of {@link AlphaParseFailure}.
     */
    default @NotNull AlphaParseFailure castToParseFailure() {
        return (AlphaParseFailure) this;
    }
}
