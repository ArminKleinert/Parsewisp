package de.kleinert.parsewisp.parsing;

import de.kleinert.parsewisp.parser_options.ParserCreationOptions;
import de.kleinert.parsewisp.reduction.ReductionType;

import static de.kleinert.parsewisp.trampoline.TrampolineListenerNode.TrampolineListenerKey;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * A class representing a choice or alternation. That is the {@code (p1 | p2)} operator in EBNF (where p1 and p2 are instances of {@link Rule}).
 * <p>
 * Notation: {@code rule1 | rule2}
 * <p>
 * Example
 * <pre>
 * {@code
 *         // Accepts the language {"a", "b", "ab"}
 *         var p = Alpha.parser("S := 'a' | 'b' | 'ab'");
 *         println(p.parse("a"));  // [:S, a]
 *         println(p.parse("b"));  // [:S, b]
 *         println(p.parse("ab")); // [:S, ab]
 * }
 * </pre>
 * <p>
 * Alternatively, the {@link ParserCreationOptions} class allows an alternative notation for defining alternations:
 * <pre>
 * {@code
 *         var opts = Alpha.ParserCreationOptions
 *                 .getDefault()
 *                 .withRedefinitionOption(Grammar.RedefinitionOption.CHOICE);
 *         var p = Alpha.parser("""
 *                 S : 'a'
 *                 S : 'b'
 *                 S : 'ab'
 *                 """, opts);
 *         println(p.parse("a"));  // [:S, a]
 *         println(p.parse("b"));  // [:S, b]
 *         println(p.parse("ab")); // [:S, ab]
 * }
 * </pre>
 */
public final class AlternationRule extends RuleWithManyChildren {
    private AlternationRule(final boolean hide,
                            final @NotNull ReductionType red,
                            final @NotNull List<Rule> rules) {
        super(hide, red, rules);
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

        var compressedRules = new ArrayList<Rule>();

        for (@NotNull Rule rule : rules) {
            if (rule instanceof AlternationRule cc) {
                compressedRules.addAll(cc.getRules());
            } else {
                compressedRules.add(rule);
            }
        }

        return new AlternationRule(
                defaultHidden, defaultReductionType,
                compressedRules.stream().distinct().toList());
    }

    /**
     * Like {@link #create(List)} except the input
     * list is distinct (each rule in the list occurs exactly once).
     * Use this method only if you are sure that the rules are distinct.
     *
     * @param rules The wrapped rules.
     * @return A rule.
     */
    public static @NotNull AlternationRule createGuaranteeDistinctAndNotEmpty(
            final @NotNull List<Rule> rules) {
        return new AlternationRule(defaultHidden, defaultReductionType, rules);
    }

    @Override
    public void parse(final int index, final @NotNull Gll runner) {
        var listener = runner.nodeListener(new TrampolineListenerKey(index, this));
        for (final @NotNull Rule rule : getRules()) {
            runner.pushListener(
                    new TrampolineListenerKey(index, rule),
                    listener
            );
        }
    }

    @Override
    public void fullParse(final int index, final @NotNull Gll runner) {
        var listener = runner.nodeListener(new TrampolineListenerKey(index, this));
        for (final @NotNull Rule rule : getRules()) {
            runner.pushFullListener(
                    new TrampolineListenerKey(index, rule),
                    listener
            );
        }
    }

    @Override
    public @NotNull AlternationRule withHideTag(boolean hide) {
        return isHidden() == hide ? this : new AlternationRule(hide, getReduction(), getRules());
    }

    @Override
    public @NotNull AlternationRule withReduction(@NotNull ReductionType red) {
        return getReduction() == red ? this : new AlternationRule(isHidden(), red, getRules());
    }

    @Override
    public @NotNull AlternationRule withRules(@NotNull List<@NotNull Rule> rules) {
        return new AlternationRule(isHidden(), getReduction(), rules);
    }
}
