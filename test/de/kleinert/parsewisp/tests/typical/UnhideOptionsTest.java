package de.kleinert.parsewisp.tests.typical;

import de.kleinert.parsewisp.Parsewisp;
import de.kleinert.parsewisp.parser.Parser;
import de.kleinert.parsewisp.parser_options.ParsingOptions;
import de.kleinert.parsewisp.parser_options.Unhide;
import de.kleinert.parsewisp.testutil.PT;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class UnhideOptionsTest {

    @Test
    void testUnhide1() {
        final @NotNull Parser p = Parsewisp.parser("""
                S   = A <B>
                <A> = 'a'
                B   = 'b'
                """);
        var text = "ab";

        Assertions.assertEquals(
                PT.create("S", "a"),
                p.parse(text));
        Assertions.assertEquals(
                PT.create("S", "a", PT.create("B", "b")),
                p.parse(text, ParsingOptions.getDefault().withUnhide(Unhide.UnhideOptions.CONTENT)));
        Assertions.assertEquals(
                PT.create("S", PT.create("A", "a")),
                p.parse(text, ParsingOptions.getDefault().withUnhide(Unhide.UnhideOptions.TAGS)));
        Assertions.assertEquals(
                PT.create("S", PT.create("A", "a"), PT.create("B", "b")),
                p.parse(text, ParsingOptions.getDefault().withUnhide(Unhide.UnhideOptions.ALL)));
    }
}
