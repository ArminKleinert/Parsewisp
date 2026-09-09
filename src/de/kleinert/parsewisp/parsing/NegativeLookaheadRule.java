package de.kleinert.parsewisp.parsing;

import de.kleinert.parsewisp.reduction.ReductionType;
import de.kleinert.parsewisp.result.failure.ParseFailureReason;
import org.jetbrains.annotations.NotNull;

import static de.kleinert.parsewisp.trampoline.TrampolineListenerNode.TrampolineListenerKey;

import de.kleinert.parsewisp.trampoline.TrampolineListenerNode;

/**
 * Represents a negative lookahead. Written {@code !P}.
 * <p>
 * Example: The production {@code S := !'a' ('a'|'b')+} matches any string of 'a' and 'b' which does NOT start with 'a'.
 */
public final class NegativeLookaheadRule extends RuleWithChild {
    private NegativeLookaheadRule(final boolean hide,
                                  final @NotNull ReductionType red,
                                  final @NotNull Rule rule) {
        super(hide, red, rule);
    }

    /**
     * Create a new instance. Depending on the implementation, allows for buffering or create a different type of rule.
     *
     * @param rule The thing to avoid.
     * @return A rule.
     */
    public static @NotNull Rule create(final @NotNull Rule rule) {
        if (rule instanceof EpsilonTerm)
            return rule;
        return new NegativeLookaheadRule(defaultHidden, defaultReductionType, rule);
    }

    private boolean resultExists_Q(
            final @NotNull Gll runner,
            final @NotNull TrampolineListenerNode.TrampolineListenerKey nodeKey) {
        final TrampolineListenerNode node = runner.tramp().getNode(nodeKey);

        if (node == null)
            return false;

        return !node.fullResults().isEmpty() || !node.results().isEmpty();
    }

    @Override
    public void parse(final int index, final @NotNull Gll runner) {
        final @NotNull Rule rule = getRule();
        final @NotNull TrampolineListenerKey nodeKey = new TrampolineListenerKey(index, rule);
        final @NotNull var thisNodeKey = new TrampolineListenerKey(index, this);

        if (resultExists_Q(runner, nodeKey)) {
            runner.fail(thisNodeKey, index, ParseFailureReason.ofNegated(this, false));
            return;
        }

        runner.pushListener(nodeKey, ignored -> runner.fail(
                thisNodeKey, index,
                ParseFailureReason.ofNegated(this, false)));

        runner.pushNegativeListener(nodeKey, () -> {
            if (!resultExists_Q(runner, nodeKey)) {
                runner.pushSuccessMessageWithoutValue(thisNodeKey, index);
            }
        });
    }

    @Override
    public void fullParse(final int index, final @NotNull Gll runner) {
        parse(index, runner);
    }

    @Override
    public @NotNull NegativeLookaheadRule withRule(final @NotNull Rule rule) {
        return new NegativeLookaheadRule(hide, red, rule);
    }

    @Override
    public @NotNull NegativeLookaheadRule withHideTag(boolean hide) {
        return isHidden() == hide ? this : new NegativeLookaheadRule(hide, red, rule);
    }

    @Override
    public @NotNull NegativeLookaheadRule withReduction(@NotNull ReductionType red) {
        return getReduction() == red ? this : new NegativeLookaheadRule(hide, red, rule);
    }
}
