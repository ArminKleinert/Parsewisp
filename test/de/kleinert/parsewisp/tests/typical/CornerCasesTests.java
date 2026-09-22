package de.kleinert.parsewisp.tests.typical;

import de.kleinert.parsewisp.Parsewisp;
import de.kleinert.parsewisp.testutil.PT;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CornerCasesTests {
    @Test
    void test10() {
        Assertions.assertEquals(
                PT.create("S", "a", PT.create("E"), "b"),
                Parsewisp.parser("S = 'a' E+ 'b'\nE = eps").parse("ab"));
        Assertions.assertEquals(
                PT.create("S", "a", PT.create("E")),
                Parsewisp.parser("S = 'a' E+\nE = eps").parse("a"));

        Assertions.assertEquals(
                PT.create("S", PT.create("E"), PT.create("E")),
                Parsewisp.parser("S = E+ E+\nE = eps").parse(""));
        Assertions.assertEquals(
                PT.create("S", PT.create("E")),
                Parsewisp.parser("S = E+\nE = eps").parse(""));
    }
}
