package de.kleinert.parsewisp.result;

import de.kleinert.parsewisp.Parsewisp;
import de.kleinert.parsewisp.collections.PretenderList;
import de.kleinert.parsewisp.parser_options.ParsingOptions;
import de.kleinert.parsewisp.collections.LazySupplierList;
import de.kleinert.parsewisp.parser.Parser;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.IntFunction;

/**
 * A class for the results of multiple parses. This typically covers {@link LazyResultList} and {@link ParsesFailureResult}.
 */
public sealed interface ParsesResult
        extends List<ParseTree>
        permits ParsesResult.LazyResultList, ParsesResult.ParsesFailureResult, TotalParsesFailureNode {
    /**
     * Takes an object and returns an appropriate subtype associated with its type.
     *
     * @param o The input.
     * @return Output as {@link ParsesResult}.
     */
    static @NotNull ParsesResult make(final @NotNull Object o) {
        if (o instanceof TotalParsesFailureNode) return (TotalParsesFailureNode) o;
        if (o instanceof LazyResultList) return (LazyResultList) o;
        if (o instanceof ParsesFailureResult) return (ParsesFailureResult) o;
        if (o instanceof ParseFailure) return new ParsesFailureResult((ParseFailure) o);
        throw new IllegalArgumentException(o.getClass().toString());
    }

    /**
     * True if this is the result of a successful parse.
     *
     * @return True if this is the result of a successful parse.
     */
    default boolean isSuccess() {
        return this instanceof LazyResultList;
    }

    /**
     * Casts the result to a list of {@link ParseTree}.
     *
     * @return A list of {@link ParseTree}.
     */
    default @NotNull LazyResultList castToParsesSuccess() {
        if (!(this instanceof LazyResultList))
            throw new ClassCastException("Cannot cast failure to success.");
        return (LazyResultList) this;
    }

    /**
     * Casts the result to an instance of {@link ParsesFailureResult}.
     *
     * @return An instance of {@link ParsesFailureResult}.
     */
    default ParsesFailureResult castToParsesFailure() {
        return (ParsesFailureResult) this;
    }

    /**
     * Converts the parse forest input into a list, each parse tree is converted using {@link ParseTree#toRawList()}.
     *
     * @return The parse forest as a list of lists.
     */
    default List<List<Object>> toRawList() {
        if (!this.isSuccess())
            throw new ClassCastException("Cannot cast failure to success.");
        return stream().map(ParseTree::toRawList).toList();
    }

    /**
     * A class for successful results of parses.
     */
    final class LazyResultList extends LazySupplierList<ParseTree> implements ParsesResult {
        /**
         * Creates a new instance (a list of parse trees).
         *
         * @param nextFn     The function.
         * @param maxResults Maximum number of results.
         */
        public LazyResultList(final @NotNull IntFunction<ParseTree> nextFn, final int maxResults) {
            super(nextFn, maxResults);
        }
    }

    /**
     * This class is used for {@link Parser#parses(String, ParsingOptions)} to represent the failure.
     */
    final class ParsesFailureResult implements ParsesResult, PretenderList<ParseTree> {
        final @NotNull ParseFailure parseFailure;

        ParsesFailureResult(final @NotNull ParseFailure parseFailure) {
            this.parseFailure = parseFailure;
        }

        /**
         * Extracts the wrapped failure object.
         *
         * @return The wrapped failure.
         */
        public @NotNull ParseFailure asFailure() {
            return parseFailure;
        }

        @Override
        public ParseTree get(final int i) {
            Objects.checkIndex(i, 0);
            throw new IllegalStateException();
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof ParsesFailureResult that)) return false;
            if (!super.equals(o)) return false;
            return Objects.equals(parseFailure, that.parseFailure);
        }

        @Override
        public int hashCode() {
            return Objects.hash(parseFailure);
        }

        @Override
        public String toString() {
            return parseFailure.toString();
        }
    }
}
