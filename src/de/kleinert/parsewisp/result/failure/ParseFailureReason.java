package de.kleinert.parsewisp.result.failure;

import de.kleinert.parsewisp.Print;
import de.kleinert.parsewisp.parsing.*;
import de.kleinert.parsewisp.parsing.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * A failure lists reasons for the failure. This class represents the possible reasons.
 *
 * @param rule            The rule was parsed when the failure appeared.
 * @param reasonString    A string describing the failure. Please prefer {@link ParseFailureReason#failureReasonString}.
 * @param untilEndOfInput Whether the production that failed covered the entire input from beginning to end. When showing the object as a string, this adds the note "(followed by end of string)" or something similar.
 * @param tag             A symbol or string indicating the type of reason. E.g. lookahead, string terminal, regex terminal, etc.
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
     */
    public String failureReasonString() {
        if (reasonString != null) return reasonString;
        return Print.ruleToString(rule);
    }

    /**
     * The tag of the production.
     *
     * @return The tag of the production.
     */
    public @NotNull String tag() {
        return tag;
    }

    /**
     * Whether the production that failed covered the entire input from beginning to end. When showing the object as a string, this adds the note "(followed by end of string)" or something similar.
     *
     * @return Whether the production that failed covered the entire input from beginning to end. When showing the object as a string, this adds the note "(followed by end of string)" or something similar.
     */
    public boolean untilEndOfInput() {
        return untilEndOfInput;
    }

    /**
     * Builds an instance based on a {@link ValueRangeTerm}.
     *
     * @param rule            The rule.
     * @param untilEndOfInput Whether the rule is followed by end-of-string.
     * @return A failure-reason based on the parameters.
     */
    public static @NotNull ParseFailureReason ofUnicodeChar(final @NotNull ValueRangeTerm rule, final boolean untilEndOfInput) {

        return new ParseFailureReason(
                rule,
                Arrays.toString(IntStream.range(rule.getLo(), rule.getHi() + 1).mapToObj(it -> (char) it).toArray()),
                untilEndOfInput, "char");
    }

    /**
     * Builds an instance based on a {@link EpsilonTerm}.
     *
     * @param rule            The rule.
     * @param untilEndOfInput Whether the rule is followed by end-of-string.
     * @return A failure-reason based on the parameters.
     */
    public static @NotNull ParseFailureReason ofEpsilon(final @NotNull EpsilonTerm rule, final boolean untilEndOfInput) {
        return new ParseFailureReason(rule, "end-of-string", untilEndOfInput, "epsilon");
    }

    /**
     * Builds an instance based on a {@link VariableRepetitionRule}.
     *
     * @param rule            The rule.
     * @param untilEndOfInput Whether the rule is followed by end-of-string.
     * @return A failure-reason based on the parameters.
     */
    public static @NotNull ParseFailureReason ofRepetition(final @NotNull VariableRepetitionRule rule, final boolean untilEndOfInput) {
        return new ParseFailureReason(rule, null, untilEndOfInput, "rep");
    }

    /**
     * Builds an instance based on a {@link LookaheadRule}.
     *
     * @param rule            The rule.
     * @param untilEndOfInput Whether the rule is followed by end-of-string.
     * @return A failure-reason based on the parameters.
     */
    public static @NotNull ParseFailureReason ofLookahead(final @NotNull LookaheadRule rule, final boolean untilEndOfInput) {
        return new ParseFailureReason(rule, untilEndOfInput ? "end-of-string" : null, untilEndOfInput, "look");
    }

    /**
     * Builds an instance based on a {@link NegativeLookaheadRule}.
     *
     * @param rule            The rule.
     * @param untilEndOfInput Whether the rule is followed by end-of-string.
     * @return A failure-reason based on the parameters.
     */
    public static @NotNull ParseFailureReason ofNegated(final @NotNull NegativeLookaheadRule rule, final boolean untilEndOfInput) {
        return new ParseFailureReason(rule, "NOT " + Print.ruleToString(rule.getRule()), untilEndOfInput, "neg");
    }

    /**
     * Builds an instance based on a {@link ExclusionRule}.
     *
     * @param rule            The rule.
     * @param untilEndOfInput Whether the rule is followed by end-of-string.
     * @return A failure-reason based on the parameters.
     */
    public static @NotNull ParseFailureReason ofExclusion(final @NotNull ExclusionRule rule, final boolean untilEndOfInput) {
        return new ParseFailureReason(rule,
                Print.ruleToString(rule.getParserExpected()) + " but NOT " + Print.ruleToString(rule.getParserExcluded()),
                untilEndOfInput, "exclude");
    }

    /**
     * Builds an instance based on a {@link OptionalRule}.
     *
     * @param rule            The rule.
     * @param untilEndOfInput Whether the rule is followed by end-of-string.
     * @return A failure-reason based on the parameters.
     */
    public static @NotNull ParseFailureReason ofOptional(final @NotNull OptionalRule rule, final boolean untilEndOfInput) {
        return new ParseFailureReason(rule, null, untilEndOfInput, "optional");
    }

    /**
     * Builds an instance based on a {@link RegexTerm}.
     *
     * @param rule            The rule.
     * @param untilEndOfInput Whether the rule is followed by end-of-string.
     * @return A failure-reason based on the parameters.
     */
    public static @NotNull ParseFailureReason ofRegexTerminal(final @NotNull RegexTerm rule, final boolean untilEndOfInput) {
        return new ParseFailureReason(rule, null, untilEndOfInput, "regex");
    }

    /**
     * Builds an instance based on a {@link StringTerm}.
     *
     * @param rule            The rule.
     * @param untilEndOfInput Whether the rule is followed by end-of-string.
     * @return A failure-reason based on the parameters.
     */
    public static @NotNull ParseFailureReason ofStringTerminal(final @NotNull StringTerm rule, final boolean untilEndOfInput) {
        return new ParseFailureReason(rule, null, untilEndOfInput, "string");
    }

    /**
     * Builds an instance based on a {@link SpecialSequenceRule}.
     *
     * @param rule            The rule.
     * @param untilEndOfInput Whether the rule is followed by end-of-string.
     * @return A failure-reason based on the parameters.
     */
    public static @NotNull ParseFailureReason ofSpecialSequence(final @NotNull SpecialSequenceRule rule, final boolean untilEndOfInput) {
        return new ParseFailureReason(rule, null, untilEndOfInput, "function");
    }
}
