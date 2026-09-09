package de.kleinert.parsewisp.parsing;

import static de.kleinert.parsewisp.trampoline.TrampolineListenerNode.TrampolineListenerKey;

import de.kleinert.parsewisp.reduction.ReductionType;
import de.kleinert.parsewisp.result.failure.ParseFailureReason;
import de.kleinert.parsewisp.trampoline.TrampolineListenerNode;
import org.jetbrains.annotations.NotNull;

/**
 * This class represents the {@code [p]} or {@code p?} operator (where p is an instance of {@link Rule}).
 * When parsing, the rule contained herein is optional (run zero times or once).
 */
public final class OptionalRule extends RuleWithChild {
    private OptionalRule(final boolean hide,
                         final @NotNull ReductionType red,
                         final @NotNull Rule rule) {
        super(hide, red, rule);
    }

    /**
     * Create a new instance. Depending on the implementation, allows for buffering or create a different type of rule.
     *
     * @param rule The rule.
     * @return A rule.
     */
    public static @NotNull Rule create(final @NotNull Rule rule) {
        if (rule instanceof EpsilonTerm)
            return rule;
        return new OptionalRule(defaultHidden, defaultReductionType, rule);
    }

    @Override
    public void parse(final int index, final @NotNull Gll runner) {
        final @NotNull Rule rule = getRule();
        final @NotNull TrampolineListenerNode.TrampolineListenerKey nodeKeyForOpt =
                new TrampolineListenerKey(index, this);
        runner.pushListener(
                new TrampolineListenerKey(index, rule),
                runner.nodeListener(nodeKeyForOpt)
        );
        runner.pushSuccessMessageWithoutValue(nodeKeyForOpt, index);
    }

    @Override
    public void fullParse(final int index, final @NotNull Gll runner) {
        final @NotNull Rule rule = getRule();
        final @NotNull TrampolineListenerNode.TrampolineListenerKey thisNodeKey = new TrampolineListenerKey(index, this);
        runner.pushFullListener(new TrampolineListenerKey(index, rule), runner.nodeListener(thisNodeKey));
        if (index == runner.tramp().getText().length()) {
            runner.pushSuccessMessageWithoutValue(thisNodeKey, index);
        } else {
            runner.fail(thisNodeKey, index, ParseFailureReason.ofOptional(this, true));
        }
    }

    @Override
    public @NotNull OptionalRule withRule(final @NotNull Rule rule) {
        return new OptionalRule(hide, red, rule);
    }

    @Override
    public @NotNull OptionalRule withHideTag(boolean hide) {
        return isHidden() == hide ? this : new OptionalRule(hide, red, rule);
    }

    @Override
    public @NotNull OptionalRule withReduction(@NotNull ReductionType red) {
        return getReduction() == red ? this : new OptionalRule(hide, red, rule);
    }
}
