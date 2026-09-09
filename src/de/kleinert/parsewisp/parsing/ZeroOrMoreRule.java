package de.kleinert.parsewisp.parsing;

import static de.kleinert.parsewisp.trampoline.TrampolineListenerNode.TrampolineListenerKey;

import de.kleinert.parsewisp.collections.FlatResultSeq;
import de.kleinert.parsewisp.reduction.ReductionType;
import org.jetbrains.annotations.NotNull;

/**
 * "Zero or more" repetition. Represents a production which repeatedly ties to match an input. E.g. {@code P*} matches zero or more.
 * <p>
 * Notation: {@code {rule}} or {@code rule*}
 */
public final class ZeroOrMoreRule extends RuleWithChild {
    private ZeroOrMoreRule(final boolean hide,
                           final @NotNull ReductionType red,
                           final @NotNull Rule parser) {
        super(hide, red, parser);
    }

    /**
     * Create a new instance. Depending on the implementation, allows for buffering or create a different type of rule.
     *
     * @param rule The {@link Rule} to match repeatedly.
     * @return A rule.
     */
    public static @NotNull Rule create(final @NotNull Rule rule) {
        if (rule instanceof EpsilonTerm)
            return rule;
        return new ZeroOrMoreRule(defaultHidden, defaultReductionType, rule);
    }

    @Override
    public void parse(final int index, final @NotNull Gll runner) {
        final @NotNull Rule rule = getRule();
        final @NotNull TrampolineListenerKey nodeKeyForStar =
                new TrampolineListenerKey(index, this);
        runner.pushListener(
                new TrampolineListenerKey(index, rule),
                OnceOrMoreRule.plusListener(FlatResultSeq.make(), rule, index, nodeKeyForStar, runner)
        );
        runner.pushSuccessMessageWithoutValue(nodeKeyForStar, index);
    }

    @Override
    public void fullParse(final int index, final @NotNull Gll runner) {
        final @NotNull Rule rule = getRule();
        final @NotNull TrampolineListenerKey nodeKeyForStar =
                new TrampolineListenerKey(index, this);
        if (index == runner.tramp().getText().length()) {
            runner.pushSuccessMessageWithoutValue(nodeKeyForStar, index);
            return;
        }
        runner.pushListener(
                new TrampolineListenerKey(index, rule),
                OnceOrMoreRule.plusFullListener(FlatResultSeq.make(), rule, index, nodeKeyForStar, runner));
    }

    @Override
    public @NotNull ZeroOrMoreRule withRule(final @NotNull Rule rule) {
        return new ZeroOrMoreRule(hide, red, rule);
    }

    @Override
    public @NotNull ZeroOrMoreRule withHideTag(boolean hide) {
        return isHidden() == hide ? this : new ZeroOrMoreRule(hide, red, rule);
    }

    @Override
    public @NotNull ZeroOrMoreRule withReduction(@NotNull ReductionType red) {
        return getReduction() == red ? this : new ZeroOrMoreRule(hide, red, rule);
    }
}
