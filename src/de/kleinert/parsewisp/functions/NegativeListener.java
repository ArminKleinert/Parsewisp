package de.kleinert.parsewisp.functions;

/**
 * Equivalent type to a {@link Procedure}, but the name is clearer. This type is used for negative lookaheads when parsing.
 */
@FunctionalInterface
public interface NegativeListener {
    /**
     * Executes the function.
     */
    void execute();
}
