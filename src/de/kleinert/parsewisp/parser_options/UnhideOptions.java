package de.kleinert.parsewisp.parser_options;

import de.kleinert.parsewisp.grammar.Grammar;

/**
 * See {@link Unhide} for documentation and an example.
 *
 * @since 0.9.7
 */
public enum UnhideOptions {
    /**
     * Do nothing.
     *
     * @see ParsingOptions#unhide()
     * @since 0.9.7
     */
    NONE,
    /**
     * Unhide tags, do not show hidden contents.
     *
     * @see Unhide#unhideTags(Grammar)
     * @see ParsingOptions#unhide()
     * @since 0.9.7
     */
    TAGS,
    /**
     * Unhide contents, but keep tags hidden.
     *
     * @see Unhide#unhideContent(Grammar)
     * @see ParsingOptions#unhide()
     * @since 0.9.7
     */
    CONTENT,
    /**
     * Show both contents and tags.
     *
     * @see Unhide#unhideAll(Grammar)
     * @see ParsingOptions#unhide()
     * @since 0.9.7
     */
    ALL
}
