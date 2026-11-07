package de.kleinert.parsewisp.parsing;

import de.kleinert.parsewisp.reduction.ReductionType;
import org.jetbrains.annotations.NotNull;

/**
 * This class represents the ABNF "lookahead" operator {@code &p} (where p is an instance of {@link Rule}).
 *
 * @since 0.9.7
 */
public final class LookaheadRule extends RuleWithChild {
    private LookaheadRule(final boolean hide,
                          final @NotNull ReductionType red,
                          final @NotNull Rule rule) {
        super(hide, red, rule);
    }

    /**
     * Create a new instance. Depending on the implementation, allows for buffering or create a different type of rule.
     *
     * @param rule The inner rule.
     * @return A rule.
     * @since 0.9.7
     */
    public static @NotNull Rule create(final @NotNull Rule rule) {
        if (rule instanceof EpsilonTerm)
            return rule;
        return new LookaheadRule(defaultHidden, defaultReductionType, rule);
    }

    @Override
    public void parse(final int index, final @NotNull Gll runner) {
        final @NotNull var rule = getRule();
        final @NotNull var nodeKey = makeKey(index, this);
        runner.pushListener(makeKey(index, rule),
                ignored -> runner.pushSuccessMessageWithoutValue(nodeKey, index));
    }

    @Override
    public void fullParse(final int index, final @NotNull Gll runner) {
        if (index == runner.tramp().getText().length()) {
            parse(index, runner);
        } else if (!(rule instanceof EpsilonTerm || rule instanceof EOFTerm)) {
            runner.fail(
                    makeKey(index, this),
                    index, this, false);
        }
    }

    @Override
    public @NotNull LookaheadRule withHideTag(final boolean hide) {
        return isHiddenInResult() == hide ? this : new LookaheadRule(hide, red, rule);
    }

    @Override
    public @NotNull LookaheadRule withReduction(final @NotNull ReductionType red) {
        return getReduction() == red ? this : new LookaheadRule(hide, red, rule);
    }

    @Override
    public @NotNull LookaheadRule withRule(final @NotNull Rule rule) {
        return new LookaheadRule(hide, red, rule);
    }
}
