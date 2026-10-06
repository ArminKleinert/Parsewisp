package de.kleinert.parsewisp.tests.typical;

import de.kleinert.parsewisp.Parsewisp;
import de.kleinert.parsewisp.testutil.PT;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

class CornerCasesTests {
    /**
     * The grammar {@code S = E+ ; E = epsilon ;} applied to the empty string used to result in the parse tree {@code [:S]}, even though {@code [:S [:E]]} was expected.
     * The reason is that repetitions (once or more, zero or more, ...) finish when one of the results has length 0.
     * This test case ensures that the behavior is handled correctly.
     */
    @Test
    void repetitionsDoNotSwallowEmptyProductions() {
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
}
