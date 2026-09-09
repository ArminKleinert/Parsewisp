package de.kleinert.parsewisp.parsing;

import de.kleinert.parsewisp.collections.FlatResultSeq;
import de.kleinert.parsewisp.functions.Listener;
import de.kleinert.parsewisp.reduction.ReductionType;
import de.kleinert.parsewisp.trampoline.TrampolineListenerNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static de.kleinert.parsewisp.trampoline.TrampolineListenerNode.TrampolineListenerKey;

/**
 * This class represents a concatenation of productions, written as {@code p1 p2 p3 ...} (where p1, p2, etc. are instances of {@link Rule}).
 * When parsing, it tries to match p1, then p2, then p3 and so on.
 */
public final class ConcatRule extends RuleWithManyChildren {
    private ConcatRule(final boolean hide,
                       final @NotNull ReductionType red,
                       final @NotNull List<Rule> parsers) {
        super(hide, red, parsers);
    }

    /**
     * Create a new instance. Depending on the implementation, allows for buffering or create a different type of rule.
     *
     * @param rules The wrapped rules.
     * @return A rule.
     */
    public static @NotNull Rule create(final @NotNull List<Rule> rules) {
        if (rules.isEmpty())
            return EpsilonTerm.getDefault();
        if (rules.size() == 1)
            return rules.get(0);

        var compressedResult = new ArrayList<Rule>();

        for (@NotNull Rule rule : rules) {
            if (rule instanceof ConcatRule cc) {
                compressedResult.addAll(cc.getRules());
            } else {
                compressedResult.add(rule);
            }
        }

        return new ConcatRule(defaultHidden, defaultReductionType, compressedResult);
    }

    /**
     * Like {@link #create(List)}, except it assumes that (1) the input includes no epsilons and (2) there are at least two rules in the input. This makes the method perform fewer optimizations, saving a miniscule amount of time.
     *
     * @param rules The rules.
     * @return A concatenation rule.
     */
    public static @NotNull ConcatRule createNoEpsilonMoreThan1(
            final @NotNull List<Rule> rules) {
        return new ConcatRule(defaultHidden, defaultReductionType, rules);
    }

    @Override
    public void parse(final int index, final @NotNull Gll runner) {
        final @NotNull List<@NotNull Rule> parsers = getRules();
        runner.pushListener(
                new TrampolineListenerKey(index, parsers.get(0)),
                catListener(FlatResultSeq.make(), parsers.subList(1, parsers.size()), new TrampolineListenerKey(index, this), runner));
    }

    @Override
    public void fullParse(final int index, final @NotNull Gll runner) {
        final @NotNull List<@NotNull Rule> parsers = getRules();
        runner.pushListener(
                new TrampolineListenerKey(index, parsers.get(0)),
                catFullListener(FlatResultSeq.make(), parsers.subList(1, parsers.size()), new TrampolineListenerKey(index, this), runner));
    }

    private @NotNull Listener catListener(final @NotNull FlatResultSeq resultsSoFar,
                                          final @NotNull List<Rule> parserSequence,
                                          final @NotNull TrampolineListenerNode.TrampolineListenerKey nodeKey,
                                          final @NotNull Gll runner) {
        return result -> {
            final @Nullable Object parsedResult = result.getResult();
            final int continueIndex = result.index();
            final @NotNull FlatResultSeq newResultsSoFar = resultsSoFar.appendOrConcat(parsedResult);

            if (parserSequence.isEmpty()) {
                runner.pushSuccessMessage(nodeKey, newResultsSoFar, continueIndex);
            } else {
                runner.pushListener(
                        new TrampolineListenerKey(continueIndex, parserSequence.get(0)),
                        catListener(
                                newResultsSoFar,
                                parserSequence.subList(1, parserSequence.size()),
                                nodeKey,
                                runner)
                );
            }
        };
    }

    private @NotNull Listener catFullListener(final @NotNull FlatResultSeq resultsSoFar,
                                              final @NotNull List<Rule> parserSequence,
                                              final @NotNull TrampolineListenerNode.TrampolineListenerKey nodeKey,
                                              final @NotNull Gll runner) {
        return result -> {
            final @Nullable var parsedResult = result.getResult();
            final var continueIndex = result.index();
            final @NotNull var newResultsSoFar = resultsSoFar.appendOrConcat(parsedResult);

            if (!parserSequence.isEmpty()) {
                var listenerKey = new TrampolineListenerKey(continueIndex, parserSequence.get(0));
                var listener = catFullListener(
                        newResultsSoFar,
                        parserSequence.subList(1, parserSequence.size()),
                        nodeKey, runner);

                if (parserSequence.size() == 1) runner.pushFullListener(listenerKey, listener);
                else runner.pushListener(listenerKey, listener);
            } else {
                runner.pushSuccessMessage(nodeKey, newResultsSoFar, continueIndex);
            }
        };
    }

    @Override
    public @NotNull Rule withHideTag(boolean hide) {
        return isHidden() == hide ? this : new ConcatRule(hide, getReduction(), getRules());
    }

    @Override
    public @NotNull Rule withReduction(@NotNull ReductionType red) {
        return getReduction() == red ? this : new ConcatRule(isHidden(), red, getRules());
    }

    @Override
    public @NotNull ConcatRule withRules(@NotNull List<@NotNull Rule> rules) {
        return new ConcatRule(isHidden(), getReduction(), rules);
    }
}
