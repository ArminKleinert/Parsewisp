package de.kleinert.parsewisp.tests.typical;

import de.kleinert.parsewisp.Parsewisp;
import de.kleinert.parsewisp.parser_options.ParsingOptions;
import de.kleinert.parsewisp.testutil.PT;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

class IterativeRegexTest {
    @Test
    void testCheckByPartials() {
        var p = Parsewisp.parser("S = #'A+'");
        var parses = Parsewisp.parses(p, "AAAA",
                ParsingOptions.getDefault()
                        .withIterativeDeepening(true)
                        .withPartial(true));
        Assertions.assertEquals(
                List.of(PT.create("S", "A"),
                        PT.create("S", "AA"),
                        PT.create("S", "AAA"),
                        PT.create("S", "AAAA")),
                parses);
    }

    @Test
    void testRegexString() {
        var p = Parsewisp.parser("S = #'A+' 'A' | #'A+' 'AA' | #'A+' 'AAA' | #'A+' 'AAAA'");

        Assertions.assertTrue(p.parses("").isEmpty());
        Assertions.assertTrue(p.parses("ABA").isEmpty());

        var parses = Parsewisp.parses(p, "AAAA",
                ParsingOptions.getDefault().withIterativeDeepening(true));
        Assertions.assertEquals(
                Set.of(PT.create("S", "A", "AAA"),
                        PT.create("S", "AA", "AA"),
                        PT.create("S", "AAA", "A")),
                new HashSet<>(parses));
    }

    @Test
    void testStringRegex() {
        var p = Parsewisp.parser("S = 'A' #'A+' | 'AA' #'A+' | 'AAA' #'A+' | 'AAAA' #'A+'");

        Assertions.assertTrue(p.parses("").isEmpty());
        Assertions.assertTrue(p.parses("ABA").isEmpty());

        var parses = Parsewisp.parses(p, "AAAA",
                ParsingOptions.getDefault().withIterativeDeepening(true));
        Assertions.assertEquals(
                Set.of(PT.create("S", "A", "AAA"),
                        PT.create("S", "AA", "AA"),
                        PT.create("S", "AAA", "A")),
                new HashSet<>(parses));
    }

    @Test
    void testRegexRegex() {
        var p = Parsewisp.parser("S = #'A+' #'A+'");

        Assertions.assertTrue(p.parses("").isEmpty());
        Assertions.assertTrue(p.parses("ABA").isEmpty());

        var parses = Parsewisp.parses(p, "AAAA",
                ParsingOptions.getDefault().withIterativeDeepening(true));
        Assertions.assertEquals(
                Set.of(PT.create("S", "A", "AAA"),
                        PT.create("S", "AA", "AA"),
                        PT.create("S", "AAA", "A")),
                new HashSet<>(parses));
    }

    @Test
    void defaultTest() {
        var p = Parsewisp.parser("S = #'A+' 'A'");
        var opts = ParsingOptions.getDefault().withIterativeDeepening(true);
        Assertions.assertEquals(
                PT.create("S", "AA", "A"),
                p.parse("AAA", opts));
    }
}