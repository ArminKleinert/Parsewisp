package de.kleinert.parsewisp.error;

/**
 * Exception which occurs if a grammar is invalid.
 *
 * @since 0.9.7
 */
public final class IllegalGrammarException extends RuntimeException {
    /**
     * Exception which occurs if a grammar is invalid.
     *
     * @param message Message.
     * @since 0.9.7
     */
    public IllegalGrammarException(String message) {
        super(message);
    }
}
