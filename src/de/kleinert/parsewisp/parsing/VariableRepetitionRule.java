package de.kleinert.parsewisp.parsing;

import static de.kleinert.parsewisp.trampoline.TrampolineListenerNode.TrampolineListenerKey;

import de.kleinert.parsewisp.collections.FlatResultSeq;
import de.kleinert.parsewisp.functions.Listener;
import de.kleinert.parsewisp.reduction.ReductionType;
import de.kleinert.parsewisp.result.failure.ParseFailureReason;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * A class representing the ABNF counted repetition operator.
 * There are multiple variants, depending on the syntax (where n1 and n2 are integers, and p is an instance of {@link Rule}):
 * <ul>
 *     <li>Repeat a minimum of {@code n1} times and a maximum of {@code n2} times: {@code n1*n2 p}</li>
 *     <li>Repeat a minimum of {@code n1} times: {@code n1* p}</li>
 *     <li>Repeat a maximum of {@code n1} times: {@code *n1 p}</li>
 *     <li>Repeat exactly of {@code n1} times: {@code n1 p}</li>
 * </ul>
 */
public final class VariableRepetitionRule extends RuleWithChild {
    private final int min;
    private final int max;

    private VariableRepetitionRule(final boolean hide,
                                   final @NotNull ReductionType red,
                                   final @NotNull Rule rule,
                                   final int min, final int max) {
        super(hide, red, rule);
        this.min = min;
        this.max = max;
    }

    /**
     * Create a new instance. Depending on the implementation, allows for buffering or create a different type of rule.
     *
     * @param rule The inner element.
     * @param min  Minimum repetitions.
     * @param max  Maximum repetitions.
     * @return A rule.
     */
    public static @NotNull Rule create(final @NotNull Rule rule, final int min, final int max) {
        if (min < 0 || min > max)
            throw new IllegalArgumentException(
                    "Illegal repetition (min=" + min + ", max=" + max + ")");
        if (min == 0 && max == 0)
            return EpsilonTerm.getDefault();
        if (rule instanceof EpsilonTerm || (min == 1 && max == 1))
            return rule;
        if ((rule instanceof VariableRepetitionRule rc) && rc.getMin() <= 1 && min <= 1) {
            final int newMin = (int) Long.min(
                    Integer.MAX_VALUE,
                    ((long) rc.getMin()) * min);
            final int newMax = (int) Long.min(
                    Integer.MAX_VALUE,
                    ((long) rc.getMax()) * max);
            return VariableRepetitionRule.create(rc.getRule(), newMin, newMax);
        }
        return new VariableRepetitionRule(defaultHidden, defaultReductionType, rule, min, max);
    }

    @Override
    public void parse(final int index, final @NotNull Gll runner) {
        final @NotNull Rule rule = getRule();
        final @NotNull TrampolineListenerKey nodeKeyForThis = new TrampolineListenerKey(index, this);
        final @NotNull TrampolineListenerKey nodeKeyForInnerRule = new TrampolineListenerKey(index, rule);
        if (getMin() == 0) {
            runner.pushSuccessMessageWithoutValue(nodeKeyForInnerRule, index);
        }
        if (getMax() == 0) {
            return;
        }
        runner.pushListener(
                nodeKeyForInnerRule,
                repListener(FlatResultSeq.make(), 0, this, nodeKeyForThis, runner));
    }

    @Override
    public void fullParse(final int index, final @NotNull Gll runner) {
        final @NotNull Rule rule = getRule();
        final @NotNull TrampolineListenerKey nodeKeyForThis = new TrampolineListenerKey(index, this);
        final @NotNull TrampolineListenerKey nodeKeyForInnerRule = new TrampolineListenerKey(index, rule);
        final @NotNull var emptyResults = FlatResultSeq.make();
        if (getMin() == 0) {
            runner.pushSuccessMessageWithoutValue(new TrampolineListenerKey(index, this), index);
        }
        if (getMax() == 0) {
            return;
        }
        runner.pushListener(
                nodeKeyForInnerRule,
                repFullListener(emptyResults, 0, rule, getMin(), getMax(), nodeKeyForThis, runner));
    }

