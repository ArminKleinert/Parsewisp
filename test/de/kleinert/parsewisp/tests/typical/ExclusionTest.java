package de.kleinert.parsewisp.tests.typical;

import de.kleinert.parsewisp.Parsewisp;
import de.kleinert.parsewisp.parser_options.ParserCreationOptions;
import de.kleinert.parsewisp.testutil.PT;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ExclusionTest {
    @Test
    void basicTest1() {
        var opts = ParserCreationOptions.getDefault();

        // Allow any number ([0-9]+) except those with two digits ([0-9][0-9]).
        var p = Parsewisp.parser("S = #\"[0-9]+\" - #\"[0-9][0-9]\"", opts);

        Assertions.assertEquals(PT.create("S", "1"), p.parse("1"));
        Assertions.assertTrue(p.parse("11").isFailure());
        Assertions.assertEquals(PT.create("S", "111"), p.parse("111"));
    }

    @Test
    void basicTest2() {
        var opts = ParserCreationOptions.getDefault();

        // Allow any number ([0-9]+) except 11.
        var p = Parsewisp.parser("S = #\"[0-9]+\" - \"11\"", opts);

        Assertions.assertEquals(PT.create("S", "1"), p.parse("1"));
        Assertions.assertTrue(p.parse("11").isFailure());
        Assertions.assertEquals(PT.create("S", "12"), p.parse("12"));
        Assertions.assertEquals(PT.create("S", "111"), p.parse("111"));
    }

    @Test
    void basicTest3() {
        var opts = ParserCreationOptions.getDefault();

        // Allow any number ([0-9]+) except 11. The number is followed by a single "a".
        var p = Parsewisp.parser("S = #\"[0-9]+\" - \"11\" \"a\"", opts);

        Assertions.assertEquals(PT.create("S", "1", "a"), p.parse("1a"));
        Assertions.assertTrue(p.parse("11a").isFailure());
        Assertions.assertEquals(PT.create("S", "12", "a"), p.parse("12a"));
        Assertions.assertTrue(p.parse("12").isFailure());
        Assertions.assertEquals(PT.create("S", "111", "a"), p.parse("111a"));
    }

    @Test
    void basicTest4() {
        var opts = ParserCreationOptions.getDefault();

        // Allow 1, 11 and 111, but not 11..
        var p = Parsewisp.parser("S = (\"1\" | \"11\" | \"111\") - \"11\"", opts);

        Assertions.assertEquals(PT.create("S", "1"), p.parse("1"));
        Assertions.assertTrue(p.parse("11").isFailure());
        Assertions.assertEquals(PT.create("S", "111"), p.parse("111"));
    }

    @Test
    void basicTest5() {
        var opts = ParserCreationOptions.getDefault();

        // Allow 1, 11 and 111, but not 11. The number is followed by a single "a".
        var p = Parsewisp.parser("S = (\"1\" | \"11\" | \"111\") - \"11\" \"a\"", opts);

        Assertions.assertEquals(PT.create("S", "1", "a"), p.parse("1a"));
        Assertions.assertTrue(p.parse("11a").isFailure());
        Assertions.assertEquals(PT.create("S", "111", "a"), p.parse("111a"));
    }

    @Test
    void identifierButNotKeyword() {
        var opts = ParserCreationOptions.getDefault();

        // Allow 1, 11 and 111, but not 11. The number is followed by a single "a".
        var p = Parsewisp.parser("""
                S = Identifier - Keyword
                Identifier = #"_*[a-zA-Z][a-zA-Z0-9_]*"
                Keyword = "int" | "char" | "void"
                """, opts);
        Assertions.assertEquals(PT.create("S", PT.create("Identifier", "a")), p.parse("a"));
        Assertions.assertEquals(PT.create("S", PT.create("Identifier", "int1")), p.parse("int1"));
        Assertions.assertEquals(PT.create("S", PT.create("Identifier", "myint")), p.parse("myint"));
        Assertions.assertTrue(p.parse("int").isFailure());
        Assertions.assertTrue(p.parse("char").isFailure());
        Assertions.assertTrue(p.parse("void").isFailure());
    }

    @Test
    void excludeFromExclusion() {
        var opts = ParserCreationOptions.getDefault();

        // Any number. But any sequence of "1"s, except "11", is not allowed.
        var p = Parsewisp.parser("S = #'[0-9]+' - #'[1]+' - '11'", opts);
        Assertions.assertTrue(p.parse("1").isFailure());
        Assertions.assertEquals(PT.create("S", "12"), p.parse("12"));
        Assertions.assertEquals(PT.create("S", "11"), p.parse("11"));
        Assertions.assertTrue(p.parse("1111").isFailure());
        Assertions.assertEquals(PT.create("S", "2"), p.parse("2"));
    }

    @Test
    void excludeFromExclusionR() {
        var opts = ParserCreationOptions.getDefault();

        // Any number. But any sequence of "1"s, except "11", is not allowed.
        var p = Parsewisp.parser("S = #'[0-9]+' - (#'[1]+' - '11')", opts);
        Assertions.assertTrue(p.parse("1").isFailure());
        Assertions.assertEquals(PT.create("S", "12"), p.parse("12"));
        Assertions.assertEquals(PT.create("S", "11"), p.parse("11"));
        Assertions.assertTrue(p.parse("1111").isFailure());
        Assertions.assertEquals(PT.create("S", "2"), p.parse("2"));
    }

    @Test
    void excludeFromExclusionL() {
        var opts = ParserCreationOptions.getDefault();

        // Any number except sequences of "1"s. "11" is also not allowed.
        var p = Parsewisp.parser("S = (#'[0-9]+' - #'[1]+') - '11'", opts);
        Assertions.assertTrue(p.parse("1").isFailure());
        Assertions.assertEquals(PT.create("S", "12"), p.parse("12"));
        Assertions.assertTrue(p.parse("11").isFailure());
        Assertions.assertTrue(p.parse("1111").isFailure());
        Assertions.assertEquals(PT.create("S", "2"), p.parse("2"));
    }
}
