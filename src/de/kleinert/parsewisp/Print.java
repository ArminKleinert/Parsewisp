package de.kleinert.parsewisp;

import de.kleinert.parsewisp.grammar.Grammar;
import de.kleinert.parsewisp.parser.*;
import de.kleinert.parsewisp.parsing.*;
import de.kleinert.parsewisp.reduction.ReductionType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Helpers for things converting things to strings.
 */
public final class Print {
    private Print() {
    }

    private static @NotNull String parenForTags(
            final @NotNull Predicate<@NotNull Rule> tags,
            final boolean hidden,
            final @NotNull Rule rule) {
        if (!hidden && tags.test(rule)) return "(" + ruleToString(rule, false) + ")";
        return ruleToString(rule, false);
    }

    private static @NotNull String parenForCompound(final boolean hidden, final @NotNull Rule rule) {
        return parenForTags(
                (rule1) -> rule1 instanceof RuleWithManyChildren,
                hidden, rule);
    }

    private static @NotNull String escape(final @NotNull String s) {
        var sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            String replacement = switch (c) {
                case '\\' -> "\\\\";
                case '\t' -> "\\t";
                case '\b' -> "\\b";
                case '\n' -> "\\n";
                case '\r' -> "\\r";
                case '\f' -> "\\f";
                case '\'' -> "\\'";      // <== not necessary
                case '\"' -> "\\\"";
                default -> "";
            };
            if (replacement.isEmpty()) sb.append(c);
            else sb.append(replacement);
        }
        return sb.toString();
    }

    /**
     * Returns a string representing the argument.
     *
     * @param rule The argument.
     * @return A string.
     */
    public static @NotNull String ruleToString(final @NotNull Rule rule) {
        return ruleToString(rule, false);
    }

    private static @NotNull String ruleToString(final @NotNull Rule rule, final boolean hidden) {
        if (!hidden && rule.isHidden()) {
            return "< " + ruleToString(rule, true) + " >";
        } else if (rule instanceof EpsilonTerm) {
            return "ε";
        } else if (rule instanceof OptionalRule) {
            return "[ " + parenForCompound(hidden, ((RuleWithChild) rule).getRule()) + " ]";
        } else if (rule instanceof OnceOrMoreRule) {
            return parenForCompound(hidden, ((RuleWithChild) rule).getRule()) + "+";
        } else if (rule instanceof ZeroOrMoreRule) {
            return "{ " + parenForCompound(hidden, ((RuleWithChild) rule).getRule()) + " }";
        } else if (rule instanceof VariableRepetitionRule) {
            final var repRule = (VariableRepetitionRule) rule;
            final int min = repRule.getMin();
            final int max = repRule.getMax();
            return ""
                    + min
                    + '*'
                    + ((max < Integer.MAX_VALUE) ? max : "")
                    + parenForCompound(hidden, repRule.getRule());
        } else if (rule instanceof AlternationRule) {
            final @NotNull List<String> ruleStrings =
                    ((RuleWithManyChildren) rule).getRules()
                            .stream()
                            .map(p -> parenForTags((rule1) -> rule1 instanceof RuleWithManyChildren, hidden, p))
                            .toList();
            return String.join(" | ", ruleStrings);
        } else if (rule instanceof OrderedChoiceRule) {
            final @NotNull List<String> ruleStrings =
                    ((RuleWithManyChildren) rule).getRules()
                            .stream()
                            .map(p -> parenForTags((rule1) -> rule1 instanceof RuleWithManyChildren, hidden, p))
                            .toList();
            return String.join(" / ", ruleStrings);
        } else if (rule instanceof ConcatRule) {
            final @NotNull List<Rule> children = ((RuleWithManyChildren) rule).getRules();
            final @NotNull Predicate<Rule> ks = (rule1) -> rule1 instanceof RuleWithManyChildren;
            final @NotNull Iterable<String> ruleStrings =
                    children.stream().map(r -> parenForTags(ks, hidden, r)).toList();
            return String.join(" ", ruleStrings);
        } else if (rule instanceof StringTerm) {
            return "\"" + escape(((StringTerm) rule).getString()) + "\"";
        } else if (rule instanceof ValueRangeTerm) {
            var valueRangeTerm = (ValueRangeTerm) rule;
            final int lo = valueRangeTerm.getLo();
            final int hi = valueRangeTerm.getHi();
            return lo == hi ? String.format("%%x%X", lo) : String.format("%%x%X-%X", lo, hi);
            //return new StringBuilder().appendCodePoint(lo).append('-').appendCodePoint(hi).toString();
        } else if (rule instanceof RegexTerm) {
            return "#\"" + ((RegexTerm) rule).getRegexp().pattern() + '"';
        } else if (rule instanceof NonTerminal) {
            return ((NonTerminal) rule).getKeyword().name();
        } else if (rule instanceof LookaheadRule) {
            return "&" + parenForCompound(hidden, ((RuleWithChild) rule).getRule());
        } else if (rule instanceof NegativeLookaheadRule) {
            return "!" + parenForCompound(hidden, ((RuleWithChild) rule).getRule());
        } else if (rule instanceof SpecialSequenceRule) {
            return "?" + rule + "?";
        } else if (rule instanceof ExclusionRule) {
            final @NotNull List<String> partStrings =
                    ((RuleWithManyChildren) rule).getRules()
                            .stream()
                            .map(rule2 -> parenForTags((rule1) -> rule1 instanceof RuleWithManyChildren, hidden, rule2))
                            .toList();
            return String.join(" - ", partStrings);
        } else if (rule instanceof EOFTerm) {
            return "eof";
        } else {
            throw new IllegalArgumentException("Can not handle value " + rule + " of type " + rule.getClass() + ".");
        }
    }

    private static @NotNull String ruleToString(
            final @NotNull Sym startProd, final @NotNull Rule rule) {
        final ReductionType red = rule.getReduction();
        if (red.isHiddenOrRaw())
            return "<" + startProd.name() + '>' + " = " + ruleToString(rule);
        else
            return startProd.name() + " = " + ruleToString(rule);
    }

    /**
     * Returns a (likely multiline) string representing a {@link Parser}.
     *
     * @param p The parser.
     * @return A string.
     */
    public static @NotNull String parserToString(final @NotNull Parser p) {
        final @NotNull Grammar grammar = p.grammar();
        final @NotNull Sym start = p.startProduction();

        final @NotNull StringBuilder sb = new StringBuilder(
                ruleToString(start, Objects.requireNonNull(grammar.getProduction(start))));

        grammar.forEach((nonTerminal, parser) -> {
                    if (!Objects.equals(nonTerminal, start)) {
                        sb.append('\n').append(ruleToString(nonTerminal, parser));
                    }
                }
        );
        return sb.toString();
    }
}
