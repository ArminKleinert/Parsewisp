package de.kleinert.parsewisp.grammar;

import de.kleinert.parsewisp.Sym;
import de.kleinert.parsewisp.parsing.*;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This class implements methods to provide pretty formatting for grammars. It can be overridden for other grammar formats.
 * By default, the default printer can be used as follows to print a grammar:
 * <pre>
 * {@code
 * var p = Parsewisp.parser(
 *         """
 *         S = P S | eps
 *         P = "(" S* ")"
 *         """); // A variant of the grammar of balanced parentheses
 * System.out.println(p.show()); // Use default printer.
 * }
 * </pre>
 * The code above prints
 * <pre>
 * {@code
 * S = P S | ε ;
 * P = "(" S* ")" ;
 * }
 * </pre>
 * To use a different printer, it can be included in the options when creating a parser, or be invoked later.
 * <pre>
 * {@code
 * var p = Parsewisp.parser(
 *         "S = P S | eps ; P = \"(\" S* \")\" ;",
 *         ParserCreationOptions.getDefault().withPrinter(new GrammarPrinter())); // << Or pass another printer
 *
 * System.out.println(new GrammarPrinter().toString(p.grammar())); // Use specific printer.
 * }
 * </pre>
 * <p>
 * To implement your own printer, you need to override the methods as follows:
 * <table>
 *     <caption>A table of rules/elements of a grammar with the associated methods in the printer.</caption>
 *     <tbody>
 *     <tr><th>Element/Class</th><th>Method</th><th>Default</th></tr>
 *     <tr><td>Production</td><td>{@link #productionToString(Sym, Rule)}</td><td>{@code name = ... ;}</td></tr>
 *     <tr><td>{@link AlternationRule}</td><td>{@link #altToString(AlternationRule)}</td><td>{@code rule1 | ... | ruleN} (Rules divided by bars)</td></tr>
 *     <tr><td>{@link ConcatRule}</td><td>{@link #catToString(ConcatRule)}</td><td>{@code rule1 ... ruleN} (rules separated by spaces)</td></tr>
 *     <tr><td>{@link EOFTerm}</td><td>{@link #eofToString(EOFTerm)}</td><td>{@code EOF}</td></tr>
 *     <tr><td>{@link EpsilonTerm}</td><td>{@link #epsToString(EpsilonTerm)}</td><td>{@code ε}</td></tr>
 *     <tr><td>{@link ExclusionRule}</td><td>{@link #exclusionToString(ExclusionRule)}</td><td>{@code expectedRule - excludedRule} (rule 1 and 2 divided by a dash)</td></tr>
 *     <tr><td>{@link LookaheadRule}</td><td>{@link #lookToString(LookaheadRule)}</td><td>{@code &rule} (AND-symbol followed by the rule)</td></tr>
 *     <tr><td>{@link NegativeLookaheadRule}</td><td>{@link #negToString(NegativeLookaheadRule)}</td><td>{@code !rule} (BANG-symbol followed by the rule)</td></tr>
 *     <tr><td>{@link NonTerminal}</td><td>{@link #ntToString(NonTerminal)}</td><td>The name.</td></tr>
 *     <tr><td>{@link OnceOrMoreRule}</td><td>{@link #onceOrMoreToString(OnceOrMoreRule)}</td><td>{@code rule+} (The rule followed by a "+")</td></tr>
 *     <tr><td>{@link OptionalRule}</td><td>{@link #optToString(OptionalRule)}</td><td>{@code rule?} (The rule followed by a "?")</td></tr>
 *     <tr><td>{@link OrderedChoiceRule}</td><td>{@link #ordToString(OrderedChoiceRule)}</td><td>{@code rule1 / ... / ruleN} (Rules separated by slashes)</td></tr>
 *     <tr><td>{@link RegexTerm}</td><td>{@link #regexpToString(RegexTerm)}</td><td>{@code #"..."} (Hash-symbol and then the pattern between double-quotes)</td></tr>
 *     <tr><td>{@link SpecialSequenceRule}</td><td>{@link #specialToString(SpecialSequenceRule)}</td><td>{@code ?...?} (The description between question-marks)</td></tr>
 *     <tr><td>{@link StringTerm}</td><td>{@link #literalToString(StringTerm)}</td><td>{@code "..."} (The string in double-quotes)</td></tr>
 *     <tr><td>{@link ValueRangeTerm}</td><td>{@link #valRangeToString(ValueRangeTerm)}</td><td>{@code %x...-...} (ABNF hexadecimal format)</td></tr>
 *     <tr><td>{@link VariableRepetitionRule}</td><td>{@link #repToString(VariableRepetitionRule)}</td><td>{@code n*m rule} (minimum STAR maximum followed by the rule)</td></tr>
 *     <tr><td>{@link ZeroOrMoreRule}</td><td>{@link #zeroOrMoreToString(ZeroOrMoreRule)}</td><td>{@code rule*} (The rule followed by a "*")</td></tr>
 *     </tbody>
 * </table>
 *
 *
 */
public class GrammarPrinter {
    /**
     * Default constructor.
     */
    public GrammarPrinter() {
    }

    @Override
    public String toString() {
        return GrammarPrinter.class.getSimpleName();
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    /**
     * Runs {@link #productionToString(Sym, Rule)} on each rule in the grammar. The starting rule is always printed first.
     *
     * @param grammar The grammar.
     * @return A string.
     */
    public @NotNull String toString(final @NotNull Grammar grammar) {
        var sb = new StringBuilder();
        var start = grammar.getStartSym();
        sb.append(productionToString(start, Objects.requireNonNull(grammar.getProduction(start))));
        for (var symRuleEntry : grammar.entrySet()) {
            if (!Objects.equals(symRuleEntry.getKey(), start)) {
                sb.append('\n');
                sb.append(productionToString(symRuleEntry.getKey(), symRuleEntry.getValue()));
            }
        }
        return sb.toString();
    }

    /**
     * Escapes a string. The terminator is a codepoint which is always escaped.
     * Otherwise, the escaped codepoints are {@code \b}, {@code \n}, {@code \t}, {@code \f}, {@code \r}, {@code \\}.
     * The intention is to escape only the symbols that need to be escaped. For example, a grammar may use single quotes to represent strings (as in {@code '...'}). In such a case, this method should be invoked with {@code '\''} as its second argument to escape any single quote in the string.
     * <pre>
     * {@code
     * printer.escape("'abc'", '\''); // => \'abc\'
     * printer.escape("\"a88b\nc\"\\", '\''); // ?> "a88b\nc"\\
     * }
     * </pre>
     *
     * @param s                   The string.
     * @param terminatorCodepoint The terminator.
     * @return A string.
     */
    public @NotNull String escape(final @NotNull String s, int terminatorCodepoint) {
        var sb = new StringBuilder();
        var codeIter = s.codePoints().iterator();
        while (codeIter.hasNext()) {
            var i = codeIter.nextInt();
            if (i == terminatorCodepoint) {
                sb.append('\\').appendCodePoint(terminatorCodepoint);
            } else {
                switch (i) {
                    case '\b' -> sb.append("\\b");
                    case '\n' -> sb.append("\\n");
                    case '\t' -> sb.append("\\t");
                    case '\f' -> sb.append("\\f");
                    case '\r' -> sb.append("\\r");
                    case '\\' -> sb.append("\\\\");
                    default -> sb.appendCodePoint(i);
                }
            }
        }
        return sb.toString();
    }

    /**
     * Precedences for rule types. The output is used to determine whether a rule needs to be wrapped in parentheses when printing.
     * For example, a zero-or-more repetition of the rule {@code A | B} would be printed as {@code (A | B)*} because the alternation has a higher precedence.
     * <p>
     * The order of precedence (descending) is:
     * <ol>
     *     <li>{@link AlternationRule} and {@link OrderedChoiceRule}</li>
     *     <li>{@link ConcatRule}</li>
     *     <li>{@link ExclusionRule}</li>
     *     <li>{@link LookaheadRule} and {@link NegativeLookaheadRule}</li>
     *     <li>{@link OptionalRule}, {@link OnceOrMoreRule} and {@link ZeroOrMoreRule}</li>
     *     <li>{@link VariableRepetitionRule}</li>
     *     <li>Any rule that can not wrap other rules. In other words: {@link Terminal}, {@link NonTerminal} or {@link SpecialSequenceRule}</li>
     * </ol>
     *
     * @param rule the rule of which the precedence is needed.
     * @return An int.
     */
    public static int precedence(final @NotNull Rule rule) {
        if (rule instanceof AlternationRule || rule instanceof OrderedChoiceRule)
            return 100;
        if (rule instanceof ConcatRule)
            return 80;
        if (rule instanceof ExclusionRule)
            return 70;
        if (rule instanceof LookaheadRule || rule instanceof NegativeLookaheadRule)
            return 60;
        if (rule instanceof OptionalRule || rule instanceof OnceOrMoreRule | rule instanceof ZeroOrMoreRule)
            return 50;
        if (rule instanceof VariableRepetitionRule)
            return 40;
        if (rule instanceof Terminal || rule instanceof NonTerminal || rule instanceof SpecialSequenceRule)
            return 10;
        throw new IllegalArgumentException();
    }

    /**
     * Prints  in the format {@code }.
     *
     * @param sym The production's name.
     * @param rhs The rule on the right-hand-side of the separator.
     * @return A string.
     */
    public @NotNull String productionToString(final @NotNull Sym sym, final @NotNull Rule rhs) {
        return sym.name() + " = " + ruleToString(rhs) + " ;";
    }

    /**
     * Wraps the second argument in parentheses if its precedence is lower than the first rule's precedence.
     * <p>
     * Example: {@link AlternationRule} has a higher precedence than {@link ConcatRule}, so an alternation of concatenations would print the concatenations in parentheses.
     * <pre>
     * {@code
     *         // As a grammar, this would be `S = "a" "b" | "c" "d"` or the equivalent `S = ("a" "b") | ("c" "d")`
     *         var rule0 = AlternationRule.create(List.of(
     *                 ConcatRule.create(List.of(
     *                         StringTerm.create("a", false),
     *                         StringTerm.create("b", false))),
     *                 ConcatRule.create(List.of(
     *                         StringTerm.create("c", false),
     *                         StringTerm.create("d", false)))
     *         ));
     *         System.out.println(printer.ruleToString(rule0)); // => "a" "b" | "c" "d"
     * }
     * </pre>
     * But if a concatenation contains two alternations, parentheses are required.
     * <pre>
     * {@code
     *         // As a grammar, this would be `S = ("a" | "b") ("c" | "d")`
     *         var rule1 = ConcatRule.create(List.of(
     *                 AlternationRule.create(List.of(
     *                         StringTerm.create("a", false),
     *                         StringTerm.create("b", false))),
     *                 AlternationRule.create(List.of(
     *                         StringTerm.create("c", false),
     *                         StringTerm.create("d", false)))
     *         ));
     *         System.out.println(printer.ruleToString(rule1)); // => ("a" | "b") ("c" | "d")
     * }
     * </pre>
     *
     * @param current     The parent rule.
     * @param ruleToPrint The rule which is wrapped by the first rule.
     * @return A string.
     */
    public @NotNull String parens(final @NotNull Rule current, final @NotNull Rule ruleToPrint) {
        if (precedence(current) <= precedence(ruleToPrint))
            return "(" + ruleToString(ruleToPrint) + ")";
        return ruleToString(ruleToPrint);
    }

    /**
     * This method just delegates the printing to a specific rule for the type of the input.
     *
     * @param rule The rule.
     * @return A string.
     */
    public @NotNull String ruleToString(final @NotNull Rule rule) {
        var sb = new StringBuilder();
        String s;

        if (rule.isHidden())
            sb.append('<');

        if (rule instanceof AlternationRule) {
            s = altToString((AlternationRule) rule);
        } else if (rule instanceof OrderedChoiceRule) {
            s = ordToString((OrderedChoiceRule) rule);
        } else if (rule instanceof ConcatRule) {
            s = catToString((ConcatRule) rule);
        } else if (rule instanceof ExclusionRule) {
            s = exclusionToString((ExclusionRule) rule);
        } else if (rule instanceof LookaheadRule) {
            s = lookToString((LookaheadRule) rule);
        } else if (rule instanceof NegativeLookaheadRule) {
            s = negToString((NegativeLookaheadRule) rule);
        } else if (rule instanceof OptionalRule) {
            s = optToString((OptionalRule) rule);
        } else if (rule instanceof OnceOrMoreRule) {
            s = onceOrMoreToString((OnceOrMoreRule) rule);
        } else if (rule instanceof ZeroOrMoreRule) {
            s = zeroOrMoreToString((ZeroOrMoreRule) rule);
        } else if (rule instanceof VariableRepetitionRule) {
            s = repToString((VariableRepetitionRule) rule);
        } else if (rule instanceof NonTerminal) {
            s = ntToString((NonTerminal) rule);
        } else if (rule instanceof SpecialSequenceRule) {
            s = specialToString((SpecialSequenceRule) rule);
        } else if (rule instanceof EOFTerm) {
            s = eofToString((EOFTerm) rule);
        } else if (rule instanceof EpsilonTerm) {
            s = epsToString((EpsilonTerm) rule);
        } else if (rule instanceof RegexTerm) {
            s = regexpToString((RegexTerm) rule);
        } else if (rule instanceof StringTerm) {
            s = literalToString((StringTerm) rule);
        } else if (rule instanceof ValueRangeTerm) {
            s = valRangeToString((ValueRangeTerm) rule);
        } else {
            throw new IllegalArgumentException("Unsupported rule type: " + rule);
        }

        sb.append(s);

        if (rule.isHidden())
            sb.append('>');

        return sb.toString();

    }

    /**
     * Prints a {@link AlternationRule}.
     * <p>
     * Default format: {@code rule1 | rule2 | ... | ruleN}
     *
     * @param rule The rule.
     * @return A string-representation of the rule
     */
    public @NotNull String altToString(final @NotNull AlternationRule rule) {
        return rule.getRules().stream()
                .map(it -> parens(rule, it))
                .collect(Collectors.joining(" | "));
    }

    /**
     * Prints a {@link ConcatRule}.
     * <p>
     * Default format: {@code rule1 rule 2 ... ruleN}
     *
     * @param rule The rule.
     * @return A string-representation of the rule
     */
    public @NotNull String catToString(final @NotNull ConcatRule rule) {
        return rule.getRules().stream()
                .map(it -> parens(rule, it))
                .collect(Collectors.joining(" "));
    }

    /**
     * Prints a {@link EOFTerm}.
     * <p>
     * Default format: {@code EOF}
     *
     * @param rule The rule.
     * @return A string-representation of the rule
     */
    public @NotNull String eofToString(final @NotNull EOFTerm rule) {
        return EOFTerm.text();
    }

    /**
     * Prints a {@link EpsilonTerm}.
     * <p>
     * Default format: {@code ε}
     *
     * @param rule The rule.
     * @return A string-representation of the rule
     */
    public @NotNull String epsToString(final @NotNull EpsilonTerm rule) {
        return "ε";
    }

    /**
     * Prints a {@link ExclusionRule}.
     * <p>
     * Default format: {@code ruleExpected - ruleExcluded}
     *
     * @param rule The rule.
     * @return A string-representation of the rule
     */
    public @NotNull String exclusionToString(final @NotNull ExclusionRule rule) {
        return parens(rule, rule.getExpected()) + " - " + parens(rule, rule.getExcluded());
    }

    /**
     * Prints a {@link LookaheadRule}.
     * <p>
     * Default format: {@code &rule}
     *
     * @param rule The rule.
     * @return A string-representation of the rule
     */
    public @NotNull String lookToString(final @NotNull LookaheadRule rule) {
        return "&" + parens(rule, rule.getRule());
    }

    /**
     * Prints a {@link NegativeLookaheadRule}.
     * <p>
     * Default format: {@code !rule}
     *
     * @param rule The rule.
     * @return A string-representation of the rule
     */
    public @NotNull String negToString(final @NotNull NegativeLookaheadRule rule) {
        return "!" + parens(rule, rule.getRule());
    }

    /**
     * Prints a {@link NonTerminal}.
     * <p>
     * Default format: {@code rule} (just the name)
     *
     * @param rule The rule.
     * @return A string-representation of the rule
     */
    public @NotNull String ntToString(final @NotNull NonTerminal rule) {
        return rule.getKeyword().name();
    }

    /**
     * Prints a {@link OnceOrMoreRule}.
     * <p>
     * Default format: {@code rule+}
     *
     * @param rule The rule.
     * @return A string-representation of the rule
     */
    public @NotNull String onceOrMoreToString(final @NotNull OnceOrMoreRule rule) {
        return parens(rule, rule.getRule()) + "+";
    }

    /**
     * Prints a {@link OptionalRule}.
     * <p>
     * Default format: {@code rule?}
     *
     * @param rule The rule.
     * @return A string-representation of the rule
     */
    public @NotNull String optToString(final @NotNull OptionalRule rule) {
        return parens(rule, rule.getRule()) + "?";
    }

    /**
     * Prints a {@link OrderedChoiceRule}.
     * <p>
     * Default format: {@code rule1 / rule 2 / ... / ruleN}
     *
     * @param rule The rule.
     * @return A string-representation of the rule
     */
    public @NotNull String ordToString(final @NotNull OrderedChoiceRule rule) {
        return rule.getRules().stream()
                .map(it -> parens(rule, it))
                .collect(Collectors.joining(" / "));
    }

    /**
     * Prints a {@link RegexTerm}.
     * <p>
     * Default format: {@code #"text"}
     *
     * @param rule The rule.
     * @return A string-representation of the rule
     */
    public @NotNull String regexpToString(final @NotNull RegexTerm rule) {
        return "#\"" + rule.getRegexp().pattern() + "\"";
    }

    /**
     * Prints a {@link SpecialSequenceRule}.
     * <p>
     * Default format: {@code ?description?}
     *
     * @param rule The rule.
     * @return A string-representation of the rule
     */
    public @NotNull String specialToString(final @NotNull SpecialSequenceRule rule) {
        return "?" + rule.getDescription() + "?";
    }

    /**
     * Prints a {@link StringTerm}.
     * <p>
     * Default format: {@code "text"}
     *
     * @param rule The rule.
     * @return A string-representation of the rule
     */
    public @NotNull String literalToString(final @NotNull StringTerm rule) {
        return '"' + escape(rule.getString(), '"') + '"';
    }

    /**
     * Prints a {@link ValueRangeTerm} (range of Unicode characters).
     * <p>
     * Default format: {@code %xX-X} or {@code %xX}
     *
     * @param rule The rule.
     * @return A string-representation of the rule
     */
    public @NotNull String valRangeToString(final @NotNull ValueRangeTerm rule) {
        if (rule.getLo() == rule.getHi()) return String.format("%%x%X", rule.getLo());
        return String.format("%%x%X-%X", rule.getLo(), rule.getHi());
    }

    /**
     * Prints a {@link VariableRepetitionRule}.
     * <p>
     * Default format: {@code min*max rule}
     *
     * @param rule The rule.
     * @return A string-representation of the rule
     */
    public @NotNull String repToString(final @NotNull VariableRepetitionRule rule) {
        return rule.getMin() + "*" + rule.getMax() + parens(rule, rule.getRule());
    }

    /**
     * Prints a {@link ZeroOrMoreRule}.
     * <p>
     * Default format: {@code rule*}
     *
     * @param rule The rule.
     * @return A string-representation of the rule
     */
    public @NotNull String zeroOrMoreToString(final @NotNull ZeroOrMoreRule rule) {
        return parens(rule, rule.getRule()) + "*";
    }
}
