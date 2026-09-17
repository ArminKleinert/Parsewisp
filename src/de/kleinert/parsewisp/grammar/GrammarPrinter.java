package de.kleinert.parsewisp.grammar;

import de.kleinert.parsewisp.Sym;
import de.kleinert.parsewisp.parsing.*;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class GrammarPrinter {
    public GrammarPrinter() {
    }

    public @NotNull String toString(final @NotNull Grammar grammar) {
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

    public @NotNull String escape(@NotNull String s, int terminator) {
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

    public int precedence(final @NotNull Rule rule) {
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

    private @NotNull String productionToString(final @NotNull Sym sym, final @NotNull Rule rhs) {
        return sym.name() + " = " + toString(rhs);
    }

    public @NotNull String maybeBraces(final @NotNull Rule current, final @NotNull Rule ruleToPrint) {
        if (precedence(current) < precedence(ruleToPrint))
            return "(" + toString(ruleToPrint) + ")";
        return toString(ruleToPrint);
    }

    public @NotNull String toString(final @NotNull Rule rule) {
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

    public String altToString(final @NotNull AlternationRule rule) {
        return rule.getRules().stream()
                .map(it -> maybeBraces(rule, it))
                .collect(Collectors.joining(" / "));
    }

    public String catToString(final @NotNull ConcatRule rule) {
        return rule.getRules().stream()
                .map(it -> maybeBraces(rule, it))
                .collect(Collectors.joining(" "));
    }

    public String eofToString(final @NotNull EOFTerm rule) {
        return "EOF";
    }

    public String epsToString(final @NotNull EpsilonTerm rule) {
        return "eps";
    }

    public String exclusionToString(final @NotNull ExclusionRule rule) {
        return maybeBraces(rule, rule.getExpected()) + " - " + maybeBraces(rule, rule.getExcluded());
    }

    public String lookToString(final @NotNull LookaheadRule rule) {
        return "&" + maybeBraces(rule, rule.getRule());
    }

    public String negToString(final @NotNull NegativeLookaheadRule rule) {
        return "!" + maybeBraces(rule, rule.getRule());
    }

    public String ntToString(final @NotNull NonTerminal rule) {
        return rule.getKeyword().name();
    }

    public String onceOrMoreToString(final @NotNull OnceOrMoreRule rule) {
        return maybeBraces(rule, rule.getRule())+"+";
    }

    public String optToString(final @NotNull OptionalRule rule) {
        return maybeBraces(rule, rule.getRule()) + "?";
    }

    public String ordToString(final @NotNull OrderedChoiceRule rule) {
        return rule.getRules().stream()
                .map(it -> maybeBraces(rule, it))
                .collect(Collectors.joining(" / "));
    }

    public String regexpToString(final @NotNull RegexTerm rule) {
        return "#\"" + rule.getRegexp().pattern() + "\"";
    }

    public String specialToString(final @NotNull SpecialSequenceRule rule) {
        return "?" + rule + "?";
    }

    public String literalToString(final @NotNull StringTerm rule) {
        return '"'+escape(rule.getString(), '"')+'"';
    }

    public String valRangeToString(final @NotNull ValueRangeTerm rule) {
        if (rule.getLo() == rule.getHi()) return String.format("%%x%X", rule.getLo());
        return String.format("%%x%X-%X", rule.getLo(), rule.getHi());
    }

    public String repToString(final @NotNull VariableRepetitionRule rule) {
        return rule.getMin() + "*" + rule.getMax() + maybeBraces(rule, rule.getRule());
    }

    public String zeroOrMoreToString(final @NotNull ZeroOrMoreRule rule) {
        return maybeBraces(rule, rule.getRule())+"*";
    }
}
