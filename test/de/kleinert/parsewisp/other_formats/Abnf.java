package de.kleinert.parsewisp.other_formats;

import de.kleinert.parsewisp.Parsewisp;
import de.kleinert.parsewisp.Sym;
import de.kleinert.parsewisp.error.ParserCreationFailure;
import de.kleinert.parsewisp.grammar.Grammar;
import de.kleinert.parsewisp.parser.Parser;
import de.kleinert.parsewisp.parser_options.ParserCreationOptions;
import de.kleinert.parsewisp.parsing.*;
import de.kleinert.parsewisp.result.Node;
import de.kleinert.parsewisp.result.ParseTree;
import de.kleinert.parsewisp.util.StrParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class Abnf {
    private final @NotNull StrParser strParser = new StrParser();
    private Abnf() {
    }

    public static Parser parser(@NotNull String grammar, @Nullable ParserCreationOptions options) {
        if (options == null) {
            options = ParserCreationOptions.getDefault();
        }

        var abnfGrammarParser = Parsewisp.parser(new AbnfBuilder(ParserCreationOptions.getDefault()).build(), ParserCreationOptions.getDefault());
        var tree = abnfGrammarParser.parse(grammar);

        if (tree.isFailure()) {
            throw new ParserCreationFailure(tree.castToParseFailure().toString());
        }

        return Parsewisp.parser(new Abnf().transform(tree.castToParseSuccess(), options.startProduction()), ParserCreationOptions.getDefault());
    }


    private Grammar transform(ParseTree parsedAbnfGrammar, @Nullable Sym start) {
        var prods = new LinkedHashMap<Sym, Rule>();
        for (Node node : parsedAbnfGrammar.getContent()) {
            var prod = rule(node.tree());
            if (start == null) start = prod.getKey().getKeyword();
            var lhs = (prod.getKey().isHidden()) ? prod.getValue().enableHideTag() : prod.getValue();
            prods.put(prod.getKey().getKeyword(), lhs);
        }
        assert start != null;
        return new Grammar(start, prods);
    }

    // Tree has format
    //    [:rule, [:nonterm, "A"], "=", [:alternation, ...]]
    //    [:rule, [:hide-nt, "<", "A", ">"], "=", [:alternation, ...]]
    Map.Entry<NonTerminal, Rule> rule(ParseTree tree) {
        var name = ntOrHideNt(tree.getNode(0).tree());
        var rhs = alternation(tree.getNode(2).tree());
        return Map.entry(name, rhs);
    }

    NonTerminal ntOrHideNt(ParseTree tree) {
        if (Objects.equals(Sym.sym("hide-nt"), tree.getTag().content())) {
            return (NonTerminal) NonTerminal.create(Sym.sym(tree.getNode(1).string())).hideTag();
        }
        return NonTerminal.create(Sym.sym(tree.getNode(0).string()));
    }

    // Format: [:alternation, [:concatenation, ...], ...]
    Rule alternation(ParseTree tree) {
        return AlternationRule.create(
                tree.getContent().stream()
                        .filter(it -> it.content() instanceof ParseTree)
                        .map(Node::tree)
                        .map(this::concatenation)
                        .toList());
    }

    // Format: [:concatenation, [:repetition, ...], [:repetition, ...]]
    Rule concatenation(ParseTree tree) {
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
    Rule repetition(ParseTree tree) {
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
            }
            else if (s.charAt(s.length()-1)=='*') {
                // Only minimum given
                min = Integer.parseInt(parts[0]);
                max = Integer.MAX_VALUE;
            }
            else {
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

    ParseTree findAlternation(ParseTree pt) {
        var content = pt.getContent();
        for (var sub : content) {
            if (sub.content() instanceof ParseTree && ((ParseTree) sub.content()).getTag().content().equals(Sym.sym("alternation"))) {
                return sub.tree();
            }
        }
        throw new IllegalStateException();
    }

    // element        =  nonterm / hide / group / option / char-val / num-val
    Rule element(ParseTree tree) {
        var inner = tree.getNode(0).tree();
        var innerTag =inner.getTag().content();
        if (innerTag.equals(Sym.sym("nonterm"))) {
        }else
        if (innerTag.equals(Sym.sym("hide"))) {
            // hide          =  "<" *c-wsp alternation *c-wsp ">"
            return alternation(findAlternation(inner)).enableHideTag();
        }else
        if (innerTag.equals(Sym.sym("group"))) {
            // group          =  "(" *c-wsp alternation *c-wsp ")"
            return alternation(findAlternation(inner));
        }else
        if (innerTag.equals(Sym.sym("option"))) {
            // option         =  "[" *c-wsp alternation *c-wsp "]"
            return OptionalRule.create(alternation(findAlternation(inner)));
        }else
        if (innerTag.equals(Sym.sym("char-val"))) {
            // char-val       =  DQUOTE *(%x20-21 / %x23-7E) DQUOTE ; quoted string of SP and VCHAR without DQUOTE
            return charVal(inner);
        }else
        if (innerTag.equals(Sym.sym("regexp"))) {
            // regexp         = #'[^'\\\\]*(?:\\\\.[^'\\\\]*)*' / #\\\"[^\\\"\\\\]*(?:\\\\.[^\\\"\\\\]*)*\\\"
            return regexp(inner);
        }else
        if (innerTag.equals(Sym.sym("num-val"))) {
            // num-val        =  "%" (bin-val / dec-val / hex-val)
            return numVal(inner);
        }
        throw new IllegalStateException();
    }

    // Actual implementation: char-val       =  #"(%[is])?\\\"[^\\\"\\\\]*(?:\\\\.[^\\\"\\\\]*)*\\\"";
    Rule charVal(ParseTree tree) {
        var string = tree.getNode(0).string();
        System.out.println(string);
        if (string.startsWith("%")) {
            return StringTerm.create(
                    strParser.processString(string.substring(2)), string.charAt(1) == 'i');
        }
        return StringTerm.create(strParser.processString(string), true);

    }

    // regexp         = #'[^'\\\\]*(?:\\\\.[^'\\\\]*)*' / #\\\"[^\\\"\\\\]*(?:\\\\.[^\\\"\\\\]*)*\\\"
    Rule regexp(ParseTree tree) {                        return RegexTerm.create(
            strParser.processRegexp((String)
                    tree.getContent().get(0).content()));}

    // num-val        =  "%" (bin-val / dec-val / hex-val)
    // bin-val        =  "b" 1*BIT [ 1*("." 1*BIT) / ("-" 1*BIT) ] ; series of concatenated bit values or single ONEOF range
    // dec-val        =  "d" 1*DIGIT [ 1*("." 1*DIGIT) / ("-" 1*DIGIT) ]
    // hex-val        =  "x" 1*HEXDIG [ 1*("." 1*HEXDIG) / ("-" 1*HEXDIG) ]
    Rule numVal(ParseTree tree) {
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
        if (minusIndex<0) {
            var s = digitStr.chars().filter(c->c!='.').mapToObj(it->String.valueOf((char) it)).collect(Collectors.joining());
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
