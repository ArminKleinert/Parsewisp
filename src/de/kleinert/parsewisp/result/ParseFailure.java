package de.kleinert.parsewisp.result;

import de.kleinert.parsewisp.result.failure.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Represents failures that occurred while parsing.
 *
 * @param index      The index where parsing failed.
 * @param reasonList The list of expected productions or other reasons why parsing failed.
 * @param line       The line where parsing failed, 1-indexed. This can be inferred from the string and index.
 * @param column     The column where parsing failed, 1-indexed. This can be inferred from the string and index.
 * @param text       The text that could not be matched.
 */
public record ParseFailure(int index,
                           @NotNull List<ParseFailureReason> reasonList,
                           int line,
                           int column,
                           @Nullable String text)
        implements ParseResult {
    /**
     * An alternative, shorter constructor. Other information will be added later separately.
     *
     * @param index      The index where parsing failed.
     * @param reasonList The list of expected things or other reasons why parsing failed.
     */
    public ParseFailure(final int index, final @NotNull List<ParseFailureReason> reasonList) {
        this(index, reasonList, -1, -1, null);
    }

    @Override
    public @NotNull String toString() {
        return FailureUtil.pprintFailure(this);
    }

    /**
     * Formats the failure into a string with all information included clearly. This is useful for debugging.
     *
     * @return A string for easy viewing.
     */
    public @NotNull String contentsToString() {
        return "[" + index + ", " + reasonList + ", " + line + ", " + column + ", " + text + "]";
    }
}
