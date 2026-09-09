package de.kleinert.parsewisp.parsing;

import de.kleinert.parsewisp.reduction.ReductionType;
import org.jetbrains.annotations.NotNull;

abstract sealed class SimpleRule
        extends Rule
        permits Terminal, NonTerminal, SpecialSequenceRule {
    protected SimpleRule(final boolean hide, final @NotNull ReductionType red) {
        super(hide, red);
    }
}
