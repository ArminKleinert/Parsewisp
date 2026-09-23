package de.kleinert.parsewisp.tests.typical;

import de.kleinert.parsewisp.Parsewisp;
import de.kleinert.parsewisp.testutil.PT;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

class CornerCasesTests {
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

    @Test
    void test11() {
        Assertions.assertEquals(
                PT.create("S", "a", PT.create("E"), "b"),
                Parsewisp.parser("S = 'a' 1*3 E 'b'\nE = eps").parse("ab"));
        Assertions.assertEquals(
                PT.create("S", "a", PT.create("E"), PT.create("E"), "b"),
                Parsewisp.parser("S = 'a' 2*3 E 'b'\nE = eps").parse("ab"));

        Assertions.assertEquals(
                Set.of(PT.create("S", "a", PT.create("E"), "b"),
                        PT.create("S", "a", PT.create("E"), PT.create("E"), "b"),
                        PT.create("S", "a", PT.create("E"), PT.create("E"), PT.create("E"), "b")),
                new HashSet<>(Parsewisp.parser("S = 'a' 1*3 E 'b'\nE = eps").parses("ab")));
    }

    @Test
    void test12() {var p = Parsewisp.parser("S = 2*4 E 'a'\nE = eps");
        System.out.println(p.parse("a"));
    }
}
