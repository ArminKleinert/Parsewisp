package de.kleinert.parsewisp.parsing;

import de.kleinert.parsewisp.reduction.ReductionType;
import org.jetbrains.annotations.NotNull;

/**
 * A general type of {@link Rule} for classes which do not have child-rules. This includes Terminals and NonTerminals, but not repetitions.
 */
public abstract sealed class SimpleRule
        extends Rule
        permits Terminal, NonTerminal, SpecialSequenceRule {
    /**
     * Constructor.
     *
     * @param hide The hide option.
     * @param red  The reduction type for this rule.
     */
    protected SimpleRule(final boolean hide, final @NotNull ReductionType red) {
        super(hide, red);
    }
}
