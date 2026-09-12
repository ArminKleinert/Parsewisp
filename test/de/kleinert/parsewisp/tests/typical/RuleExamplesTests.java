package de.kleinert.parsewisp.tests.typical;

import de.kleinert.parsewisp.Parsewisp;
import de.kleinert.parsewisp.parser_options.RedefinitionOption;
import de.kleinert.parsewisp.parser_options.ParserCreationOptions;
import de.kleinert.parsewisp.testutil.PT;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

class RuleExamplesTests {
    @Test
    void testChoiceExample1() {
        var p1 = Parsewisp.parser("S = 'a' | 'b' | 'ab'");
        Assertions.assertEquals(PT.create("S", "a"), p1.parse("a"));
        Assertions.assertEquals(PT.create("S", "b"), p1.parse("b"));
        Assertions.assertEquals(PT.create("S", "ab"), p1.parse("ab"));

        var opts = ParserCreationOptions
                .getDefault()
                .withRedefinitionOption(RedefinitionOption.CHOICE);
        var p2 = Parsewisp.parser("""
                S =  'a'
                S =/ 'b'
                S =/ 'ab'
                """, opts);
        Assertions.assertEquals(PT.create("S", "a"), p2.parse("a"));
        Assertions.assertEquals(PT.create("S", "b"), p2.parse("b"));
        Assertions.assertEquals(PT.create("S", "ab"), p2.parse("ab"));
    }
}
