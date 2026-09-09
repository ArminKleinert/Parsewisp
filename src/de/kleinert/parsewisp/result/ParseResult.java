package de.kleinert.parsewisp.result;

import org.jetbrains.annotations.NotNull;

/**
 * A class for the results of single parses. This typically covers {@link ParseTree} and {@link ParseFailure}.
 */
public sealed interface ParseResult
        permits ParseTree, ParseFailure, ParseFailureNode {
    /**
     * Takes an object and returns an appropriate subtype associated with its type.
     *
     * @param o The input.
     * @return Output as {@link ParseResult}.
     */
    static @NotNull ParseResult make(final @NotNull Object o) {
        if (o instanceof ParseTree || o instanceof ParseFailure) {
            return (ParseResult) o;
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
     * Casts the result to an instance of {@link ParseFailure}.
     *
     * @return An instance of {@link ParseFailure}.
     */
    default @NotNull ParseFailure castToParseFailure() {
        return (ParseFailure) this;
    }
}
