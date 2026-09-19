package de.kleinert.parsewisp.parser_options;

import de.kleinert.parsewisp.parsing.AlternationRule;

/**
 * Options for deciding what to do when a production is added that already exists.
 * <p>
 * Consider the following grammar:
 * <pre>
 * {@code
 *      S = A
 *      S = B
 *      S = C
 * }
 * </pre>
 * The question this class tries to answer is "what to do?". The parser knows thanks to these options.
 * <ul>
 *     <li>{@link RedefinitionOption#OVERRIDE}: {@code S = C}</li>
 *     <li>{@link RedefinitionOption#ERROR}: Fails.</li>
 *     <li>{@link RedefinitionOption#CHOICE}: {@code S = A | B | C}</li>
 *     <li>{@link RedefinitionOption#KEEP}: {@code S = A}</li>
 * </ul>
 *
 * @since 0.9.7
 */
public enum RedefinitionOption {
    /**
     * Ignore existing. Replace and forget.
     * <p>
     * Example: Adding Grammar productions "S = A" and "S = "B" results in "S = B" and discards the first.
     *
     * @since 0.9.7
     */
    OVERRIDE,

    /**
     * Throw exception if a duplicate is added.
     * <p>
     * Example: Adding Grammar productions "S = A" and "S = "B" results in an error.
     *
     * @since 0.9.7
     */
    ERROR,

    /**
     * Create an {@link AlternationRule}.
     * <p>
     * Example: Adding Grammar productions "S = A" and "S = "B" creates a new production "S = A | B".
     *
     * @since 0.9.7
     */
    CHOICE,

    /**
     * Keep old value.
     * <p>
     * Example: Adding Grammar productions "S = A" and "S = "B" keeps "S = A".
     *
     * @since 0.9.7
     */
    KEEP;

    /**
     * Default setting.
     *
     * @since 0.9.7
     */
    public final static RedefinitionOption defaultOption = ERROR;
}
