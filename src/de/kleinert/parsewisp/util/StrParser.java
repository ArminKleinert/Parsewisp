package de.kleinert.parsewisp.util;

import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

/**
 * Helper functions for creating parsers from strings.
 *
 * @since 0.9.7
 */
public final class StrParser {
    final @NotNull StringBuilder sb;

    /**
     * The constructor initializes a StringBuilder or similar object for internal use.
     *
     * @since 0.9.7
     */
    public StrParser() {
        sb = new StringBuilder();
    }

    private @NotNull String unescape(final int offset, final int backOffset, final char starter, final @NotNull String s) {
        sb.setLength(0);
        for (int i = offset; i < s.length() - backOffset; i++) {
            char c = s.charAt(i);

            if (c == starter) {
                break;
            }

            if (c == '\\') {
                if (i + 1 >= s.length())
                    throw new IllegalArgumentException("Encountered backslash character at end of string: " + s);

                i++;
                final char c2 = s.charAt(i);

                if (c2 == starter) {
                    sb.append(c2);
                } else {
                    switch (c2) {
                        case 'b' -> sb.append('\b');
                        case 'f' -> sb.append('\f');
                        case 'n' -> sb.append('\n');
                        case 'r' -> sb.append('\r');
                        case 't' -> sb.append('\t');
                        case 'u' -> {
                            i += 4;
                            sb.appendCodePoint(Integer.parseInt(s, i - 3, i + 1, 16));
                        }
                        case '\\' -> sb.append('\\');
                        default -> throw new IllegalArgumentException(s + " at " + i + " ; sb=" + sb);
                    }
                }
            } else {
                sb.append(c);
            }
        }

        return sb.toString();
    }

    private @NotNull String unescapeRegex(final int offset, final int backOffset, final char starter, final @NotNull String s) {
        sb.setLength(0);
        for (int i = offset; i < s.length() - backOffset; i++) {
            char c = s.charAt(i);

            if (c == starter) {
                break;
            }

            if (c == '\\') {
                if (i + 1 >= s.length())
                    throw new IllegalArgumentException("Encountered backslash character at end of string: " + s);

                i++;
                final char c2 = s.charAt(i);
                if (c2 == starter) {
                    sb.append(c2);
                } else {
                    sb.append('\\').append(c2);
                }
            } else {
                sb.append(c);
            }
        }

        return sb.toString();
    }

    /**
     * Unescapes a (non-empty) string. Handles most escape sequences, including Unicode chars (starting with "\\u"), but not octal escape sequences.
     *
     * @param s The (non-empty) input string. Format: '...' or "..."
     * @return A string.
     * @since 0.9.7
     */
    public @NotNull String processString(final @NotNull String s) {
        return processString(s, 1, 1);
    }

    /**
     * Unescapes a string. Handles most escape sequences, including Unicode chars (starting with "\\u"), but not octal escape sequences. Both offsets are assumed to be 0 or greater.
     *
     * @param s          The (non-empty) input string. Format: '...' or "...", but could be anything.
     * @param offset     The offset from index 0. For example, a string of the form "..." would use offset 1, but a string of the form ''...'' would use offset 2.
     * @param backOffset The offset from the end. This separates the final character of the string. For example, a string of the form "..." would use backOffset 1, but a string of the form ''...'' would use backOffset 2.
     * @return A string.
     * @since 0.9.7
     */
    public @NotNull String processString(final @NotNull String s, int offset, int backOffset) {
        return unescape(offset, backOffset, s.charAt(0), s);
    }

    /**
     * Unescapes a (non-empty) string and turns it into a {@link Pattern}. Handles most escape sequences, including Unicode chars (starting with "\\u"), but not octal escape sequences. Both offsets are assumed to be 0 or greater.
     *
     * @param s          The (non-empty) input string. Format: #'...' or #"...", but can be anything.
     * @param offset     The offset from index 0. For example, a regex of the form #"..." would use offset 2, a regex of the form `...` would use offset 1.
     * @param backOffset The offset from the end. This separates the final character of the string. For example, a regex of the form #"..." would use backOffset 1, but a regex of the form #''...'' would use backOffset 2.
     * @return A regex.
     * @since 0.9.7
     */
    public @NotNull Pattern processRegexp(final @NotNull String s, final int offset, final int backOffset) {
        return Pattern.compile(unescapeRegex(offset, backOffset, s.charAt(offset - 1), s));
    }
}