    private @NotNull Listener repListener(final @NotNull FlatResultSeq resultsSoFar,
                                          final int nResultsSoFar,
                                          final @NotNull VariableRepetitionRule repRule,
                                          final @NotNull TrampolineListenerKey nodeKey,
                                          final @NotNull Gll runner) {
        return result -> {
            final @Nullable Object parsedResult = result.getResult();
            final int continueIndex = result.index();

            final @NotNull FlatResultSeq newResultsSoFar = resultsSoFar.appendOrConcat(parsedResult);

            final int newNResultsSoFar = nResultsSoFar + 1;

            if (repRule.getMin() <= newNResultsSoFar && newNResultsSoFar <= repRule.getMax()) {
                runner.pushSuccessMessage(nodeKey, newResultsSoFar, continueIndex);
            }

            if (newNResultsSoFar < repRule.getMax()) {
                runner.pushListener(
                        new TrampolineListenerKey(continueIndex, repRule.getRule()),
                        repListener(newResultsSoFar, newNResultsSoFar, repRule, nodeKey, runner)
                );
            }
        };
    }

    private @NotNull Listener repFullListener(final @NotNull FlatResultSeq resultsSoFar,
                                              final int nResultsSoFar,
                                              final @NotNull Rule rule,
                                              final int minimum,
                                              final int maximum,
                                              final @NotNull TrampolineListenerKey nodeKey,
                                              final @NotNull Gll runner) {
        return result -> {
            final @Nullable var parsedResult = result.getResult();
            final int continueIndex = result.index();
            final @NotNull var newResultsSoFar = resultsSoFar.appendOrConcat(parsedResult);
            final int newNResultsSoFar = nResultsSoFar + 1;
            if (continueIndex == runner.tramp().getText().length()) {
                if (minimum <= newNResultsSoFar && newNResultsSoFar <= maximum) {
                    runner.pushSuccessMessage(nodeKey, newResultsSoFar, continueIndex);
                } else {
                    runner.fail(nodeKey, continueIndex,
                            ParseFailureReason.ofRepetition(this, false));
                }
            } else {
                if (newNResultsSoFar < maximum) {
                    final @NotNull var listener = repFullListener(
                            newResultsSoFar, newNResultsSoFar,
                            rule, minimum, maximum, nodeKey, runner);
                    runner.pushListener(new TrampolineListenerKey(continueIndex, rule), listener);
                } else {
                    runner.fail(nodeKey, continueIndex,
                            ParseFailureReason.ofRepetition(this, false));
                }
            }
        };
    }

    /**
     * Return the minimum number of repetitions.
     *
     * @return minimum
     */
    public int getMin() {
        return min;
    }

    /**
     * Return the maximum number of repetitions.
     *
     * @return maximum
     */
    public int getMax() {
        return max;
    }

    @Override
    public @NotNull VariableRepetitionRule withRule(final @NotNull Rule rule) {
        return new VariableRepetitionRule(hide, red, rule, min, max);
    }

    @Override
    public @NotNull VariableRepetitionRule withHideTag(boolean hide) {
        return isHidden() == hide ? this : new VariableRepetitionRule(hide, red, rule, min, max);
    }

    @Override
    public @NotNull VariableRepetitionRule withReduction(@NotNull ReductionType red) {
        return getReduction() == red ? this : new VariableRepetitionRule(hide, red, rule, min, max);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof VariableRepetitionRule that)) return false;
        if (this == that) return true;
        return hide == that.hide
                && Objects.equals(red, that.red)
                && Objects.equals(rule, that.rule)
                && Objects.equals(min, that.min)
                && Objects.equals(max, that.max);
    }

    @Override
    public int hashCode() {
        return min * 31 + max * 31 + Objects.hash(hide, red, rule);
    }
}
