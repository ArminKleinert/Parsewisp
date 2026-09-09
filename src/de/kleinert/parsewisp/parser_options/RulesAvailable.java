package de.kleinert.parsewisp.parser_options;

import de.kleinert.parsewisp.parsing.*;

/**
 * Rules that can be used when building parsers. Some appear only in EBNF, some only in ABNF.
 * <p>
 * If some kind of rule is not allowed, there are alternatives for most.
 * Check the source code of {@code RuleAlternativesTests} (cannot link here because that file is in the "test" folder).
 */
public enum RulesAvailable {
    /**
     * Various ABNF rules.
     *
     * <pre>
     * {@code
     * ALPHA  = #"[a-zA-Z]"
     * BIT    = #"[01]"
     * CHAR   = #"[\\u0001-\\u007F]"        // 7-bit ascii, excloding NULL
     * CR     = "\r"                        // Carriage return
     * CRLF   = "\r\n"                      // Carriage return + line feed
     * CTL    = #"[\\u0000-\\u001F|\\u007F]"
     * DIGIT  = #"[0-9]"
     * DQUOTE = "\""                        // Double quote character
     * HEXDIG = #"[0-9a-fA-F]"
     * HTAB   = #"\t"                       // Horizontal tab
     * LF     = #"\n"                       // Line feed
     * LWSP   = *(WSP / CRLF WSP)
     * OCTET  = #"[\\u0000-\\u00FF]"
     * SP     = " "                         // Space
     * VCHAR  = #"[\\u0021-\\u007E]"
     * WSP    = SP / HTAB                   // Space or horizontal tag
     * }
     * </pre>
     * <p>
     * Possible replacements through other rule types: See above.
     */
    ABNF_CORE,

    /**
     * "Alternation" or "choice" rule.
     * <p>
     * Notation: {@code rule1 | rule2}
     * <p>
     * Possible replacements through other rule types: None.
     *
     * @see AlternationRule
     */
    ALTERNATION,

    /**
     * EBNF "Syntactic exception" or "except"
     * <p>
     * Notation: {@code rule1 - rule2}
     * <p>
     * Possible replacements through other rule types: None, but {@link #NEGATIVE_LOOKAHEAD} comes close.
     *
     * @see ExclusionRule
     */
    EXCLUSION,

    /**
     * Explicit EOF (end-of-file) rule. This can be useful in *very* specific circumstances, for example a negative lookahead which contains an EOF.
     * <p>
     * Notation: See {@link EOFTerm}
     * <p>
     * Possible replacements through other rule types: None.
     *
     * @see EOFTerm
     */
    EXPLICIT_EOF,

    /**
     * EBNF identifiers normally must have the following form: {@code letter (letter|digit|_)+}. ABNF and BNF use the "-" character in place of the "_".
     * <p>
     * Possible replacements through other rule types: N.A.
     */
    ABNF_IDENTIFIERS,

    /**
     * EBNF identifiers normally must have the following form: {@code letter (letter|digit|_)+}. ABNF and BNF also allow the "-" character.
     * <p>
     * With this option, any character can be used except those which are used for other purposes:
     * {@code " ' ! ? + * [ ] ( ) { } < > : = / | # & } and whitespace characters.
     * <p>
     * This means that the production {@code 🎁 = "a"} becomes legal.
     * <p>
     * Possible replacements through other rule types: N.A.
     */
    EXTENDED_IDENTIFIERS,

    /**
     * "Lookahead" or "expect" rule.
     * <p>
     * Notation: {@code &look rule}.
     * <p>
     * Example: {@code &'a' ('a' | 'b')} means "use the alternation {@code a|b}, but start with an {@code 'a'}.". I can't think of better examples.
     * <p>
     * Possible replacements through other rule types: None.
     *
     * @see LookaheadRule
     */
    LOOKAHEAD,

    /**
     * Negative lookahead.
     * <p>
     * Notation: {@code !look rule}
     * <p>
     * Example: {@code !'a' ('a' | 'b')} means "use the alternation {@code a|b}, but do NOT start with an {@code 'a'}.". I can't think of better examples.
     * <p>
     * Possible replacements through other rule types: None.
     *
     * @see NegativeLookaheadRule
     */
    NEGATIVE_LOOKAHEAD,

    /**
     * "Zero or once" or "Optional" rule.
     * <p>
     * Notation: {@code [rule]}
     * <p>
     * Possible replacements through other rule types:
     * {@code S = [rule]} can be replaced by {@code S = rule | epsilon}
     *
     * @see RulesAvailable#OPTIONAL_QUERY
     * @see OptionalRule
     */
    OPTIONAL,

    /**
     * Similar to {@link #OPTIONAL}, but different notation.
     * <p>
     * Notation: {@code rule?}
     * <p>
     * Possible replacements through other rule types: Equivalent to {@link #OPTIONAL}.
     *
     * @see RulesAvailable#OPTIONAL
     * @see OptionalRule
     */
    OPTIONAL_QUERY,

