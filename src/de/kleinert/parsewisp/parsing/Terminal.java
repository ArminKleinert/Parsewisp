package de.kleinert.parsewisp.parsing;

import de.kleinert.parsewisp.reduction.ReductionType;
import org.jetbrains.annotations.NotNull;

/**
 * A general type of {@link Rule} for terminals (epsilon, regex, string, char)
 */
public abstract sealed class Terminal
        extends SimpleRule
        permits EOFTerm, EpsilonTerm, RegexTerm, StringTerm, ValueRangeTerm {
    protected Terminal(final boolean hide, final @NotNull ReductionType red) {
        super(hide, red);
    }
}
