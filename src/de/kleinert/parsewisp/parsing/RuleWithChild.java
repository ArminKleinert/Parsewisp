package de.kleinert.parsewisp.parsing;

import de.kleinert.parsewisp.reduction.ReductionType;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Abstraction of {@link Rule} types which wrap one other {@link Rule}. Examples: repetitions, optionals, lookahead.
 */
public sealed abstract class RuleWithChild
        extends Rule
        permits LookaheadRule, NegativeLookaheadRule, OptionalRule, OnceOrMoreRule, VariableRepetitionRule, ZeroOrMoreRule {
    private long bufferedHashCode = Long.MIN_VALUE;
    protected final @NotNull Rule rule;

    RuleWithChild(final boolean hide,
                  final @NotNull ReductionType red,
                  final @NotNull Rule rule) {
        super(hide, red);
        this.rule = rule;
    }

    /**
     * Get the inner {@link Rule} used for parsing. For example, for a repetition {@code P+}, returns {@code P}.
     *
     * @return The inner {@link Rule}.
     */
    public @NotNull Rule getRule() {
        return rule;
    }

    /**
     * Set the inner {@link Rule} used for parsing and returns an instance of the same class.
     *
     * @param rule The new inner {@link Rule}.
     * @return A new instance.
     */
    public abstract @NotNull RuleWithChild withRule(final @NotNull Rule rule);

    @Override
    public @NotNull Rule unhideContent() {
        return ((RuleWithChild) withHideTag(false)).withRule(rule.unhideContent());
    }

    @Override
    public boolean equals(Object o) {
        if (!getClass().equals(o.getClass())) return false;
        if (hashCode() != o.hashCode()) return false;
        final @NotNull var that = (RuleWithChild) o;
        if (!Objects.equals(getReduction(), that.getReduction())) return false;
        if (!Objects.equals(isHidden(), that.isHidden())) return false;
        return Objects.equals(getRule(), that.getRule());
    }

    @Override
    public int hashCode() {
        if (bufferedHashCode == Long.MIN_VALUE)
            bufferedHashCode = Objects.hash(getClass(), getReduction(), isHidden(), getRule());
        return (int) bufferedHashCode;
    }
}
