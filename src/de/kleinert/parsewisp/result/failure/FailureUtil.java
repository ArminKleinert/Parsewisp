package de.kleinert.parsewisp.result.failure;

import de.kleinert.parsewisp.result.ParseFailure;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Utilities for printing and modifying failure objects. Calling these methods from outside the parsing algorithm is heavily discouraged and might result in immediate termination of the user.
 */
public final class FailureUtil {
    private FailureUtil() {
    }

    /**
     * Creates string with caret at nth position, 1-based
     * and accounts for horizontal tabs which might change
     * the alignment of the '^' to the error location.
     *
     * @param failure The failure.
     * @param text    The text.
     * @return A new string.
     */
    public static @NotNull ParseFailure augmentFailure(
            final @NotNull ParseFailure failure,
            final @NotNull String text) {
        int line = 1;
        int col = 1;

        final int index = failure.index();

        final PrimitiveIterator.OfInt charCodes = text.chars().iterator();
        for (int counter = 0; counter < index; counter++) {
            final int code = charCodes.nextInt();
            if (code == (int) '\n') {
                line++;
                col = 1;
//            } else if (code == (int) '\t') {
//                col += 4;
            } else {
                col++;
            }
        }

        final @NotNull Optional<String> lineText = text.lines().skip(line - 1).findFirst();
        return new ParseFailure(index, failure.reasonList(), line, col, lineText.orElse(null));
    }

    /**
     * Creates string with caret at nth position, 1-based
     * and accounts for horizontal tabs which might change
     * the alignment of the '^' to the error location.
     *
     * @param text The text.
     * @param n    The index.
     * @return Indentation and marker.
     */
    public static @NotNull String marker(final String text, final int n) {
        if (text == null) return "<No text>";

        final Pattern reg = Pattern.compile("\\S");
        final String markerText = reg.matcher(text).replaceAll(" ");
        if (n <= 1) return "^";
        return markerText.substring(0, n - 1) + '^';
    }

    /**
     * Adds a failure reasonList to a failure object if the next index (parameter8 us greater than the failure's index.
     *
     * @param failure   The failure object.
     * @param newReason The new reasonList.
     * @param nextIndex The next index for the reasonList.
     * @return A modified failure object.
     */
    public static @NotNull ParseFailure modifyFailureByIndex(
            final @Nullable ParseFailure failure,
            final ParseFailureReason newReason,
            final int nextIndex) {
        final int currentIndex = failure == null ? 0 : failure.index();
        if (nextIndex > currentIndex)
            return new ParseFailure(nextIndex, new ArrayList<>(Collections.singletonList(newReason)));
        if (nextIndex < currentIndex) return Objects.requireNonNull(failure);

        final List<ParseFailureReason> newReasonList = new ArrayList<>(failure == null ? List.of() : failure.reasonList());
        newReasonList.add(newReason);
        return new ParseFailure(nextIndex, newReasonList);
    }

    /**
     * Creates a nicely formatted string for a failure object.
     *
     * @param failure The failure.
     * @return The string.
     */
    public static @NotNull String pprintFailure(final @NotNull ParseFailure failure) {
        final int line = ((Number) failure.line()).intValue();
        final int column = ((Number) failure.column()).intValue();
        final String text = failure.text();
        final List<ParseFailureReason> reason = failure.reasonList();

        final StringBuilder sb = new StringBuilder();
        sb.append("Parse error at line ").append(line).append(", column ").append(column).append(";\n");
        if (text != null) {
            sb.append(text).append('\n');
            sb.append(marker(text, column)).append('\n');
        } else {
            sb.append("<No text provided.>\n");
        }

        final List<String> fullReasons = reason.stream()
                .filter(ParseFailureReason::untilEndOfInput)
                .map(ParseFailureReason::failureReasonString)
                .distinct().toList();
        final List<String> partialReasons =
                reason.stream()
                        .filter(Predicate.not(ParseFailureReason::untilEndOfInput))
                        .map(ParseFailureReason::failureReasonString)
                        .distinct().toList();

        final int total = fullReasons.size() + partialReasons.size();

        if (total == 1) {
            sb.append("Expected:").append('\n');
        } else if (total > 1) {
            sb.append("Expected one of:").append('\n');
        }

        for (String fullReasonExpect : fullReasons) {
            sb.append(fullReasonExpect);
            if (!fullReasonExpect.equals("end-of-string"))
                sb.append(" (follow by end-of-string)");
            sb.append('\n');
        }

        for (String partialReasonExpect : partialReasons) {
            sb.append(partialReasonExpect).append('\n');
        }

        return sb.toString();
    }
}
