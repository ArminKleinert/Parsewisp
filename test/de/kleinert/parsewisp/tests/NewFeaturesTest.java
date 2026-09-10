package de.kleinert.parsewisp.tests;

import de.kleinert.parsewisp.Parsewisp;
import de.kleinert.parsewisp.parser.Parser;
import de.kleinert.parsewisp.parser_options.ParserCreationOptions;
import de.kleinert.parsewisp.parser_options.ParsingOptions;
import de.kleinert.parsewisp.parser_options.RulesAvailable;
import de.kleinert.parsewisp.testutil.PT;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

class NewFeaturesTest {

    @Test void test0 () {
        {
            var p = Parsewisp.parser("S = 'a' epsilon* 'b'");
            System.out.println(p.parse("bc"));
        }
    }
    @Test
    void test1() {
        var grammar = """
                S = 'a' A
                A = epsilon A | epsilon
                """;
        var p = Parsewisp.parser(grammar);
        System.out.println(p.parse("a"));
        System.out.println(p.parses("a").stream().limit(5).toList());
    }
    @Test
    void testExampleFromWikipedia() {
        var grammar = """
                S = digit digit-excluding-zero*
                digit = "0" | digit-excluding-zero ;
                digit-excluding-zero = "1" | "2" | "3" | "4" | "5" | "6" | "7" | "8" | "9" ;
                """;
        var p = Parsewisp.parser(grammar);
        System.out.println(p.parse("91"));
        System.out.println(p.parse("091"));
    }
    @Test
    void testArithmetic() {
        var p = Parsewisp.parser("""
               sum          = sum ('+'|'-') sum | product
               product      = power ('*'|'/') product | power
               power        = paren-or-val '^' power | paren-or-val
               paren-or-val = '(' sum ')' | number
               number       = ('+'|'-')? ('0'|'1'|'2'|'3'|'4'|'5'|'6'|'7'|'8'|'9')+
               """, ParserCreationOptions.newWithStandardWhitespace());
        System.out.println(p.parse("1"));
        System.out.println(p.parse("1+2"));
        System.out.println(p.parse("1 + 2"));
        System.out.println(p.parse("1*2"));
        System.out.println(p.parse("1+2*3^4"));
        System.out.println(p.parse("1*2+3*4"));
        System.out.println(p.parse("1*(2+-3)^4"));
    }
    @Test
    void negativeEpsilon() {
        var p = Parsewisp.parser("S = !epsilon epsilon 'a'");
        System.out.println(p.parse("a"));
    }
    @Test
    void zeroOrMoreRuleCausesInfiniteEpsilonProblem() {
        var p = Parsewisp.parser("S = (S epsilon)*", ParserCreationOptions.getDefault());
        System.out.println(p.grammar().analyze().definedTerminals());
        System.out.println(p.parses("").stream().limit(5).toList());
    }

    @Test
    void bigTest() throws IOException {
        var p = Parsewisp.parser(
                Files.readString(Path.of("testres/grammars/c99.g")),
                ParserCreationOptions.newWithStandardWhitespace()
        );
        var res = p.parse("""
                int utf8_from_codepoint(utf8_code_pt c, utf8_chr *const buff) {
                  if (c < 0x80) {
                    buff[0] = ((c >> 0) & 0x7F) | 0x00;
                    return 1;
                  } else if (c < 0x0800) {
                    buff[0] = ((c >> 6) & 0x1F) | 0xC0;
                    buff[1] = ((c >> 0) & 0x3F) | 0x80;
                    return 2;
                  } else if (c < 0x010000) {
                    buff[0] = ((c >> 12) & 0x0F) | 0xE0;
                    buff[1] = ((c >> 6) & 0x3F) | 0x80;
                    buff[2] = ((c >> 0) & 0x3F) | 0x80;
                    return 3;
                  } else if (c < 0x110000) {
                    buff[0] = ((c >> 18) & 0x07) | 0xF0;
                    buff[1] = ((c >> 12) & 0x3F) | 0x80;
                    buff[2] = ((c >> 6) & 0x3F) | 0x80;
                    buff[3] = ((c >> 0) & 0x3F) | 0x80;
                    return 4;
                  }
                  return -1;
                }
                """.repeat(156));
        System.out.println(res.castToParseSuccess().getSpanStart());
        System.out.println(res.castToParseSuccess().getSpanEndExclusive());
    }

