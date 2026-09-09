package de.kleinert.parsewisp.tests.typical.rules_available;

import de.kleinert.parsewisp.Parsewisp;
import de.kleinert.parsewisp.parser_options.ParserCreationOptions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class RuleAlternativesTests {
    @Test
    void oneOrMoreRepetitionReplacements() {
        var p1 = Parsewisp.parser("S = \"a\"+");
        var p2 = Parsewisp.parser("S = \"a\" {\"a\"}");
        var p3 = Parsewisp.parser("""
                S   = "a" | "a" A
                <A> = "a" A | ε
                """);

        Assertions.assertTrue(p1.parse("").isFailure());
        Assertions.assertTrue(p2.parse("").isFailure());
        Assertions.assertTrue(p3.parse("").isFailure());

        Assertions.assertEquals(p1.parse("aa"), p2.parse("aa"));
        Assertions.assertEquals(p1.parse("aa"), p3.parse("aa"));
    }

    @Test
    void optionalRepetitionReplacements() {
        var p1 = Parsewisp.parser("S = {\"a\"}");
        var p2 = Parsewisp.parser("""
                S   = ε | A
                <A> = "a" A | ε
                """);
        Assertions.assertEquals(p1.parse(""), p2.parse(""));
        Assertions.assertEquals(p1.parse("aa"), p2.parse("aa"));
    }

    @Test
    void epsilonReplacements() {
        var p1 = Parsewisp.parser("S = ε");
        var p2 = Parsewisp.parser("S = \"\"");
        Assertions.assertEquals(p1.parse(""), p2.parse(""));
    }

    @Test
    void valueRangeReplacements() {
        var p1 = Parsewisp.parser("S = %x41-5a", ParserCreationOptions.abnf());
        var p2 = Parsewisp.parser("S = #\"[\\x{41}-\\x{5a}]\"");
        var p3 = Parsewisp.parser("S = \"A\" | \"B\" | \"C\" | \"D\" | \"E\" | \"F\" | \"G\" | \"H\" | \"I\" | \"J\" | \"K\" | \"L\" | \"M\" | \"N\" | \"O\" | \"P\" | \"Q\" | \"R\" | \"S\" | \"T\" | \"U\" | \"V\" | \"W\" | \"X\" | \"Y\" | \"Z\"");
        Assertions.assertEquals(p1.parse("D"), p2.parse("D"));
        Assertions.assertEquals(p1.parse("D"), p3.parse("D"));
    }

    @Test
    void optionReplacements() {
        var p1 = Parsewisp.parser("S = \"a\"?");
        var p2 = Parsewisp.parser("S = ε | \"a\"");
        Assertions.assertEquals(p1.parse("a"), p2.parse("a"));
    }

    @Test
    void variableRepeatExact() {
        var p1 = Parsewisp.parser("S = 4 \"a\"", ParserCreationOptions.abnf());
        var p2 = Parsewisp.parser("S = \"a\" \"a\" \"a\" \"a\"");
        Assertions.assertEquals(p1.parse("aaaa"), p2.parse("aaaa"));
    }

    @Test
    void variableRepeatMin() {
        var p1 = Parsewisp.parser("S = 4* \"a\"", ParserCreationOptions.abnf());
        var p2 = Parsewisp.parser("S = \"a\" \"a\" \"a\" \"a\" {\"a\"}");
        Assertions.assertEquals(p1.parse("aaaa"), p2.parse("aaaa"));
        Assertions.assertEquals(p1.parse("aaaaaa"), p2.parse("aaaaaa"));
    }

    @Test
    void variableRepeatMax() {
        var p1 = Parsewisp.parser("S = *4 \"a\"", ParserCreationOptions.abnf());
        var p2 = Parsewisp.parser("S = \"a\"? \"a\"? \"a\"? \"a\"?");
        Assertions.assertEquals(p1.parse(""), p2.parse(""));
        Assertions.assertEquals(p1.parse("aa"), p2.parse("aa"));
        Assertions.assertEquals(p1.parse("aaaa"), p2.parse("aaaa"));
    }

    @Test
    void variableRepeatMinMax() {
        var p1 = Parsewisp.parser("S = 2*4 \"a\"", ParserCreationOptions.abnf());
        var p2 = Parsewisp.parser("S = \"a\" \"a\" \"a\"? \"a\"?");
        Assertions.assertEquals(p1.parse("aa"), p2.parse("aa"));
        Assertions.assertEquals(p1.parse("aaaa"), p2.parse("aaaa"));
    }

    @Test
    void variableRepeatFree() {
        var p1 = Parsewisp.parser("S = * \"a\"", ParserCreationOptions.abnf());
        var p2 = Parsewisp.parser("S = {\"a\"}");
        Assertions.assertEquals(p1.parse(""), p2.parse(""));
        Assertions.assertEquals(p1.parse("aaaa"), p2.parse("aaaa"));
    }

    @Test
    void epsilon() {
        {
            var p1 = Parsewisp.parser("S = \"a\" ε");
            var p2 = Parsewisp.parser("S = \"a\" \"\"", ParserCreationOptions.getDefault().withEpsilonNames(List.of()));
            Assertions.assertEquals(p1.parse("a"), p2.parse("a"));
        }
        {
            var p1 = Parsewisp.parser("S = ε");
            var p2 = Parsewisp.parser("S = \"\"", ParserCreationOptions.getDefault().withEpsilonNames(List.of()));
            Assertions.assertEquals(p1.parse(""), p2.parse(""));
        }
    }
}
