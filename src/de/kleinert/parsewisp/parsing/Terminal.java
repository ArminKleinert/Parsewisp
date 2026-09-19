package de.kleinert.parsewisp.parsing;

import de.kleinert.parsewisp.reduction.ReductionType;
import org.jetbrains.annotations.NotNull;

/**
 * A general type of {@link Rule} for terminals (epsilon, regex, string, char).
 *
 * @since 0.9.7
 */
public abstract sealed class Terminal
        extends SimpleRule
        permits EOFTerm, EpsilonTerm, RegexTerm, StringTerm, ValueRangeTerm {
    /**
     * Constructor.
     *
     * @param hide The hide option.
     * @param red  The reduction type for this rule.
     * @since 0.9.7
     */
    protected Terminal(final boolean hide, final @NotNull ReductionType red) {
        super(hide, red);
    }
}
