package de.kleinert.parsewisp.functions;

/**
 * Equivalent type to a {@link Procedure}, but the name is clearer. This type is used for negative lookaheads when parsing.
 *
 * @since 0.9.7
 */
@FunctionalInterface
public interface NegativeListener {
    /**
     * Executes the function.
     *
     * @since 0.9.7
     */
    void execute();
}
