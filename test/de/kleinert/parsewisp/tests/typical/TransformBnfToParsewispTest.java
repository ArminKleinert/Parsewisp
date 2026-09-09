package de.kleinert.parsewisp.tests.typical;

import de.kleinert.parsewisp.Parsewisp;
import de.kleinert.parsewisp.Sym;
import de.kleinert.parsewisp.grammar.Grammar;
import de.kleinert.parsewisp.parser.Parser;
import de.kleinert.parsewisp.parser_options.ParserCreationOptions;
import de.kleinert.parsewisp.parser_options.RulesAvailable;
import de.kleinert.parsewisp.parsing.*;
import de.kleinert.parsewisp.result.ParseResult;
import de.kleinert.parsewisp.testutil.PT;
import de.kleinert.parsewisp.util.Transform;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class TransformBnfToParsewispTest {
    private @NotNull Parser parser() {
        try {
            var opts = ParserCreationOptions.getDefault()
                    .addAvailableRule(RulesAvailable.EXPLICIT_EOF)
                    .addAvailableRule(RulesAvailable.ABNF_IDENTIFIERS)
                    .withRuleDefinitionOps(Set.of("::="));
            return Parsewisp.parser(
                    Files.readString(Path.of("testres/grammars/bnf.g")),
                    opts
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    Parser transform(ParseResult apr, Sym start) {
        final @NotNull Map<@NotNull Sym, @NotNull Function<List<Object>, Object>> transform;

        transform = new IdentityHashMap<>();
        transform.put(Sym.sym("S"), this::firstNode);
        transform.put(Sym.sym("syntax"), this::syntax);
        transform.put(Sym.sym("rule"), this::rule);
        transform.put(Sym.sym("opt-whitespace"), this::optWhitespace);
        transform.put(Sym.sym("expression"), this::expression);
        transform.put(Sym.sym("line-end"), this::lineEnd);
        transform.put(Sym.sym("list"), this::list);
        transform.put(Sym.sym("term"), this::term);
        transform.put(Sym.sym("literal"), this::literal);
        transform.put(Sym.sym("text1"), this::text1);
        transform.put(Sym.sym("text2"), this::text2);
        transform.put(Sym.sym("character"), this::firstNode);
        transform.put(Sym.sym("letter"), this::firstNode);
        transform.put(Sym.sym("digit"), this::firstNode);
        transform.put(Sym.sym("symbol"), this::symbol);
        transform.put(Sym.sym("character1"), this::firstNode);
        transform.put(Sym.sym("character2"), this::firstNode);
        transform.put(Sym.sym("rule-name"), this::ruleName);
        transform.put(Sym.sym("rule-char"), this::firstNode);

        Function<Object, Parser> finalizer = (prodList) -> {
            //noinspection unchecked
            var prods = ((List<Map.Entry<Sym, Rule>>) prodList).stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (x, y) -> y, LinkedHashMap::new));
            return Parsewisp.parser(new Grammar(start, prods), ParserCreationOptions.getDefault());
        };

        return Transform.transform(apr, transform, finalizer);
    }

    @Test
    void testTransform() {
        var parse = parser().parse("""
                <S> ::= <opt-whitespace> '+' <opt-whitespace> <number> <opt-whitespace> | <opt-whitespace> '-' <opt-whitespace> <number> <opt-whitespace>
                <number> ::= <digit> <number> | <digit>
                <digit> ::= '0' | '1' | '2' | '3' | '4' | '5' | '6' | '7' | '8' | '9'
                <opt-whitespace> ::= " " <opt-whitespace> | "\\t" <opt-whitespace> | "\\n" <opt-whitespace> | ""
                """);
        var albnf = transform(parse, Sym.sym("S"));
        Assertions.assertEquals(
                PT.create("S",
                        PT.create("opt-whitespace"),
                        "+",
                        PT.create("opt-whitespace"),
                        PT.create("number", PT.create("digit", "9"), PT.create("number", PT.create("digit", "5"))),
                        PT.create("opt-whitespace")),
                albnf.parse("+95")
        );
        Assertions.assertEquals(
                PT.create("S",
                        PT.create("opt-whitespace", " ", PT.create("opt-whitespace")),
                        "+",
                        PT.create("opt-whitespace", "\t", PT.create("opt-whitespace")),
                        PT.create("number", PT.create("digit", "9"), PT.create("number", PT.create("digit", "5"))),
                        PT.create("opt-whitespace")),
                albnf.parse(" +\t95")
        );
    }

    @Test
    void test2() {
        var bnfGrammar = """
                <syntax>         ::= <rule> | <rule> <syntax>
                <rule>           ::= <opt-whitespace> "<" <rule-name> ">" <opt-whitespace> "::=" <opt-whitespace> <expression> <line-end>
                <opt-whitespace> ::= " " <opt-whitespace> | ""
                <expression>     ::= <list> | <list> <opt-whitespace> "|" <opt-whitespace> <expression>
                <line-end>       ::= <opt-whitespace> "\\n" | <opt-whitespace> "\\n" <line-end>
                <list>           ::= <term> | <term> <opt-whitespace> <list>
                <term>           ::= <literal> | "<" <rule-name> ">"
                <literal>        ::= '"' <text1> '"' | "'" <text2> "'"
                <text1>          ::= "" | <character1> <text1>
                <text2>          ::= "" | <character2> <text2>
                <character>      ::= <letter> | <digit> | <symbol>
                <letter>         ::= "A" | "B" | "C" | "D" | "E" | "F" | "G" | "H" | "I" | "J" | "K" | "L" | "M" | "N" | "O" | "P" | "Q" | "R" | "S" | "T" | "U" | "V" | "W" | "X" | "Y" | "Z" | "a" | "b" | "c" | "d" | "e" | "f" | "g" | "h" | "i" | "j" | "k" | "l" | "m" | "n" | "o" | "p" | "q" | "r" | "s" | "t" | "u" | "v" | "w" | "x" | "y" | "z"
                <digit>          ::= "0" | "1" | "2" | "3" | "4" | "5" | "6" | "7" | "8" | "9"
                <symbol>         ::= "\\n" | "\\r" | "\\t" | "|" | " " | "!" | "#" | "$" | "%" | "&" | "(" | ")" | "*" | "+" | "," | "-" | "." | "/" | ":" | ";" | ">" | "=" | "<" | "?" | "@" | "[" | "\\\\" | "]" | "^" | "_" | "`" | "{" | "}" | "~"
                <character1>     ::= <character> | "'"
                <character2>     ::= <character> | '"'
                <rule-name>      ::= <letter> | <rule-name> <rule-char>
                <rule-char>      ::= <letter> | <digit> | "-"
                """;
        var bnfParserForBnf = transform(parser().parse(bnfGrammar), Sym.sym("syntax"));
        Assertions.assertTrue(bnfParserForBnf.parse(bnfGrammar).isSuccess());
    }

    // syntax         ::= rule | rule syntax
    private Object syntax(List<Object> pt) {
        if (pt.size() == 1) return List.of(pt.get(0));
        var arr = new ArrayList<>();
        arr.add(pt.get(0));
        arr.addAll((Collection<?>) pt.get(1));
        return arr;
    }

    // rule           ::= opt-whitespace "<" rule-name ">" opt-whitespace "::=" opt-whitespace expression line-end
    private Object rule(final List<Object> pt) {
        var ruleName = pt.get(2); // rule-name
        var expression = pt.get(7); // expression
        return Map.entry(Sym.sym(ruleName.toString()), expression);
    }

    // line-end       ::= opt-whitespace "\n" | opt-whitespace "\n" line-end
    private Object lineEnd(final List<Object> pt) {
        return null;
    }

    // expression     ::= list | list opt-whitespace "|" opt-whitespace expression
    private Object expression(final List<Object> pt) {
        if (pt.size() == 1)
            return pt.get(0); // list
        var list = (Rule) pt.get(0); // list
        var expr = pt.get(4); // expression
        if (expr instanceof List) {
            //noinspection unchecked
            return AlternationRule.create(Stream.concat(Stream.of(list), ((List<Rule>) expr).stream()).toList());
        }
        return AlternationRule.create(List.of(list, (Rule) expr));
    }

    // opt-whitespace ::= " " opt-whitespace | ""
    private Object optWhitespace(final List<Object> pt) {
        // Do nothing
        return null;
    }

    // list           ::= term | term opt-whitespace list
    private Object list(final List<Object> pt) {
        if (pt.size() > 1)
            return ConcatRule.create(List.of((Rule) pt.get(0), (Rule) pt.get(2))); // term and list
        return pt.get(0); // term
    }

    // term           ::= literal | "<" rule-name ">"
    private Object term(final List<Object> pt) {
        if (pt.size() == 1) return pt.get(0);
        return pt.get(1);
    }

    // literal        ::= '"' text1 '"' | "'" text2 "'"
    private Object literal(final List<Object> pt) {
        return StringTerm.create(pt.get(1).toString(), false);
    }

    // text1          ::= "" | character1 text1
    private Object text1(final List<Object> pt) {
        //System.out.println(pt.stream().map(Object::toString).collect(Collectors.joining()));
        return unescape(pt.stream().map(Object::toString).collect(Collectors.joining()));
    }

    // text2          ::= "" | character2 text2
    private Object text2(final List<Object> pt) {
        return unescape(pt.stream().map(Object::toString).collect(Collectors.joining()));
    }

    // character      ::= letter | digit | symbol
    // letter         ::= "A" | "B" | "C" | "D" | "E" | "F" | "G" | "H" | "I" | "J" | "K" | "L" | "M" | "N" | "O" | "P" | "Q" | "R" | "S" | "T" | "U" | "V" | "W" | "X" | "Y" | "Z" | "a" | "b" | "c" | "d" | "e" | "f" | "g" | "h" | "i" | "j" | "k" | "l" | "m" | "n" | "o" | "p" | "q" | "r" | "s" | "t" | "u" | "v" | "w" | "x" | "y" | "z"
    // digit          ::= "0" | "1" | "2" | "3" | "4" | "5" | "6" | "7" | "8" | "9"
    // character1     ::= character | "'"
    // character2     ::= character | '"'
    // rule-char      ::= letter | digit | "-"
    private Object firstNode(final List<Object> pt) {
        return pt.get(0);
    }

    // symbol         ::= "\n" | "\r" | "\t" | "|" | " " | "!" | "#" | "$" | "%" | "&" | "(" | ")" | "*" | "+" | "," | "-" | "." | "/" | ":" | ";" | ">" | "=" | "<" | "?" | "@" | "[" | "\\" | "]" | "^" | "_" | "`" | "{" | "}" | "~"
    private Object symbol(final List<Object> pt) {
        return pt.get(0);
    }

    // rule-name      ::= letter | rule-name rule-char
    private Object ruleName(final List<Object> pt) {
        return NonTerminal.create(Sym.sym(pt.stream().map(Object::toString).collect(Collectors.joining())));
    }

    private String unescape(String s) {
        return s.replace("\\b", "\b")
            .replace("\\t", "\t")
            .replace("\\n", "\n")
            .replace("\\f", "\f")
            .replace("\\r", "\r")
            .replace("\\s", " ")
            .replace("\\\"", "\"")
            .replace("\\'", "'")
            .replace("\\\\", "\\");}
}
    