    @Test
    void orderedChoiceTest() {
        {
            final @NotNull var p = Parsewisp.parser("""
                    S = A / B / C / D / E
                    A = ε
                    B = ε
                    C = ε
                    D = ε
                    E = ε
                    """);
            System.out.println(p);
            var ps = p.parses("", ParsingOptions.getDefault()).stream().toList();
            System.out.println();
            System.out.println("Expect: [[:S, [:A]], [:S, [:B]], [:S, [:C]], [:S, [:D]], [:S, [:E]]]");
            System.out.println("Have:   " + ps);
        }
        {
            final @NotNull var grammar = """
                    S = (r1 / r2)*
                    r1 = 'a'
                    r2 = 'a'
                    """;
            final @NotNull var text = "aa";
            final @NotNull var p = Parsewisp.parser(grammar);
            final @NotNull var possibleParses = List.of(
                    PT.create("S", PT.create("r1", "a"), PT.create("r1", "a")),
                    PT.create("S", PT.create("r2", "a"), PT.create("r1", "a")),
                    PT.create("S", PT.create("r1", "a"), PT.create("r2", "a")),
                    PT.create("S", PT.create("r2", "a"), PT.create("r2", "a"))
            );
//            System.out.println("Expect: "+possibleParses);
//            System.out.println("Have:   "+Parsewisp.parses(p, text));
        }
    }

    @Test
    void wsExample1() {
        final @NotNull Parser whitespace = Parsewisp.parser(
                """
                        whitespace = #'\\s+'
                        """);
        final @NotNull Parser auto_whitespace_example = Parsewisp.parser(
                """
                        S = A B
                        <A> = 'foo'
                        <B> = #'\\d+'
                        """,
                ParserCreationOptions.newWithStandardWhitespace());

        var tree = PT.create("S", "foo", "123");

        Assertions.assertEquals(tree, auto_whitespace_example.parse("foo 123"));
    }

    @Test
    void simple() {
        var g = """
                Expression = Term , { ( '+' | '-' ) , Term } ;
                Term       = Factor , { ( '*' | '/' ) , Factor } ;
                Factor     = Number | '(', Expression, ')' ;
                Number     = ['+' | '-' ] Digit , { Digit } ;
                Digit      = '0' | '1' | '2' | '3' | '4' | '5' | '6' | '7' | '8' | '9' ;
                """;
        var p = Parsewisp.parser(g);
        System.out.println(p.parse("(8-9)*-20/18+1"));
    }

    @Test
    void repRepTest() {
        var p = Parsewisp.parser("S = 0*4A\n<A> = 'a'");
        Assertions.assertEquals(PT.create("S"), p.parse(""));
        Assertions.assertEquals(PT.create("S", "a"), p.parse("a"));
        Assertions.assertEquals(PT.create("S", "a", "a"), p.parse("aa"));
        Assertions.assertEquals(PT.create("S", "a", "a", "a"), p.parse("aaa"));
        Assertions.assertEquals(PT.create("S", "a", "a", "a", "a"), p.parse("aaaa"));
    }

    @Test
    void exclusionFullTest1() {
        var p6 = Parsewisp.parser(
                "S := #'[0-9]+' - ('11' | '13')",
                ParserCreationOptions.getDefault().addAvailableRule(RulesAvailable.EXCLUSION));
        System.out.println(p6);
        System.out.println("---");
        System.out.println(p6.parse("12"));
        System.out.println("---");
        System.out.println(p6.parse("11"));
    }

    @Test
    void exclusionFullTest() {
        var p6 = Parsewisp.parser(
                "S := #'[0-9]+' - '11'",
                ParserCreationOptions.getDefault().addAvailableRule(RulesAvailable.EXCLUSION));
        System.out.println(p6);
        System.out.println("---");
        System.out.println(p6.parse("12"));
        System.out.println("---");
        System.out.println(p6.parse("11"));
    }

    @Test
    void exclusionTest() {
        var p6 = Parsewisp.parser(
                "S := #'[0-9]+' - '11' 'a'",
                ParserCreationOptions.getDefault().addAvailableRule(RulesAvailable.EXCLUSION));
        System.out.println(p6);
        System.out.println("---");
        System.out.println(p6.parse("12a"));
        System.out.println("---");
        System.out.println(p6.parse("12"));
        System.out.println("---");
        System.out.println(p6.parse("11a"));
        System.out.println("---");
        System.out.println(p6.parse("ba"));
    }
}
