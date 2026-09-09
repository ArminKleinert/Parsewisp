package de.kleinert.parsewisp.error;

/**
 * Exception which occurs if a grammar is invalid.
 */
public final class IllegalGrammarException extends RuntimeException {
    /**
     * Exception which occurs if a grammar is invalid.
     *
     * @param message Message.
     */
    public IllegalGrammarException(String message) {
        super(message);
    }
}
