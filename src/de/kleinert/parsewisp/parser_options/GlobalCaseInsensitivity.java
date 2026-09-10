package de.kleinert.parsewisp.parser_options;

/**
 * Used for determining whether all string terminals should be made case-insensitive when parsing.
 */
public enum GlobalCaseInsensitivity {
    /**
     * Yes, always parse case-insensitive.
     */
    TRUE,
    /**
     * No, never.
     */
    FALSE,
    /**
     * Eh, depends. E.g. for ABNF, would be true. False for EBNF. Leave it to the implementation of Parsewisp.
     */
    DEFAULT
}
