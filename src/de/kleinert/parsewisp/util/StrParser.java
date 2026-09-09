package de.kleinert.parsewisp.util;

import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

/**
 * Helper functions for creating parsers from strings.
 */
public final class StrParser {
    final @NotNull StringBuilder sb;

    /**
     * The constructor initializes a StringBuilder or similar object for internal use.
     */
    public StrParser() {
        sb = new StringBuilder();
    }

    private @NotNull String unescape(final int offset, final char starter, final @NotNull String s) {
        sb.setLength(0);
        for (int i = offset; i < s.length() - 1; i++) {
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
                            i += 5;
                            sb.appendCodePoint(Integer.parseInt(s, i - 4, i, 16));
                        }
                        case '\\' -> sb.append('\\');
                        default -> sb.append('\\').append(c2);
                    }
                }
            } else {
                sb.append(c);
            }
        }

        return sb.toString();
    }

    /**
     * Unescapes a string. Handles most escape sequences, including Unicode chars (starting with "\\u"), but not octal escape sequences.
     *
     * @param s The input string. Format: '...' or "..."
     * @return A string.
     */
    public @NotNull String processString(final @NotNull String s) {
        return unescape(1, s.charAt(0), s);
    }

    /**
     * Unescapes a string and turns it into a {@link Pattern}. Handles most escape sequences, including Unicode chars (starting with "\\u"), but not octal escape sequences.
     *
     * @param s The input string. Format: #'...' or #"..."
     * @return A regex.
     */
    public @NotNull Pattern processRegexp(final @NotNull String s) {
        return Pattern.compile(unescape(2, s.charAt(1), s));
    }
}
