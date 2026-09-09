package de.kleinert.parsewisp.error;

import de.kleinert.parsewisp.Parsewisp;
import de.kleinert.parsewisp.parser_options.ParserCreationOptions;
import org.jetbrains.annotations.NotNull;

/**
 * {@link RuntimeException} which occurs if the parser could not be created.
 *
 * @see Parsewisp#parser(String, ParserCreationOptions)
 */
public final class ParserCreationFailure extends RuntimeException {
    /**
     * {@link RuntimeException} which occurs if the parser could not be created.
     *
     * @param exception Argument.
     * @see Parsewisp#parser(String, ParserCreationOptions)
     */
    public ParserCreationFailure(@NotNull IllegalArgumentException exception) {
        super(exception);
    }

    /**
     * {@link RuntimeException} which occurs if the parser could not be created.
     *
     * @param exception Argument.
     * @see Parsewisp#parser(String, ParserCreationOptions)
     */
    public ParserCreationFailure(@NotNull IllegalGrammarException exception) {
        super(exception);
    }

    /**
     * {@link RuntimeException} which occurs if the parser could not be created.
     *
     * @param message Argument.
     * @see Parsewisp#parser(String, ParserCreationOptions)
     */
    public ParserCreationFailure(String message) {
        super(message);
    }
}
