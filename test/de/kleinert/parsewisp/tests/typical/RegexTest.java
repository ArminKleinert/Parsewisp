package de.kleinert.parsewisp.tests.typical;

import de.kleinert.parsewisp.Parsewisp;
import de.kleinert.parsewisp.parser_options.ParserCreationOptions;
import de.kleinert.parsewisp.testutil.PT;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RegexTest {
    @Test
    void testNum() {
        var opts = ParserCreationOptions.getDefault();
        var p = Parsewisp.parser("S = #\"[a-fA-F0-9]+\"", opts);
        Assertions.assertEquals(
                PT.create("S", "7F"),
                p.parse("7F")
        );
    }

    @Test
    void testUnicodeSequence() {
        // This used to cause a bug where "\\u" consumed one character too far.
        // E.g. "\\u0041-" would consume the minus as if it was part of the number sequence.
        var p = Parsewisp.parser("S = #'[\\u0041-\\u005A]+'");
        Assertions.assertEquals(PT.create("S", "ABC"), p.parse("ABC"));
    }
}
