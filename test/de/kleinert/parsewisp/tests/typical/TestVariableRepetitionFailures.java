package de.kleinert.parsewisp.tests.typical;

import de.kleinert.parsewisp.Parsewisp;
import de.kleinert.parsewisp.error.ParserCreationFailure;
import de.kleinert.parsewisp.parser_options.ParserCreationOptions;
import de.kleinert.parsewisp.parser_options.ParsingOptions;
import de.kleinert.parsewisp.testutil.PT;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

class TestVariableRepetitionFailures {
    @Test
    void parseWithPartial1() {
        var text = "aaaaaa";
        var treesPartial = Set.of(
                PT.create("S"),
                PT.create("S", "a"),
                PT.create("S", "a", "a"),
                PT.create("S", "a", "a", "a"),
                PT.create("S", "a", "a", "a", "a"),
                PT.create("S", "a", "a", "a", "a", "a"),
                PT.create("S", "a", "a", "a", "a", "a", "a")
        );
        var partialOpts = ParsingOptions.getDefault().withPartial(true);
        var creationOpts = ParserCreationOptions.getDefault();
        var repeated_a = Parsewisp.parser("""
                        S = 0*6 'a' 'a'
                        """,
                creationOpts);
        //System.out.println(repeated_a);
        Assertions.assertEquals(treesPartial, new HashSet<>(repeated_a.parses(text, partialOpts)));
    }
    @Test
    void parseWithPartial2() {
        var text = "aaaaaa";
        var treesPartial = Set.of(
                PT.create("S", "a"),
                PT.create("S", "a", "a"),
                PT.create("S", "a", "a", "a"),
                PT.create("S", "a", "a", "a", "a"),
                PT.create("S", "a", "a", "a", "a", "a"),
                PT.create("S", "a", "a", "a", "a", "a", "a")
        );
        var partialOpts = ParsingOptions.getDefault().withPartial(true);
        var creationOpts = ParserCreationOptions.getDefault();
        var repeated_a = Parsewisp.parser("""
                        S = 'a' 0*6 'a'
                        """,
                creationOpts);
        //System.out.println(repeated_a);
        Assertions.assertEquals(treesPartial, new HashSet<>(repeated_a.parses(text, partialOpts)));
    }

    @Test
    void parseFullWithPartial() {
        var text = "aaaaaa";
        var treesPartial = Set.of(
                PT.create("S", "a"),
                PT.create("S", "a", "a"),
                PT.create("S", "a", "a", "a"),
                PT.create("S", "a", "a", "a", "a"),
                PT.create("S", "a", "a", "a", "a", "a"),
                PT.create("S", "a", "a", "a", "a", "a", "a")
        );
        var partialOpts = ParsingOptions.getDefault().withPartial(true);
        var creationOpts = ParserCreationOptions.getDefault();
        var repeated_a = Parsewisp.parser("""
                        S = 1*6 'a'
                        """,
                creationOpts);
        //System.out.println(repeated_a);
        Assertions.assertEquals(treesPartial, new HashSet<>(repeated_a.parses(text, partialOpts)));
    }
    @Test
    void parseRepetitionMinMax() {
            final @NotNull var p = Parsewisp.parser("S = 2*4 'a'");
            Assertions.assertTrue(p.parse("").isFailure());
            Assertions.assertTrue(p.parse("a").isFailure());
            Assertions.assertEquals(PT.create("S", "a", "a"), p.parse("aa"));
            Assertions.assertEquals(PT.create("S", "a", "a", "a"), p.parse("aaa"));
            Assertions.assertEquals(PT.create("S", "a", "a", "a", "a"), p.parse("aaaa"));
            Assertions.assertTrue(p.parse("aaaaa").isFailure());
    }
    @Test
    void createRepetitionParserFailure() {
        // Negative minimum
        Assertions.assertThrows(ParserCreationFailure.class, () -> Parsewisp.parser("S = -1*2 'a'"));
        // Negative maximum
        Assertions.assertThrows(ParserCreationFailure.class, () -> Parsewisp.parser("S = *-1 'a'"));
        // Negative minimum
        Assertions.assertThrows(ParserCreationFailure.class, () -> Parsewisp.parser("S = -1* 'a'"));
        // Negative exact
        Assertions.assertThrows(ParserCreationFailure.class, () -> Parsewisp.parser("S = -1 'a'"));
        // Minimum greater than maximum
        Assertions.assertThrows(ParserCreationFailure.class, () -> Parsewisp.parser("S = 4*2 'a'"));
    }
}
