package de.kleinert.parsewisp.result.failure;

import de.kleinert.parsewisp.grammar.GrammarPrinter;
import de.kleinert.parsewisp.parsing.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A failure lists reasons for the failure. This class represents the possible reasons.
 *
 * @param rule            The rule was parsed when the failure appeared.
 * @param reasonString    A string describing the failure. Please prefer {@link ParseFailureReason#failureReasonString}.
 * @param untilEndOfInput Whether the production that failed covered the entire input from beginning to end. When showing the object as a string, this adds the note "(followed by end of string)" or something similar.
 * @param tag             A symbol or string indicating the type of reason. E.g. lookahead, string terminal, regex terminal, etc.
 * @since 0.9.7
 */
public record ParseFailureReason(
        @NotNull Rule rule,
        @Nullable String reasonString,
        boolean untilEndOfInput,
        @NotNull String tag) {
    /**
     * Representation of the failure reasonList as a string.
     *
     * @return A representation of the expected production as a string.
     * @since 0.9.7
     */
    public String failureReasonString() {
        if (reasonString != null) return reasonString;
        return GrammarPrinter.getDefault().ruleToString(rule);
    }

    /**
     * The tag of the production.
     *
     * @return The tag of the production.
     * @since 0.9.7
     */
    public @NotNull String tag() {
        return tag;
    }

    /**
     * Whether the production that failed covered the entire input from beginning to end. When showing the object as a string, this adds the note "(followed by end of string)" or something similar.
     *
     * @return Whether the production that failed covered the entire input from beginning to end. When showing the object as a string, this adds the note "(followed by end of string)" or something similar.
     * @since 0.9.7
     */
    public boolean untilEndOfInput() {
        return untilEndOfInput;
    }

    /**
     * Creates a {@link ParseFailureReason} for the given inputs.
     *
     * @param rule     The rule.
     * @param untilEnd Whether the rule is followed by end-of-string.
     * @param printer  The printer used to create a string-representation of the failure.
     * @return A failure-reason based on the parameters.
     * @since 0.9.7
     */
    public static ParseFailureReason creatureFailReason(@NotNull Rule rule, boolean untilEnd, GrammarPrinter printer) {
        @Nullable String str = null;
        @NotNull String tag;

        if (rule instanceof ValueRangeTerm) {
            tag = "char";
        } else if (rule instanceof EpsilonTerm || rule instanceof EOFTerm) {
            tag = "epsilon";
            str = "end-of-string";
        } else if (rule instanceof VariableRepetitionRule) {
            tag = "rep";
        } else if (rule instanceof LookaheadRule) {
            str = untilEnd ? "end-of-string" : null;
            tag = "look";
        } else if (rule instanceof NegativeLookaheadRule) {
            str = "NOT " + printer.ruleToString(((NegativeLookaheadRule) rule).getRule());
            tag = "neg";
        } else if (rule instanceof ExclusionRule) {
            str = printer.ruleToString(((ExclusionRule) rule).getExpected()) + " but NOT " + printer.ruleToString(((ExclusionRule) rule).getExcluded());
            tag = "exclude";
        } else if (rule instanceof OptionalRule) {
            tag = "optional";
        } else if (rule instanceof RegexTerm) {
            tag = "regex";
        } else if (rule instanceof StringTerm) {
            tag = "string";
        } else if (rule instanceof SpecialSequenceRule) {
            tag = "function";
        } else {
            throw new IllegalArgumentException("Cannot create FailureReason for " + rule + " of class " + rule.getClass());
        }

        if (str == null) str = printer.ruleToString(rule);

        return new ParseFailureReason(rule, str, untilEnd, tag);
    }
}
