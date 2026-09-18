package de.kleinert.parsewisp.grammar;

import de.kleinert.parsewisp.Sym;
import de.kleinert.parsewisp.parsing.*;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GrammarPrinter {

    public static record PrinterPrecedenceEntry<T>(int precedence, Function<T, String> f) {}

    Map<Class<Rule>, PrinterPrecedenceEntry> m;
    public GrammarPrinter() {
        this((Map<Class<Rule>, PrinterPrecedenceEntry>) ((Object) Map.ofEntries(
                Map.entry(AlternationRule.class, new PrinterPrecedenceEntry<AlternationRule>(100, GrammarPrinter::altToString)),
                Map.entry(OrderedChoiceRule.class, new PrinterPrecedenceEntry<OrderedChoiceRule>(90, GrammarPrinter::ordToString)),
                Map.entry(ConcatRule.class, new PrinterPrecedenceEntry<ConcatRule>(90, GrammarPrinter::catToString)),
                Map.entry(ExclusionRule.class, new PrinterPrecedenceEntry<ExclusionRule>(90, GrammarPrinter::exclusionToString)),
                Map.entry(LookaheadRule.class, new PrinterPrecedenceEntry<LookaheadRule>(90, GrammarPrinter::lookToString)),
                Map.entry(NegativeLookaheadRule.class, new PrinterPrecedenceEntry<NegativeLookaheadRule>(90, GrammarPrinter::negToString)),
                Map.entry(OptionalRule.class, new PrinterPrecedenceEntry<OptionalRule>(90, GrammarPrinter::optToString)),
                Map.entry(OnceOrMoreRule.class, new PrinterPrecedenceEntry<OnceOrMoreRule>(90, GrammarPrinter::onceOrMoreToString)),
                Map.entry(ZeroOrMoreRule.class, new PrinterPrecedenceEntry<ZeroOrMoreRule>(90, GrammarPrinter::zeroOrMoreToString)),
                Map.entry(VariableRepetitionRule.class, new PrinterPrecedenceEntry<VariableRepetitionRule>(90, GrammarPrinter::repToString)),
                Map.entry(NonTerminal.class, new PrinterPrecedenceEntry<NonTerminal>(90, GrammarPrinter::ntToString)),
                Map.entry(SpecialSequenceRule.class, new PrinterPrecedenceEntry<SpecialSequenceRule>(90, GrammarPrinter::specialToString)),

                Map.entry(StringTerm.class, new PrinterPrecedenceEntry<StringTerm>(90, GrammarPrinter::literalToString)),
                Map.entry(RegexTerm.class, new PrinterPrecedenceEntry<RegexTerm>(90, GrammarPrinter::regexpToString)),
                Map.entry(ValueRangeTerm.class, new PrinterPrecedenceEntry<ValueRangeTerm>(90, GrammarPrinter::valRangeToString)),
                Map.entry(EOFTerm.class, new PrinterPrecedenceEntry<EOFTerm>(90, GrammarPrinter::eofToString)),
                Map.entry(EpsilonTerm.class, new PrinterPrecedenceEntry<EpsilonTerm>(90, GrammarPrinter::epsToString))
        )));
    }
    public GrammarPrinter(Map<Class<Rule>, PrinterPrecedenceEntry> m) {this.m = m;
    }

    public static @NotNull String toString(final @NotNull Grammar grammar) {
        var sb = new StringBuilder();
        var start = grammar.getStartSym();
        sb.append(productionToString(start, grammar.getProduction(start)));
        for (Map.Entry<@NotNull Sym, Rule> symRuleEntry : grammar.entrySet()) {
            if (!Objects.equals(symRuleEntry.getKey(), start)) {
                sb.append('\n');
                sb.append(productionToString(symRuleEntry.getKey(), symRuleEntry.getValue()));
            }
        }
        return sb.toString();
    }

    public static @NotNull String escape(@NotNull String s, int terminator) {
        var sb = new StringBuilder();
        var codeIter = s.codePoints().iterator();
        while (codeIter.hasNext()) {
            var i = codeIter.nextInt();
            if (i == terminator) {
                sb.append('\\').appendCodePoint(terminator);
            } else {
                switch (i) {
                    case '\b' -> sb.append("\\b");
                    case '\n' -> sb.append("\\n");
                    case '\t' -> sb.append("\\t");
                    case '\f' -> sb.append("\\f");
                    case '\r' -> sb.append("\\r");
                    case '"' -> sb.append("\\\"");
                    default -> sb.appendCodePoint(i);
                }
            }
        }
        return sb.toString();
    }

    public static int precedence(final @NotNull Rule rule) {
        if (rule instanceof AlternationRule)
            return 100;
        if (rule instanceof OrderedChoiceRule)
            return 99;
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

    private static @NotNull String productionToString(final @NotNull Sym sym, final @NotNull Rule rhs) {
        return sym.name() + " = " + toString(rhs);
    }

    public static @NotNull String maybeBraces(final @NotNull Rule current, final @NotNull Rule ruleToPrint) {
        if (precedence(current) < precedence(ruleToPrint))
            return "(" + toString(ruleToPrint) + ")";
        return toString(ruleToPrint);
    }

    public static @NotNull String toString(final @NotNull Rule rule) {
return
    }

    public static String altToString(final @NotNull AlternationRule rule) {
        return rule.getRules().stream()
                .map(it -> maybeBraces(rule, it))
                .collect(Collectors.joining(" / "));
    }

    public static String catToString(final @NotNull ConcatRule rule) {
        return rule.getRules().stream()
                .map(it -> maybeBraces(rule, it))
                .collect(Collectors.joining(" "));
    }

    public static String eofToString(final @NotNull EOFTerm rule) {
        return "EOF";
    }

    public static String epsToString(final @NotNull EpsilonTerm rule) {
        return "eps";
    }

    public static String exclusionToString(final @NotNull ExclusionRule rule) {
        return maybeBraces(rule, rule.getExpected()) + " - " + maybeBraces(rule, rule.getExcluded());
    }

    public static String lookToString(final @NotNull LookaheadRule rule) {
        return "&" + maybeBraces(rule, rule.getRule());
    }

    public static String negToString(final @NotNull NegativeLookaheadRule rule) {
        return "!" + maybeBraces(rule, rule.getRule());
    }

    public static String ntToString(final @NotNull NonTerminal rule) {
        return rule.getKeyword().name();
    }

    public static String onceOrMoreToString(final @NotNull OnceOrMoreRule rule) {
        return maybeBraces(rule, rule.getRule())+"+";
    }

    public static String optToString(final @NotNull OptionalRule rule) {
        return maybeBraces(rule, rule.getRule()) + "?";
    }

    public static String ordToString(final @NotNull OrderedChoiceRule rule) {
        return rule.getRules().stream()
                .map(it -> maybeBraces(rule, it))
                .collect(Collectors.joining(" / "));
    }

    public static String regexpToString(final @NotNull RegexTerm rule) {
        return "#\"" + rule.getRegexp().pattern() + "\"";
    }

    public static String specialToString(final @NotNull SpecialSequenceRule rule) {
        return "?" + rule + "?";
    }

    public static String literalToString(final @NotNull StringTerm rule) {
        return '"'+escape(rule.getString(), '"')+'"';
    }

    public static String valRangeToString(final @NotNull ValueRangeTerm rule) {
        if (rule.getLo() == rule.getHi()) return String.format("%%x%X", rule.getLo());
        return String.format("%%x%X-%X", rule.getLo(), rule.getHi());
    }

    public static String repToString(final @NotNull VariableRepetitionRule rule) {
        return rule.getMin() + "*" + rule.getMax() + maybeBraces(rule, rule.getRule());
    }

    public static String zeroOrMoreToString(final @NotNull ZeroOrMoreRule rule) {
        return maybeBraces(rule, rule.getRule())+"*";
    }
}
