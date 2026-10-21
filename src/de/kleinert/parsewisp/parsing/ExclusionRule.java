package de.kleinert.parsewisp.parsing;

import de.kleinert.parsewisp.Sym;
import de.kleinert.parsewisp.grammar.Grammar;
import de.kleinert.parsewisp.reduction.ReductionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import static de.kleinert.parsewisp.trampoline.TrampolineListenerNode.TrampolineListenerKey;

/**
 * Describes the "Syntactic exception" or "except" operator. That is the {@code (p1 - p2)} operator in EBNF (where p1 and p2 are instances of {@link Rule}).
 * <p>
 * Notation: {@code rule1 - rule2}  (match rule1 except if it also matches rule2)
 * <p>
 * Example
 * <pre>
 * {@code
 *         var p = Parsewisp.parser("S := #'[0-9]+' - '11'"); // Any positive number except 11.
 *         println(p.parse("12"));  // [:S, 12]
 *         println(p.parse("11"));  // Failure
 * }
 * </pre>
 * See also: <a href="https://www.iso.org/standard/26153.html">ISO/IEC 14977:1996</a> and <a href="https://stackoverflow.com/a/35138946">an explanation on StackOverflow</a>.
 *
 * @since 0.9.7
 */
public final class ExclusionRule extends RuleWithManyChildren {
    private final @NotNull Rule expected;
    private final @NotNull Rule excluded;

    // 0: Default (Complex grammar)
    // 1: Simple string terminal
    // 2: Regex terminal
    // 3: Alternation of string or regex terminals
    // 4: Concatenation of string or regex terminals
    private final int mode;

    private ExclusionRule(final boolean hide, final @NotNull ReductionType red,
                          final @NotNull Rule expected,
                          final @NotNull Rule excluded) {
        super(hide, red, List.of(expected, excluded));
        this.expected = expected;
        this.excluded = excluded;

        Predicate<Rule> stringOrRegexPredicate = it -> it instanceof StringTerm || it instanceof RegexTerm;

        if (excluded instanceof StringTerm) mode = 1;
        else if (excluded instanceof RegexTerm) mode = 2;
        else if (excluded instanceof AlternationRule && ((AlternationRule) excluded).rules.stream().allMatch(stringOrRegexPredicate))
            mode = 3;
        else if (excluded instanceof ConcatRule && ((ConcatRule) excluded).rules.stream().allMatch(stringOrRegexPredicate))
            mode = 4;
        else if (excluded instanceof EpsilonTerm) mode = 5;
        else mode = 0;
    }

