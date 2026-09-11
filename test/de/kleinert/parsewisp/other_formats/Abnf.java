package de.kleinert.parsewisp.other_formats;

import de.kleinert.parsewisp.Parsewisp;
import de.kleinert.parsewisp.Sym;
import de.kleinert.parsewisp.error.ParserCreationFailure;
import de.kleinert.parsewisp.grammar.Grammar;
import de.kleinert.parsewisp.grammar.GrammarBuilder;
import de.kleinert.parsewisp.parser.Parser;
import de.kleinert.parsewisp.parser_options.ParserCreationOptions;
import de.kleinert.parsewisp.parsing.*;
import de.kleinert.parsewisp.result.Node;
import de.kleinert.parsewisp.result.ParseTree;
import de.kleinert.parsewisp.util.StrParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Abnf {
    private Abnf() {
    }

    public static @NotNull Parser parser(@NotNull String grammar, @Nullable ParserCreationOptions options) {
        if (options == null) {
            options = ParserCreationOptions.getDefault();
        }

        var abnfGrammarParser = Parsewisp.parser(baseGrammar(), ParserCreationOptions.getDefault());
        var tree = abnfGrammarParser.parse(grammar);

        if (tree.isFailure()) {
            throw new ParserCreationFailure(tree.castToParseFailure().toString());
        }

        return Parsewisp.parser(new Abnf().transform(tree.castToParseSuccess(), options), ParserCreationOptions.getDefault());
    }

    public static @NotNull Grammar baseGrammar() {
        return new AbnfBuilder(ParserCreationOptions.getDefault()).build();
    }


    private @NotNull Grammar transform(@NotNull ParseTree parsedAbnfGrammar, @NotNull ParserCreationOptions options) {
        return new Transformer(parsedAbnfGrammar, options).build();
    }

    private static class Transformer extends GrammarBuilder {
        private final @NotNull StrParser strParser = new StrParser();
        private final @NotNull ParseTree parsedAbnfGrammar;

        protected Transformer(@NotNull ParseTree parsedAbnfGrammar, @NotNull ParserCreationOptions options) {
            super(options);
            this.parsedAbnfGrammar = parsedAbnfGrammar;
        }

        @Override
        protected void make() {
            for (@NotNull Node node : parsedAbnfGrammar.getContent()) {
                var prod = rule(node.tree());
                var lhs = (prod.getKey().isHidden()) ? prod.getValue().enableHideTag() : prod.getValue();
                addProduction(prod.getKey().getKeyword(), lhs);
            }

            {
                var CRLF = StringTerm.create("\r\n", false);
                var WSP = RegexTerm.create(Pattern.compile("[\\u0020\\u0009]"));
                addProduction(Sym.sym("ALPHA"), RegexTerm.create(Pattern.compile("[a-zA-Z]")));
                addProduction(Sym.sym("BIT"), RegexTerm.create(Pattern.compile("[01]")));
                addProduction(Sym.sym("CHAR"), RegexTerm.create(Pattern.compile("[\\u0001-\\u007F]")));
                addProduction(Sym.sym("CR"), StringTerm.create("\r", false));
                addProduction(Sym.sym("CRLF"), CRLF);
                addProduction(Sym.sym("CTL"), RegexTerm.create(Pattern.compile("[\\u0000-\\u001F|\\u007F]")));
                addProduction(Sym.sym("DIGIT"), RegexTerm.create(Pattern.compile("[0-9]")));
                addProduction(Sym.sym("DQUOTE"), StringTerm.create("\"", false));
                addProduction(Sym.sym("HEXDIG"), RegexTerm.create(Pattern.compile("[0-9a-fA-F]")));
                addProduction(Sym.sym("HTAB"), RegexTerm.create(Pattern.compile("\t")));
                addProduction(Sym.sym("LF"), RegexTerm.create(Pattern.compile("\n")));
                addProduction(Sym.sym("LWSP"), ZeroOrMoreRule.create(AlternationRule.create(List.of(WSP, ConcatRule.create(List.of(CRLF, WSP))))));
                addProduction(Sym.sym("OCTET"), RegexTerm.create(Pattern.compile("[\\u0000-\\u00FF]")));
                addProduction(Sym.sym("SP"), StringTerm.create(" ", false));
                addProduction(Sym.sym("VCHAR"), RegexTerm.create(Pattern.compile("[\\u0021-\\u007E]")));
                addProduction(Sym.sym("WSP"), WSP);
            }
        }

        // Tree has format
        //    [:rule, [:nonterm, "A"], "=", [:alternation, ...]]
        //    [:rule, [:hide-nt, "<", "A", ">"], "=", [:alternation, ...]]
        private @NotNull Map.Entry<NonTerminal, Rule> rule(@NotNull ParseTree tree) {
            var name = ntOrHideNt(tree.getNode(0).tree());
            var rhs = alternation(tree.getNode(2).tree());
            return Map.entry(name, rhs);
        }

        private @NotNull NonTerminal ntOrHideNt(@NotNull ParseTree tree) {
            if (Objects.equals(Sym.sym("hide-nt"), tree.getTag().content())) {
                return (NonTerminal) NonTerminal.create(Sym.sym(tree.getNode(1).string())).hideTag();
            }
            return NonTerminal.create(Sym.sym(tree.getNode(0).string()));
        }

        // Format: [:alternation, [:concatenation, ...], ...]
        private @NotNull Rule alternation(@NotNull ParseTree tree) {
            return AlternationRule.create(
                    tree.getContent().stream()
                            .filter(it -> it.content() instanceof ParseTree)
                            .map(Node::tree)
                            .map(this::concatenation)
                            .toList());
        }

        // Format: [:concatenation, [:repetition, ...], [:repetition, ...]]
        private @NotNull Rule concatenation(@NotNull ParseTree tree) {
            return ConcatRule.create(
                    tree.getContent().stream()
                            .filter(it -> it.content() instanceof ParseTree)
                            .map(Node::tree)
                            .map(this::repetition)
                            .toList());
        }

        // [:repetition, [:element, ...]]
        // [:repetition, "*", [:element, ...]]
        // [:repetition, "...*", [:element, ...]]
        // [:repetition, "*...", [:element, ...]]
        // [:repetition, "...*...", [:element, ...]]
        private @NotNull Rule repetition(@NotNull ParseTree tree) {
            if (tree.size() == 2) {
                return element(tree.getNode(0).tree());
            }
            var rule = element(tree.getNode(1).tree());
            var s = tree.getNode(0).string();

            if (s.isEmpty()) {
                return rule;
            }

            var parts = s.split("\\*");
            final int min, max;

            if (parts.length == 1) {
                // Format at this point is [0-9]+\\* or \\*[0-9]+ or [0-9]+

                if (s.charAt(0) == '*') {
                    // Only maximum given
                    min = 0;
                    max = Integer.parseInt(parts[0]);
                } else if (s.charAt(s.length() - 1) == '*') {
                    // Only minimum given
                    min = Integer.parseInt(parts[0]);
                    max = Integer.MAX_VALUE;
                } else {
                    // Exact number given
                    min = Integer.parseInt(parts[0]);
                    max = min;
                }
            } else if (parts.length == 0) {
                // No minimum, no maximum
                return ZeroOrMoreRule.create(rule);
            } else {
                min = Integer.parseInt(parts[0]);
                max = Integer.parseInt(parts[1]);
            }

            return VariableRepetitionRule.create(rule, min, max);
        }

        private @NotNull ParseTree findAlternation(ParseTree pt) {
            var content = pt.getContent();
            for (var sub : content) {
                if (sub.content() instanceof ParseTree && ((ParseTree) sub.content()).getTag().content().equals(Sym.sym("alternation"))) {
                    return sub.tree();
                }
            }
            throw new IllegalStateException();
        }

        // element        =  nonterm / hide / group / option / char-val / num-val
        private @NotNull Rule element(@NotNull ParseTree tree) {
            var inner = tree.getNode(0).tree();
            var innerTag = inner.getTag().content();
            if (innerTag.equals(Sym.sym("nonterm"))) {
                // nonterm        = #"[a-zA-Z][a-zA-Z0-9\\-]*"
                return nt(inner.getNode(0).string());
            } else if (innerTag.equals(Sym.sym("hide"))) {
                // hide           =  "<" *c-wsp alternation *c-wsp ">"
                return alternation(findAlternation(inner)).enableHideTag();
            } else if (innerTag.equals(Sym.sym("group"))) {
                // group          =  "(" *c-wsp alternation *c-wsp ")"
                return alternation(findAlternation(inner));
            } else if (innerTag.equals(Sym.sym("option"))) {
                // option         =  "[" *c-wsp alternation *c-wsp "]"
                return OptionalRule.create(alternation(findAlternation(inner)));
            } else if (innerTag.equals(Sym.sym("char-val"))) {
                // char-val       =  DQUOTE *(%x20-21 / %x23-7E) DQUOTE ; quoted string of SP and VCHAR without DQUOTE
                return charVal(inner);
            } else if (innerTag.equals(Sym.sym("regexp"))) {
                // regexp         = #'[^'\\\\]*(?:\\\\.[^'\\\\]*)*' / #\\\"[^\\\"\\\\]*(?:\\\\.[^\\\"\\\\]*)*\\\"
                return regexp(inner);
            } else if (innerTag.equals(Sym.sym("num-val"))) {
                // num-val        =  "%" (bin-val / dec-val / hex-val)
                return numVal(inner);
            }
            throw new IllegalStateException();
        }

        // Actual implementation: char-val       =  #"(%[is])?\\\"[^\\\"\\\\]*(?:\\\\.[^\\\"\\\\]*)*\\\"";
        private @NotNull Rule charVal(@NotNull ParseTree tree) {
            var string = tree.getNode(0).string();
            System.out.println(string);
            if (string.startsWith("%")) {
                return StringTerm.create(
                        strParser.processString(string.substring(2)), string.charAt(1) == 'i');
            }
            return StringTerm.create(strParser.processString(string), true);

        }

        // regexp         = #'[^'\\\\]*(?:\\\\.[^'\\\\]*)*' / #\\\"[^\\\"\\\\]*(?:\\\\.[^\\\"\\\\]*)*\\\"
        private @NotNull Rule regexp(@NotNull ParseTree tree) {
            return RegexTerm.create(
                    strParser.processRegexp((String)
                            tree.getContent().get(0).content()));
        }

        // num-val        =  "%" (bin-val / dec-val / hex-val)
        // bin-val        =  "b" 1*BIT [ 1*("." 1*BIT) / ("-" 1*BIT) ] ; series of concatenated bit values or single ONEOF range
        // dec-val        =  "d" 1*DIGIT [ 1*("." 1*DIGIT) / ("-" 1*DIGIT) ]
        // hex-val        =  "x" 1*HEXDIG [ 1*("." 1*HEXDIG) / ("-" 1*HEXDIG) ]
        private @NotNull Rule numVal(@NotNull ParseTree tree) {
            var sub = tree.getNode(1).tree();
            var prefix = sub.getNode(0).string();

            final int radix = switch (prefix) {
                case "b" -> 2;
                case "d" -> 10;
                case "x" -> 16;
                default -> throw new IllegalStateException();
            };

            System.out.println(sub);

            var digitStr = sub.getNode(1).string();
            var minusIndex = digitStr.indexOf('-');
            if (minusIndex < 0) {
                var s = digitStr.chars().filter(c -> c != '.').mapToObj(it -> String.valueOf((char) it)).collect(Collectors.joining());
                System.out.println(s);
                var minMax = Integer.parseInt(s, radix);
                return ValueRangeTerm.create(minMax, minMax);
            }

            var parts = digitStr.split("-");
            var min = Integer.parseInt(parts[0], radix);
            var max = Integer.parseInt(parts[1], radix);
            return ValueRangeTerm.create(min, max);
        }
    }
}
