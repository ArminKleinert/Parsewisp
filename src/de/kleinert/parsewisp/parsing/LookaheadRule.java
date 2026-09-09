package de.kleinert.parsewisp.parsing;

import de.kleinert.parsewisp.reduction.ReductionType;
import de.kleinert.parsewisp.result.failure.ParseFailureReason;
import org.jetbrains.annotations.NotNull;

import static de.kleinert.parsewisp.trampoline.TrampolineListenerNode.TrampolineListenerKey;

/**
 * This class represents the ABNF "lookahead" operator {@code &p} (where p is an instance of {@link Rule}).
 */
public final class LookaheadRule extends RuleWithChild {
    private LookaheadRule(final boolean hide,
                          final @NotNull ReductionType red,
                          final @NotNull Rule parser) {
        super(hide, red, parser);
    }

    /**
     * Create a new instance. Depending on the implementation, allows for buffering or create a different type of rule.
     *
     * @param rule The inner parser.
     * @return A rule.
     */
    public static @NotNull Rule create(final @NotNull Rule rule) {
        if (rule instanceof EpsilonTerm)
            return rule;
        return new LookaheadRule(defaultHidden, defaultReductionType, rule);
    }

    @Override
    public void parse(final int index, final @NotNull Gll runner) {
        final @NotNull Rule rule = getRule();
        final @NotNull var nodeKey = new TrampolineListenerKey(index, this);
        runner.pushListener(new TrampolineListenerKey(index, rule),
                ignored -> runner.pushSuccessMessageWithoutValue(nodeKey, index));
    }

    @Override
    public void fullParse(final int index, final @NotNull Gll runner) {
        if (index == runner.tramp().getText().length()) {
            parse(index, runner);
        } else {
            runner.fail(
                    new TrampolineListenerKey(index, this),
                    index,
                    ParseFailureReason.ofLookahead(this, false));
        }
    }

    @Override
    public @NotNull LookaheadRule withHideTag(final boolean hide) {
        return isHidden() == hide ? this : new LookaheadRule(hide, red, rule);
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
