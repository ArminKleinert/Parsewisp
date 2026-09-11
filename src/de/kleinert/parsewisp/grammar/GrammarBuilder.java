package de.kleinert.parsewisp.grammar;

import de.kleinert.parsewisp.Sym;
import de.kleinert.parsewisp.error.IllegalGrammarException;
import de.kleinert.parsewisp.parser.Parser;
import de.kleinert.parsewisp.parser_options.ParserCreationOptions;
import de.kleinert.parsewisp.parsing.*;
import de.kleinert.parsewisp.reduction.ReductionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * This class provides an easy way to construct grammars. To use it, it needs to be inherited and the {@link #make()} method overridden.
 * Using a GrammarBuilder is much faster than constructing grammars from strings.
 * <p>
 * In the following example, two equivalent grammars and parsers are constructed from a string and from a builder. The assertions show that the grammars are equal and that the parsers give equivalent outputs.
 * <pre>
 * {@code
 *         var gFromString = Parsewisp.parser("""
 *                         S = NUMBER NUMBER*
 *                         NUMBER = '0' | '1' | '2' | '3' | '4' | '5' | '6' | '7' | '8' | '9'
 *                         """)
 *                 .grammar();
 *         var gFromGB = new GrammarBuilder(ParserCreationOptions.getDefault()) {
 *             @Override
 *             public void make() {
 *                 addProduction("S", concat(Sym.sym("NUMBER"), repeatMin(nt(Sym.sym("NUMBER")), 0)));
 *                 addProduction("NUMBER", alternation("0", "1", "2", "3", "4", "5", "6", "7", "8", "9"));
 *             }
 *         }.build();
 *
 *         Assertions.assertEquals(gFromString, gFromGB);
 *
 *         var pFromString = Parsewisp.parser(gFromString, ParserCreationOptions.getDefault().withStartProduction(Sym.sym("S")));
 *         var pFromGB = Parsewisp.parser(gFromGB, ParserCreationOptions.getDefault().withStartProduction(Sym.sym("S")));
 *         var text = "0123456789";
 *         Assertions.assertEquals(pFromString.parse(text), pFromGB.parse(text));
 * }
 * </pre>
 * <p>
 * The {@link #addProduction(Sym, Rule)} method can be used to add new productions.
 */
public abstract class GrammarBuilder {
    protected @NotNull LinkedHashMap<Sym, Rule> productions;
    protected final @NotNull ParserCreationOptions options;
    private final BufferForRules buffer;

    protected GrammarBuilder(final @NotNull ParserCreationOptions options) {
        productions = new LinkedHashMap<>();
        this.options = options;
        buffer = new BufferForRules();
    }

    /**
     * Override this to create a grammar. Used in {@link #build()}.
     */
    protected abstract void make();

    /**
     * Use this to construct the grammar.
     *
     * @return The grammar.
     */
    public final @NotNull Grammar build() {
        return buildWithWhitespace(null, null);
    }

    /**
     * Use this to construct the grammar. Productions can be added before starting the builder.
     *
     * @param initialProductions Productions to add in the beginning.
     * @param wsParser           Whitespace parser to include.
     * @return The grammar.
     */
    public final @NotNull Grammar buildWithWhitespace(
            final @Nullable LinkedHashMap<Sym, Rule> initialProductions,
            final @Nullable Parser wsParser) {
        final @NotNull Sym start;

        synchronized (this) {
            if (initialProductions != null) {
                for (final @NotNull var entry : initialProductions.entrySet()) {
                    addProduction(entry.getKey(), entry.getValue());
                }
            }

            make();

            start = options.startProduction() != null
                    ? options.startProduction()
                    : productions.keySet().iterator().next();

            compress();

            ReductionType.applyStandardReductionToProductions(productions);
            if (wsParser != null) {
                autoWhitespace(start, wsParser.grammar(), wsParser.startProduction());
            }
        }

        var g = new Grammar(start, productions);

        if (options.checkCorrectness()) {
            final @NotNull var analysisResult = g.analyze();
            if (!analysisResult.isValid())
                throw new IllegalGrammarException(
                        stringBuildGrammarCreationFailureMessage(analysisResult));
        }

        return g;
    }

    private String stringBuildGrammarCreationFailureMessage(Grammar.GrammarInfo analysisResult) {
        throw new IllegalGrammarException(
                "The grammar is invalid. Undefined productions: "
                        + analysisResult.getUndefinedUsedNTs()
                        + "; number of terminals: "
                        + analysisResult.definedTerminals().size()
                        + " (a valid grammar has at least one terminal)");
    }

    /**
     * Add multiple entries.
     *
     * @param entries The entries.
     * @see #addProduction(Sym, Rule)
     */
    protected final void addAllProductions(
            final @NotNull Collection<Map.Entry<Sym, Rule>> entries) {
        for (final @NotNull var entry : entries) {
            addProduction(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Adds a production to the output. The specific behavior depends on the {@link ParserCreationOptions#redefinitionOption()} used.
     *
     * @param lhs The production's key. (left-hand-side)
     * @param rhs The production's right-hand-side.
     */
    protected final void addProduction(
            final @NotNull Sym lhs, final @NotNull Rule rhs) {
        var existing = productions.putIfAbsent(lhs, rhs);
        if (existing == null)
            return;

        switch (options.redefinitionOption()) {
            case OVERRIDE -> productions.put(lhs, rhs);
            case ERROR -> throw new IllegalArgumentException(
                    "Production already in grammar: " + lhs);
            case CHOICE -> productions.put(lhs, alternation(existing, rhs));
            case KEEP -> {
            }
        }
    }

    /**
     * Adds a production to the output. This method is a convenient alternative to {@link #addProduction(Sym, Rule)} if you do not want to make the string into a symbol yourself.
     *
     * @param lhs The production's key. (left-hand-side)
     * @param rhs The production's right-hand-side.
     * @see #addProduction(Sym, Rule)
     */
    protected final void addProduction(
            final @NotNull String lhs, final @NotNull Rule rhs) {
        addProduction(Sym.sym(lhs), rhs);
    }

    /**
     * Creates a rule depending on the input's specific type.
     * <ul>
     * <li>For {@code null}, use {@link #epsilon()}.</li>
     * <li>For {@code Rule}, return the input.</li>
     * <li>For {@code String}, use {@link #string(String)}.</li>
     * <li>For {@code Pattern}, use {@link #regex(Pattern)}.</li>
     * <li>For {@code Sym}, use {@link #nt(Sym)}.</li>
     * <li>For {@code List}, use {@link #concat(List)}.</li>
     * <li>For {@code Set}, use {@link #alternation(Object, Object...)}.</li>
     * </ul>
     *
     * @param c Input object.
     * @return A rule depending on the input's type.
     */
    protected final @NotNull Rule of(final @Nullable Object c) {
        if (c == null) {
            return EpsilonTerm.getDefault();
        }
        if (c instanceof Rule) {
            return (Rule) c;
        }
        if (c instanceof String) {
            return string((String) c);
        }
        if (c instanceof Pattern) {
            return regex((Pattern) c);
        }
        if (c instanceof Sym) {
            return nt((Sym) c);
        }
        if (c instanceof List<?>) {
            return concat(((List<?>) c).stream().map(this::of).toList());
        }
        if (c instanceof Set<?>) {
            return alternationC(((Set<?>) c).stream().distinct().map(this::of).toList());
        }
        throw new IllegalArgumentException(c.getClass().getName());
    }

    /**
     * Create a {@link RegexTerm}. The rule might be buffered.
     * If the regex can only ever match a single string, a {@link StringTerm} might be returned instead.
     *
     * @param p The input regex.
     * @return A {@link RegexTerm} or something that returns equivalent outputs when parsing.
     */
    protected final @NotNull Rule regex(final @NotNull Pattern p) {
        return buffer.getOrAddRegex(p);
    }

    /**
     * See {@link #regex(Pattern)}.
     *
     * @param s The input regex.
     * @return A {@link RegexTerm} or something that returns equivalent outputs when parsing.
     */
    protected final @NotNull Rule regex(final @NotNull String s) {
        return buffer.getOrAddRegex(Pattern.compile(s));
    }

    /**
     * Creates a {@link LookaheadRule}.
     * The output can be a {@link EpsilonTerm} if the input is epsilon.
     *
     * @param input The rule to look for.
     * @return The new rule.
     */
    protected final @NotNull Rule lookahead(final @NotNull Object input) {
        var rule = of(input);
        return LookaheadRule.create(rule);
    }

    /**
     * Creates a {@link NegativeLookaheadRule}.
     * The output can be a {@link EpsilonTerm} if the input is epsilon.
     *
     * @param input The rule to avoid.
     * @return The new rule.
     */
    protected final @NotNull Rule negate(final @NotNull Object input) {
        var rule = of(input);
        return NegativeLookaheadRule.create(rule);
    }

    /**
     * Creates a {@link Rule} to match the codepoint range.
     * This can be any kind of {@link Rule} capable of accomplishing that task.
     *
     * @param lo The minimum codepoint.
     * @param hi The maximum codepoint.
     * @return The new rule.
     */
    protected @NotNull Rule unicodeChar(final int lo, final int hi) {
        return ValueRangeTerm.create(lo, hi);
    }

    /**
     * Creates a {@link Rule} to match the codepoint.
     * This can be any kind of {@link Rule} capable of accomplishing that task.
     *
     * @param loHi The codepoint.
     * @return The new rule.
     */
    protected @NotNull Rule unicodeChar(final int loHi) {
        return ValueRangeTerm.create(loHi, loHi);
    }

    /**
     * Creates a {@link OrderedChoiceRule}.
     * <ul>
     * <li>If the argument List is empty, a {@link EpsilonTerm} is returned instead.</li>
     * <li>If there is only one element in the list, it is returned.</li>
     * <li>Otherwise, returns a {@link OrderedChoiceRule}, as expected.</li>
     * </ul>
     *
     * @param inputRules The parsers for the output.
     * @return A rule.
     */
    protected final @NotNull Rule orderedChoice(final @NotNull List<Object> inputRules) {
        final @NotNull var result = OrderedChoiceRule.create(inputRules.stream().map(this::of).toList());
        return result;
    }

    /**
     * Create a {@link NonTerminal} from the input symbol. The output might be buffered.
     *
     * @param name The name symbol of the output rule.
     * @return A {@link NonTerminal}.
     */
    protected @NotNull NonTerminal nt(final @NotNull Sym name) {
        return buffer.getOrAddNt(name);
    }

    /**
     * Equivalent to {@link #nt(Sym)} with the symbol creates from the input string.
     *
     * @param name The name symbol of the output rule.
     * @return A {@link NonTerminal}.
     */
    protected @NotNull NonTerminal nt(final @NotNull String name) {
        return buffer.getOrAddNt(Sym.sym(name));
    }

    /**
     * Creates a {@link StringTerm}
     * or {@link EpsilonTerm} if the string is empty.
     *
     * @param string          The string to match.
     * @param caseInsensitive Whether the Terminal will match case-insensitive.
     * @return The new rule.
     */
    protected @NotNull Rule string(final @NotNull String string, final boolean caseInsensitive) {
        return buffer.getOrAddString(string, caseInsensitive);
    }

    /**
     * Creates a case-sensitive {@link StringTerm}
     * or {@link EpsilonTerm} if the string is empty.
     *
     * @param string The string to match.
     * @return The new rule.
     */
    protected final @NotNull Rule stringCS(final @NotNull String string) {
        return string(string, false);
    }

    /**
     * Creates a case-insensitive {@link StringTerm}
     * or {@link EpsilonTerm} if the string is empty.
     *
     * @param string The string to match.
     * @return The new rule.
     */
    protected final @NotNull Rule stringCI(final @NotNull String string) {
        return string(string, true);
    }

    /**
     * Creates a {@link StringTerm}
     * or {@link EpsilonTerm} if the string is empty
     * . The case-sensitivity depends on the options used for this {@link GrammarBuilder}.
     *
     * @param string The string to match.
     * @return The new rule.
     */
    protected final @NotNull Rule string(final @NotNull String string) {
        return switch (options.stringCaseInsensitive()) {
            case TRUE -> buffer.getOrAddString(string, true);
            case FALSE, DEFAULT -> buffer.getOrAddString(string, false);
        };
    }

    /**
     * Create a copy of the input for which {@link Rule#isHidden} returns true.
     *
     * @param o The input.
     * @return A copy of the input for which {@link Rule#isHidden} returns true.
     */
    protected final @NotNull Rule hide(final @NotNull Object o) {
        return of(o).enableHideTag();
    }

    /**
     * Get an epsilon rule.
     *
     * @return An {@link EpsilonTerm}.
     */
    protected final @NotNull Rule epsilon() {
        return EpsilonTerm.getDefault();
    }

    /**
     * Create a rule which matches end of input.
     *
     * @return A rule.
     */
    protected final @NotNull Rule eof() {
        return EOFTerm.getDefault();
    }

    /**
     * Creates a {@link ConcatRule}.
     * <ul>
     * <li>If the argument List is empty, a {@link EpsilonTerm} is returned instead.</li>
     * <li>If there is only one element in the list, it is returned.</li>
     * <li>Otherwise, returns a {@link ConcatRule}, as expected.</li>
     * </ul>
     *
     * @param rule  First rule for the output.
     * @param rules More rules for the output.
     * @param <T>   Type for the rules.
     * @return A rule.
     */
    @SafeVarargs
    protected final <T> @NotNull Rule concat(
            final @Nullable T rule, final @Nullable T... rules) {
        return concat(Stream.concat(
                        Stream.of(rule),
                        Arrays.stream(rules))
                .map(this::of)
                .toList());
    }

    /**
     * Creates a {@link ConcatRule}.
     * <ul>
     * <li>If the argument List is empty, a {@link EpsilonTerm} is returned instead.</li>
     * <li>If there is only one element in the list, it is returned.</li>
     * <li>Otherwise, returns a {@link ConcatRule}, as expected.</li>
     * </ul>
     *
     * @param rules The rules for the output.
     * @return A rule.
     */
    protected final @NotNull Rule concat(
            final @NotNull List<@NotNull Rule> rules) {
        return ConcatRule.create(rules);
    }

    /**
     * Like {@link #concat(List)}, except it assumes that (1) the input is not empty and (2) the input does not include epsilons.
     *
     * @param rules The rules.
     * @return A {@link ConcatRule}.
     */
    protected @NotNull ConcatRule concatNoEpsilonMoreThan1(
            final @NotNull List<Rule> rules) {
        return ConcatRule.createNoEpsilonMoreThan1(rules);
    }

    /**
     * Creates a {@link AlternationRule}.
     * <ul>
     * <li>If the argument List is empty, a {@link EpsilonTerm} is returned instead.</li>
     * <li>If there is only one element in the list, it is returned.</li>
     * <li>Otherwise, returns a {@link AlternationRule}, as expected.</li>
     * </ul>
     *
     * @param rule  The first element for the output.
     * @param rules More elements.
     * @return A rule.
     */
    protected final @NotNull Rule alternation(
            final @NotNull Object rule, final @NotNull Object... rules) {
        final @NotNull List<@NotNull Rule> result = Stream
                .concat(Stream.of(rule), Arrays.stream(rules))
                .map(this::of)
                .distinct()
                .toList();
        return alternationC(result);
    }

    /**
     * Creates a {@link AlternationRule}.
     * <ul>
     * <li>If the argument List is empty, a {@link EpsilonTerm} is returned instead.</li>
     * <li>If there is only one element in the list, it is returned.</li>
     * <li>Otherwise, returns a {@link AlternationRule}, as expected.</li>
     * </ul>
     *
     * @param rules The rules for the output.
     * @return A rule.
     */
    protected final @NotNull Rule alternationC(final @NotNull List<Rule> rules) {
        return AlternationRule.create(rules);
    }

    /**
     * Like {@link #alternationC(List)} except the input list is assumed to be distinct
     * (each rule in the list occurs exactly once) and not empty.
     * Use this method only if you are sure that the rules are distinct.
     *
     * @param rules The rules.
     * @return A rule.
     */
    protected final @NotNull AlternationRule alternationGuaranteeDistinctAndNotEmpty(
            final @NotNull List<Rule> rules) {
        return AlternationRule.createGuaranteeDistinctAndNotEmpty(rules);
    }

    /**
     * Represents a special sequence. The typical EBNF-notation is {@code ?...?}
     * where {@code ...} stands for some free-form text.
     * For example, {@code S = ?any whitespace except newline?} means what it says.
     * This allows some interaction with the program around the parser.
     * However, special sequences have to be heavily abstracted here.
     *
     * @param description The description of the special sequence.
     * @param function    The function which does what the description says.
     * @return A {@link SpecialSequenceRule}.
     */
    protected @NotNull Rule specialSequence(
            final @NotNull String description,
            final @NotNull Function<@NotNull String, Optional<String>> function) {
        return SpecialSequenceRule.create(description, function);
    }

    /**
     * Creates a {@link ZeroOrMoreRule} or an {@link EpsilonTerm} if the input is epsilon.
     * <ul>
     * <li>If minimum and maximum amount of repetitions is set to 0, return an {@link EpsilonTerm}.</li>
     * <li>If minimum and maximum amount of repetitions is set to 1, may return the input rule.</li>
     * <li>If minimum and maximum amount of repetitions are equal, a {@link ConcatRule} might be returned instead.</li>
     * <li>Otherwise, does as normally expected.</li>
     * </ul>
     *
     * @param min  The minimum amount of repetitions.
     * @param max  The maximum amount of repetitions.
     * @param rule The rule.
     * @return A rule, as described.
     */
    protected final @NotNull Rule repeat(
            final @NotNull Rule rule, final int min, final int max) {
        var r = of(rule);
        return VariableRepetitionRule.create(r, min, max);
    }

    /**
     * Creates a {@link ExclusionRule}.
     *
     * @param ruleExpected The rule that must be matched.
     * @param ruleExcluded The rule that must not be matched.
     * @return A {@link ExclusionRule}.
     */
    protected final @NotNull Rule exclude(
            final @NotNull Rule ruleExpected, final @NotNull Rule ruleExcluded) {
        final @NotNull var r1 = ruleExpected;
        final @NotNull var r2 = ruleExcluded;
        return ExclusionRule.create(r1, r2);
    }

    /**
     * Equivalent to {@code repeat(rule, exact, exact)}.
     *
     * @param rule  The rule.
     * @param exact Minimum and maximum number of repetitions.
     * @return A repetition rule.
     * @see #repeat(Rule, int, int)
     */
    protected final @NotNull Rule repeat(
            final @NotNull Rule rule, final int exact) {
        return repeat(rule, exact, exact);
    }

    /**
     * Equivalent to {@code repeat(rule, min, Integer.MAX_VALUE)}.
     *
     * @param rule The rule.
     * @param min  Minimum number of repetitions.
     * @return A repetition rule.
     * @see #repeat(Rule, int, int)
     */
    protected final @NotNull Rule repeatMin(
            final @NotNull Rule rule, final int min) {
        return repeat(rule, min, Integer.MAX_VALUE);
    }

    /**
     * Equivalent to {@code repeat(rule, 0, max)}.
     *
     * @param rule The rule.
     * @param max  Maximum number of repetitions.
     * @return A repetition rule.
     * @see #repeat(Rule, int, int)
     */
    protected final @NotNull Rule repeatMax(
            final @NotNull Rule rule, final int max) {
        return repeat(rule, 0, max);
    }

    /**
     * Creates a {@link ZeroOrMoreRule} or an {@link EpsilonTerm} if the input is epsilon.
     *
     * @param rule The rule to match repeatedly.
     * @return A rule.
     */
    protected final @NotNull Rule zeroOrMore(final @NotNull Rule rule) {
        return repeat(rule, 0, Integer.MAX_VALUE);
    }

    /**
     * Creates a {@link OnceOrMoreRule} or an {@link EpsilonTerm} if the input is epsilon.
     *
     * @param rule The rule to match repeatedly.
     * @return A rule.
     */
    protected final @NotNull Rule onceOrMore(final @NotNull Rule rule) {
        return repeat(rule, 1, Integer.MAX_VALUE);
    }

    /**
     * Creates a {@link OptionalRule} or an {@link EpsilonTerm} if the input is epsilon.
     *
     * @param rule The rule to maybe match.
     * @return A rule.
     */
    protected final @NotNull Rule optional(final @NotNull Rule rule) {
        return OptionalRule.create(rule);
    }

    private void compress() {
        for (final @NotNull var symRuleEntry : productions.entrySet()) {
            final @NotNull var value = symRuleEntry.getValue();
            final @NotNull var compressedRule = compressRule(value);
            if (compressedRule != value) {// Yes, I need a reference equality check here.
                symRuleEntry.setValue(compressedRule);
            }
        }
    }

    private @NotNull Rule compressRule(final @NotNull Rule originalRule) {
        if (originalRule instanceof VariableRepetitionRule) {
            final var variableRepetitionRule = (VariableRepetitionRule) originalRule;
            final var min = variableRepetitionRule.getMin();
            final var max = variableRepetitionRule.getMax();
            final @NotNull var rule = compressRule(variableRepetitionRule.getRule());

            Rule newRule = null;
            if (max == Integer.MAX_VALUE) {
                if (min == 0) newRule = ZeroOrMoreRule.create(rule);
                else if (min == 1) newRule = OnceOrMoreRule.create(rule);
            } else if (min == 0 && max == 1) {
                newRule = OptionalRule.create(rule);
            }
            if (newRule == null) {
                newRule = variableRepetitionRule;
            }

            return newRule
                    .withHideTag(variableRepetitionRule.isHidden())
                    .withReduction(variableRepetitionRule.getReduction());
        }
        if (originalRule instanceof RuleWithManyChildren) {
            var ruleWithManyChildren = (RuleWithManyChildren) originalRule;
            return ruleWithManyChildren
                    .withRules(ruleWithManyChildren
                            .getRules()
                            .stream()
                            .map(this::compressRule)
                            .toList());
        }
        if (originalRule instanceof RuleWithChild) {
            var ruleWithChild = (RuleWithChild) originalRule;
            return ruleWithChild.withRule(compressRule(ruleWithChild.getRule()));
        }
        return originalRule;
    }

    private @NotNull Rule autoWhitespaceHelper(
            final @NotNull Rule originalRule,
            final @NotNull Rule whitespaceRule) {
        if (originalRule instanceof NonTerminal) {
            return originalRule;
        }
        if (originalRule instanceof EpsilonTerm) {
            return originalRule;
        }
        if (originalRule instanceof RuleWithChild) {
            var rule = (RuleWithChild) originalRule;
            return rule.withRule(
                    autoWhitespaceHelper(rule.getRule(), whitespaceRule));
        }
        if (originalRule instanceof RuleWithManyChildren) {
            var combWithParsers = (RuleWithManyChildren) originalRule;
            final @NotNull List<@NotNull Rule> rules = combWithParsers
                    .getRules()
                    .stream()
                    .map(p -> autoWhitespaceHelper(p, whitespaceRule))
                    .toList();
            return combWithParsers.withRules(rules);
        }
        if (originalRule instanceof Terminal) {
            final @NotNull List<Rule> rules = new ArrayList<>();
            rules.add(whitespaceRule);
            final @NotNull Rule result;
            if (!originalRule.getReduction().isHiddenOrRaw()) {
                // Hide the terminal in the output.
                // It still appears in the tree, but is flattened into the concatenation.
                rules.add(originalRule.withReduction(ReductionType.
                        standardIntermediateReduction()));
                result = concat(rules).withReduction(
                        originalRule.getReduction());
            } else {
                rules.add(originalRule);
                result = concat(rules);
            }
            return result;
        }
        if (originalRule instanceof SpecialSequenceRule) {
            return originalRule;
        }
        throw new IllegalArgumentException(originalRule.getClass().getName());
//        return switch (originalRule) {
//            case NonTerminal ignored -> originalRule;
//            case EpsilonTerm ignored2 -> originalRule;
//            case RuleWithChild rule -> (rule.withRule(
//                    autoWhitespaceHelper(rule.getRule(), whitespaceRule)));
//            case RuleWithManyChildren combWithParsers -> {
//                final @NotNull List<@NotNull Rule> rules = combWithParsers
//                        .getRules()
//                        .stream()
//                        .map(p -> autoWhitespaceHelper(p, whitespaceRule))
//                        .toList();
//                yield (combWithParsers.withRules(rules));
//            }
//            case Terminal ignored -> {
//                final @NotNull List<Rule> rules = new ArrayList<>();
//                rules.add(whitespaceRule);
//                final @NotNull Rule result;
//                if (!originalRule.getReduction().isHiddenOrRaw()) {
//                    // Hide the terminal in the output.
//                    // It still appears in the tree, but is flattened into the concatenation.
//                    rules.add(originalRule.withReduction(ReductionType.
//                            standardIntermediateReduction()));
//                    result = concat(rules).withReduction(
//                            originalRule.getReduction());
//                } else {
//                    rules.add(originalRule);
//                    result = concat(rules);
//                }
//                yield result;
//            }
//            case SpecialSequenceRule specialSequenceRule -> specialSequenceRule;
//        };
    }

    private void autoWhitespace(final @NotNull Sym start,
                                final @NotNull Grammar grammarWS,
                                final @NotNull Sym startWS) {
        final @NotNull Rule wsParser =
                optional(nt(startWS)).enableHideTag();

        final @NotNull LinkedHashMap<@NotNull Sym, @NotNull Rule> finalGrammar =
                new LinkedHashMap<>(productions);
        for (final @NotNull var symRuleEntry : finalGrammar.entrySet()) {
            symRuleEntry.setValue(autoWhitespaceHelper(
                    symRuleEntry.getValue(), wsParser));
        }

        final @NotNull Rule startWithoutReduction = (
                finalGrammar.get(start)
                        .withReduction(ReductionType.standardInitialReduction()));
        final @NotNull Rule newStartComb =
                concat(List.of(startWithoutReduction, wsParser))
                        .withReduction(finalGrammar.get(start).getReduction());

        finalGrammar.put(start, newStartComb);
        finalGrammar.putAll(grammarWS);
        finalGrammar.put(startWS,
                Objects.requireNonNull(grammarWS.getProduction(startWS)).hideTag());

        productions = finalGrammar;
    }

    protected <T extends Rule> T buffer(T rule) {
        if (rule instanceof StringTerm) {
            buffer.getOrAdd((StringTerm) rule);
        }
        if (rule instanceof RegexTerm) {
            buffer.getOrAdd((RegexTerm) rule);
        }
        if (rule instanceof NonTerminal) {
            buffer.getOrAdd((NonTerminal) rule);
        }
        return rule;
    }
}