    /**
     * Create a new instance. Represents {@code (ruleExpected - ruleExcluded)}.
     * Depending on the implementation, allows for buffering or create a different type of rule.
     *
     * @param ruleExpected The rule that must be matched.
     * @param ruleExcluded The rule that must not be matched.
     * @return A rule.
     * @since 0.9.7
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
        final @NotNull TrampolineListenerKey nodeKeyForExpect = new TrampolineListenerKey(index, expected);

        runner.pushListener(
                nodeKeyForExpect,
                expectedSuccess -> {
                    var substring = runner.tramp().getText().substring(index, expectedSuccess.index());

                    if (reparsePart(substring, runner)) {
                        runner.fail(nodeKeyForThis, index, this, false);
                        return;
                    }

                    runner.pushSuccessAgainWithNewKey(nodeKeyForThis, expectedSuccess);
                });
    }

    @Override
    public void fullParse(final int index, final @NotNull Gll runner) {
        final @NotNull TrampolineListenerKey nodeKeyForThis = new TrampolineListenerKey(index, this);
        final @NotNull TrampolineListenerKey nodeKeyForExpect = new TrampolineListenerKey(index, expected);

        runner.pushListener(
                nodeKeyForExpect,
                expectedSuccess -> {
                    var substring = runner.tramp().getText().substring(index, expectedSuccess.index());

                    if (reparsePart(substring, runner)) {
                        runner.fail(nodeKeyForThis, index, this, true);
                        return;
                    }

                    runner.pushSuccessAgainWithNewKey(nodeKeyForThis, expectedSuccess);
                });
    }

    private @Nullable WeakReference<Gll> referenceForBufferedElements;
    private @Nullable Grammar modifiedExclusionGrammar = null;
    private @Nullable Sym modifiedExclusionStart = null;

    private boolean reparsePart(final @NotNull String subs, final @NotNull Gll runner) {
        return switch (mode) {
            case 1, 2, 5 ->  // String mode, Regex mode, Epsilon mode.
                    reparseStrOrRegex(subs, excluded);
            case 3 -> {
                // If any of the rules in the exclusion match, the parse failed.
                for (var r : ((AlternationRule) excluded).rules) {
                    if (reparseStrOrRegex(subs, r)) yield true;
                }
                yield false;
            }
            case 4 -> {
                // If all rules in the exclusion succeeded, the parse failed.
                for (var r : ((ConcatRule) excluded).rules) {
                    if (!reparseStrOrRegex(subs, r)) yield false;
                }
                yield true;
            }
            default -> reparseFromMoreComplexGrammar(subs, runner);
        };
    }

    private boolean reparseStrOrRegex(String subs, Rule r1) {
        if (r1 instanceof StringTerm) {
            var r = (StringTerm) r1;
            return r.isCaseInsensitive()
                    ? r.getString().equalsIgnoreCase(subs)
                    : r.getString().equals(subs);
        }
        if (r1 instanceof RegexTerm) {
            return ((RegexTerm) r1).getRegexp().matcher(subs).matches();
        }
        if (r1 instanceof EpsilonTerm) {
            return subs.isEmpty();
        }
        throw new IllegalStateException();
    }

    private boolean reparseFromMoreComplexGrammar(final @NotNull String subs, final @NotNull Gll runner) {
        final @NotNull var oldGrammar = runner.tramp().getGrammar();
        final @NotNull Sym startSymbol;
        final @NotNull Grammar grammar;

        if (referenceForBufferedElements != null && referenceForBufferedElements.get() == runner) {
            assert (modifiedExclusionGrammar != null);
            assert (modifiedExclusionStart != null);
            startSymbol = modifiedExclusionStart;
            grammar = modifiedExclusionGrammar;
        } else if (excluded instanceof NonTerminal) {
            // The rule was already a non-terminal. Simply change starting production.
            startSymbol = ((NonTerminal) excluded).getKeyword();
            grammar = oldGrammar;
        } else {
            // Find a new start production name which is not in the grammar yet.
            Sym tempSym;
            if (!oldGrammar.containsKey(Sym.sym("RerunForExclusion0"))) {
                tempSym = Sym.sym("RerunForExclusion0");
            } else {
                int n = 0;
                StringBuilder nameBuilder = new StringBuilder("RerunForExclusion");
                do {
                    nameBuilder.setLength(17);
                    n++;
                    tempSym = Sym.sym(nameBuilder.append(n).toString());
                } while (oldGrammar.containsKey(tempSym));
            }
            startSymbol = tempSym;

            // Make new Grammar
            var tempG = new LinkedHashMap<>(oldGrammar);
            tempG.put(
                    startSymbol,
                    ConcatRule.create(List.of(excluded, EOFTerm.getDefault())));
            ReductionType.applyStandardReductionToProductions(tempG);

            grammar = new Grammar(startSymbol, tempG);

            referenceForBufferedElements = new WeakReference<>(runner);
            modifiedExclusionStart = startSymbol;
            modifiedExclusionGrammar = grammar;
        }
        return Gll.parse(grammar, startSymbol, subs, false, false, runner.getGrammarPrinter()).isSuccess();
    }

    /**
     * The rule that is expected. E.g. in the exclusion rule {@code (A - B)}, it would be {@code A}.
     *
     * @return The expected rule.
     */
    public @NotNull Rule getExpected() {
        return expected;
    }

    /**
     * The rule that is to be excluded. E.g. in the exclusion rule {@code (A - B)}, it would be {@code B}.
     *
     * @return The excluded rule.
     */
    public @NotNull Rule getExcluded() {
        return excluded;
    }

    @Override
    public @NotNull ExclusionRule withHideTag(final boolean hide) {
        return isHiddenInResult() == hide ? this : new ExclusionRule(hide, this.getReduction(), expected, excluded);
    }

    @Override
    public @NotNull ExclusionRule withReduction(final @NotNull ReductionType red) {
        return getReduction() == red ? this : new ExclusionRule(isHiddenInResult(), red, expected, excluded);
    }

    @Override
    public @NotNull ExclusionRule withRules(final @NotNull List<@NotNull Rule> rules) {
        if (rules.size() != 2)
            throw new IllegalArgumentException("Must pass exactly 2 arguments.");
        return new ExclusionRule(isHiddenInResult(), getReduction(), rules.get(0), rules.get(rules.size() - 1));
    }
}
