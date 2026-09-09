package de.kleinert.parsewisp.functions;

import de.kleinert.parsewisp.result.success.ParseMessage;
import org.jetbrains.annotations.NotNull;

/**
 * A functional type which takes a {@link ParseMessage} as an input and returns nothing.
 * This is equivalent to a {@link java.util.function.Consumer} taking an {@link ParseMessage}, but the name is a bit clearer.
 */
@FunctionalInterface
public interface Listener {
    /**
     * Runs the function.
     * @param o The input.
     */
    void execute(final @NotNull ParseMessage o);
}