    /**
     * "Zero or more" repetition.
     * <p>
     * Notation: {@code {rule}}
     * <p>
     * Possible replacements through other rule types:
     * Can be replaced by using {@link #ALTERNATION} and more productions.
     *
     * @see RulesAvailable#OPTIONAL_REPETITION_STAR
     * @see ZeroOrMoreRule
     */
    OPTIONAL_REPETITION,

    /**
     * "Zero or more" repetition. Similar to {@link #OPTIONAL_REPETITION}, but different notation.
     * <p>
     * Notation: {@code rule*}
     * <p>
     * Possible replacements through other rule types: Equivalent to {@link #OPTIONAL_REPETITION}.
     *
     * @see RulesAvailable#OPTIONAL_REPETITION
     * @see ZeroOrMoreRule
     */
    OPTIONAL_REPETITION_STAR,

    /**
     * ABNF-style {@link OrderedChoiceRule} '/' with the extension that the output should be ordered and deterministic.
     * <p>
     * Notation: {@code rule1 / rule2}
     * <p>
     * Possible replacements through other rule types: None, but {@link #ALTERNATION} is close enough.
     *
     * @see OrderedChoiceRule
     */
    ORDERED_CHOICE,

    /**
     * "Once or more" repetition.
     * <p>
     * Notation: {@code rule+}
     * <p>
     * Possible replacements through other rule types:
     * Can be replaced by using {@link #OPTIONAL_REPETITION}.
     *
     * @see OnceOrMoreRule
     */
    PLUS,

    /**
     * Regex rules.
     * <p>
     * Notation: {@code #'...'} or {@code #"..."}
     * <p>
     * Possible replacements through other rule types:
     * Can be replaced by using combinations of all other kinds of rules, mainly Strings.
     *
     * @see RegexTerm
     */
    REGEX,

    /**
     * Single-quoted strings are technically not allowed by ABNF.
     * <p>
     * Possible replacements through other rule types:
     * {@code S = 'a'} can be safely replaced by {@code S = "a"}, but this requires escaping the quotation-marks in code.
     *
     * @see StringTerm
     */
    SINGLY_QUOTED,

    /**
     * EBNF special sequence.
     * <p>
     * The notation {@code ?...?} is not implemented currently.
     * <p>
     * Possible replacements: None.
     *
     * @see SpecialSequenceRule
     */
    SPECIAL_SEQUENCE,

    /**
     * ABNF string prefixes {@code %i"..."} for case insensitivity and {@code %s"..."} for forced case sensitivity.
     * <p>
     * {@code S = %i"..."} is already implicit in ABNF, but needs to be explicit in other formats.
     * <br/>
     * {@code S = %s"..."} is already implicit in EBNF, but needs to be explicit in ABNF.
     * <p>
     * Possible replacements through other rule types: None.
     */
    STRING_CASE_SENSITIVITY_PREFIX,

    /**
     * ABNF value range.
     * <p>
     * Notation: {@code %xXXXX} or {@code %xXXXX-XXXX}, {@code %bBBBB} or {@code %bBBBB-BBBB}, {@code %dDDDD} or {@code %dDDDD-DDDD}. "X" denotes a hexadecimal digit, "B" denotes a binary digit, "O" (uppercase o) denotes an octal digit.
     * <p>
     * Possible replacements through other rule types:
     * A value range can be replaced by a regex or an alternation of string terminals.
     * If you need multiple characters outside the range of 16-bit characters, value ranges become useful.
     *
     * @see ValueRangeTerm
     */
    VALUE_RANGE,

    /**
     * ABNF "counted repetition" or "variable repetition" rule, notated by a star-prefix.
     * <p>
     * Notation: {@code n*m rule} or {@code n* rule} or {@code *m rule} or {@code n rule} or {@code * rule} (this is only available if {@link RulesAvailable#OPTIONAL_REPETITION_STAR} is not allowed).
     * <p>
     * Possible replacements through other rule types:
     * <ul>
     *     <li>{@code n*m rule} can be replaced by using {@code n} occurrences of the rule followed by {@code m-n} {@link #OPTIONAL} occurrences of the rule</li>
     *     <li>{@code n* rule} can be replaced by using {@code n} occurrences of the rule followed by an {@link #OPTIONAL_REPETITION} of the rule</li>
     *     <li>{@code *m rule} can be replaced by using {m} {@link #OPTIONAL} occurrences of the rule</li>
     *     <li>{@code n rule} is equivalent to n occurrences of the rule</li>
     *     <li>{@code * rule} is equivalent to {@link #OPTIONAL_REPETITION}</li>
     * </ul>
     *
     * @see VariableRepetitionRule
     */
    VARIABLE_REPEAT,
}
