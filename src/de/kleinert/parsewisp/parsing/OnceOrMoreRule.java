package de.kleinert.parsewisp.parsing;

import static de.kleinert.parsewisp.trampoline.TrampolineListenerNode.TrampolineListenerKey;

import de.kleinert.parsewisp.collections.FlatResultSeq;
import de.kleinert.parsewisp.functions.Listener;
import de.kleinert.parsewisp.reduction.ReductionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a "once or more" parse. That is the {@code p+} operator (where p is an instance of {@link Rule}).
 */
public final class OnceOrMoreRule extends RuleWithChild {
    private OnceOrMoreRule(final boolean hide,
                           final @NotNull ReductionType red,
                           final @NotNull Rule rule) {
        super(hide, red, rule);
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
        return new OnceOrMoreRule(defaultHidden, defaultReductionType, rule);
    }

    @Override
    public void parse(final int index, final @NotNull Gll runner) {
        final @NotNull Rule rule = getRule();
        runner.pushListener(
                new TrampolineListenerKey(index, rule),
                plusListener(FlatResultSeq.make(), rule, index, new TrampolineListenerKey(index, this), runner)
        );
    }

    @Override
    public void fullParse(final int index, final @NotNull Gll runner) {
        final @NotNull Rule rule = getRule();
        runner.pushListener(
                new TrampolineListenerKey(index, rule),
                plusFullListener(FlatResultSeq.make(), rule, index, new TrampolineListenerKey(index, this), runner)
        );
    }

    @NotNull
    static Listener plusListener(final @NotNull FlatResultSeq resultsSoFar,
                                 final @NotNull Rule rule,
                                 final int prevIndex,
                                 final @NotNull TrampolineListenerKey nodeKey,
                                 final @NotNull Gll runner) {
        return result -> {
            final @Nullable Object parsedResult = result.getResult();
            final int continueIndex = result.index();
            if (continueIndex == prevIndex) {
                if (resultsSoFar.isEmpty()) {
                    runner.pushSuccessMessageWithoutValue(nodeKey, continueIndex);
                }
                return;
            }
            final var newResultsSoFar = resultsSoFar.appendOrConcat(parsedResult);
            runner.pushListener(
                    new TrampolineListenerKey(continueIndex, rule),
                    plusListener(newResultsSoFar, rule, continueIndex, nodeKey, runner));
            runner.pushSuccessMessage(nodeKey, newResultsSoFar, continueIndex);
        };
    }


    @NotNull
    static Listener plusFullListener(final @NotNull FlatResultSeq resultsSoFar,
                                     final @NotNull Rule rule,
                                     final int prevIndex,
                                     final @NotNull TrampolineListenerKey nodeKey,
                                     final @NotNull Gll runner) {
        return result -> {
            final @Nullable var parsedResult = result.getResult();
            final var continueIndex = result.index();
            if (continueIndex == prevIndex) {
                if (resultsSoFar.isEmpty()) {
                    runner.pushSuccessMessageWithoutValue(nodeKey, continueIndex);
                }
                return;
            }
            final var newResultsSoFar = resultsSoFar.appendOrConcat(parsedResult);
            if (continueIndex == runner.tramp().getText().length()) {
                runner.pushSuccessMessage(nodeKey, newResultsSoFar, continueIndex);
            } else {
                runner.pushListener(
                        new TrampolineListenerKey(continueIndex, rule),
                        plusFullListener(newResultsSoFar, rule, continueIndex, nodeKey, runner));
            }
        };
    }

    @Override
    public @NotNull OnceOrMoreRule withRule(final @NotNull Rule rule) {
        return new OnceOrMoreRule(hide, red, rule);
    }

    @Override
    public @NotNull OnceOrMoreRule withHideTag(boolean hide) {
        return isHidden() == hide ? this : new OnceOrMoreRule(hide, red, rule);
    }

    @Override
    public @NotNull OnceOrMoreRule withReduction(@NotNull ReductionType red) {
        return getReduction() == red ? this : new OnceOrMoreRule(hide, red, rule);
    }
}
