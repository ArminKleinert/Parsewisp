package de.kleinert.parsewisp.parsing;

import de.kleinert.parsewisp.Sym;
import de.kleinert.parsewisp.grammar.Grammar;
import de.kleinert.parsewisp.reduction.ReductionType;
import de.kleinert.parsewisp.result.ParseResult;
import de.kleinert.parsewisp.result.failure.ParseFailureReason;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

import static de.kleinert.parsewisp.trampoline.TrampolineListenerNode.TrampolineListenerKey;

/**
 * Describes the "Syntactic exception" or "except" operator. That is the {@code (p1 - p2)} operator in EBNF (where p1 and p2 are instances of {@link Rule}).
 * <p>
 * Notation: {@code rule1 - rule2}  (match rule1 except if it also matches rule2)
 * <p>
 * Example
 * <pre>
 * {@code
 *         // Accepts the language {"a", "b", "ab"}
 *         var p = Parsewisp.parser("S := #'[0-9]+' - '11'"); // Any positive number except 11.
 *         println(p.parse("12"));  // [:S, 12]
 *         println(p.parse("11"));  // Failure
 * }
 * </pre>
 * See also: <a href="https://www.iso.org/standard/26153.html">ISO/IEC 14977:1996</a> and <a href="https://stackoverflow.com/a/35138946">an explanation on StackOverflow</a>.
 */
public final class ExclusionRule extends RuleWithManyChildren {
    private final @NotNull Rule parserExpected;
    private final @NotNull Rule parserExcluded;

    private ExclusionRule(final boolean hide, final @NotNull ReductionType red,
                          final @NotNull Rule parserExpected,
                          final @NotNull Rule parserExcluded) {
        super(hide, red, List.of(parserExpected, parserExcluded));
        this.parserExpected = parserExpected;
        this.parserExcluded = parserExcluded;
    }

    /**
     * Create a new instance. Represents {@code (ruleExpected - ruleExcluded)}.
     * Depending on the implementation, allows for buffering or create a different type of rule.
     *
     * @param ruleExpected The rule that must be matched.
     * @param ruleExcluded The rule that must not be matched.
     * @return A rule.
     */
    public static @NotNull Rule create(final @NotNull Rule ruleExpected,
                                       final @NotNull Rule ruleExcluded) {
        if (Objects.equals(ruleExpected, ruleExcluded))
            return EpsilonTerm.getDefault();
        return new ExclusionRule(defaultHidden, defaultReductionType, ruleExpected, ruleExcluded);
    }

    @Override
    public void parse(final int index, final @NotNull Gll runner) {
        final @NotNull TrampolineListenerKey nodeKeyForThis = new TrampolineListenerKey(index, this);
        final @NotNull TrampolineListenerKey nodeKeyForExpect = new TrampolineListenerKey(index, parserExpected);

        runner.pushListener(
                nodeKeyForExpect,
                expectedSuccess -> {
                    var substring = runner.tramp().getText().substring(index, expectedSuccess.index());

                    if (reparsePart(substring, runner).isSuccess()) {
                        runner.fail(nodeKeyForThis, index, ParseFailureReason.ofExclusion(this, false));
                        return;
                    }

                    runner.pushSuccessAgainWithNewKey(nodeKeyForThis, expectedSuccess);
                });
    }

    @Override
    public void fullParse(final int index, final @NotNull Gll runner) {
        final @NotNull TrampolineListenerKey nodeKeyForThis = new TrampolineListenerKey(index, this);
        final @NotNull TrampolineListenerKey nodeKeyForExpect = new TrampolineListenerKey(index, parserExpected);

        runner.pushListener(
                nodeKeyForExpect,
                expectedSuccess -> {
                    var substring = runner.tramp().getText().substring(index, expectedSuccess.index());

                    if (reparsePart(substring, runner).isSuccess()) {
                        runner.fail(nodeKeyForThis, index, ParseFailureReason.ofExclusion(this, true));
                        return;
                    }

                    runner.pushSuccessAgainWithNewKey(nodeKeyForThis, expectedSuccess);
                });
    }

    private @NotNull ParseResult reparsePart(final @NotNull String subs, final @NotNull Gll runner) {
        final @NotNull var oldGrammar = runner.tramp().getGrammar();
        final @NotNull Sym startSymbol;
        final @NotNull Grammar grammar;

        if (parserExcluded instanceof NonTerminal) {
            // The rule was already a non-terminal. Simply change starting production.
            startSymbol = ((NonTerminal) parserExcluded).getKeyword();
            grammar = oldGrammar;
        } else {
            // Find a new start production name which is not in the grammar yet.
            Sym tempSym;
            int n = 0;
            do {
                tempSym = Sym.sym("RerunForExclusion" + n++);
            } while (oldGrammar.containsKey(tempSym));
            startSymbol = tempSym;

            // Make new Grammar
            var tempG = new LinkedHashMap<>(oldGrammar);
            tempG.put(
                    startSymbol,
                    ConcatRule.create(List.of(parserExcluded, EOFTerm.getDefault())));
            ReductionType.applyStandardReductionToProductions(tempG);

            grammar = new Grammar(startSymbol, tempG);
        }
        return Gll.parse(grammar, startSymbol, subs, false, false);
    }

    /**
     * The rule that is expected. E.g. in the exclusion rule {@code (A - B)}, it would be {@code A}.
     *
     * @return The expected rule.
     */
    public @NotNull Rule getParserExpected() {
        return parserExpected;
    }

    /**
     * The rule that is to be excluded. E.g. in the exclusion rule {@code (A - B)}, it would be {@code B}.
     *
     * @return The excluded rule.
     */
    public @NotNull Rule getParserExcluded() {
        return parserExcluded;
    }

    @Override
    public @NotNull ExclusionRule withHideTag(final boolean hide) {
        return isHidden() == hide ? this : new ExclusionRule(hide, this.getReduction(), parserExpected, parserExcluded);
    }

    @Override
    public @NotNull ExclusionRule withReduction(final @NotNull ReductionType red) {
        return getReduction() == red ? this : new ExclusionRule(isHidden(), red, parserExpected, parserExcluded);
    }

    @Override
    public @NotNull ExclusionRule withRules(final @NotNull List<@NotNull Rule> rules) {
        if (rules.size() != 2)
            throw new IllegalArgumentException("Must pass exactly 2 arguments.");
        return new ExclusionRule(isHidden(), getReduction(), rules.get(0), rules.get(rules.size()-1));
    }
}
