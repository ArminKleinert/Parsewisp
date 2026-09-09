package de.kleinert.parsewisp.functions;

/**
 * A functional type for a procedure (no arguments, void output). {@link java.lang.Runnable} does the same thing, but has a different purpose.
 */
@FunctionalInterface
public interface Procedure {
    /**
     * Runs the procedure.
     */
    void execute();
}
