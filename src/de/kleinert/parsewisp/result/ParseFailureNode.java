package de.kleinert.parsewisp.result;

import de.kleinert.parsewisp.Sym;
import org.jetbrains.annotations.NotNull;

/**
 * This class represents parse failure embedded into a parse tree when parsing only for the first parse.
 *
 * @param text  The text.
 * @param key   The production name which failed.
 * @param start Start index of the failure.
 * @param end   End index of the failure.
 */
public record ParseFailureNode(
        @NotNull String text,
        @NotNull Sym key,
        int start,
        int end) implements ParseResult {
    @Override
    public @NotNull String toString() {
        return "[" + key + ", could not parse \"" + text + "\" at " + start + ".." + end + "]";
    }
}
