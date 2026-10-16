package de.kleinert.parsewisp.tests.obnoxious_tests;

import de.kleinert.parsewisp.Parsewisp;
import de.kleinert.parsewisp.result.ParsesResult;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.rules.Timeout;

/**
 * Test cases for especially obnoxious test cases.
 */
public class ObnoxiousTestCase {
    /**
     * Do I really need to document default constructors for test cases?
     */
    public ObnoxiousTestCase() {
    }

    /**
     * Timeout for all test cases in this class.
     */
    @Rule
    public Timeout timeout = Timeout.millis(5000);

    /**
     * <pre>
     *     Mode: All parses.
     *     Grammar: {@code S = S | epsilon}
     *     Text: {@code ""}
     *     Expect: I don't know.
     *     Status: Timeout.
     *     Note: This is technically correct behavior, but...
     * </pre>
     */
    @Test
    public void infiniteEpsilon() {
        var p = Parsewisp.parser("S = S | epsilon");
        Assertions.assertDoesNotThrow(() -> p.parses("").size());
    }

    /**
     * <pre>
     *     Mode: All parses.
     *     Grammar: {@code S = { A | B }\nA = 'a'\nB = 'a'}
     *     Text: {@code "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"} (30 times "a")
     *     Expect: Be able to generate at least 3 results
     *     Status: Timeout. Heap exhaustion if no timeout in place.
     *     Note: The cause here is in the parsing algorithm, not in the {@link ParsesResult.LazyResultList}.
     * </pre>
     */
    @Test
    public void streamOnVeryBigResultList() {
        final var p = Parsewisp.parser("S = { A | B }\nA = 'a'\nB = 'a'");
        var res = p.parses("a".repeat(30));
        var iter = res.iterator();
        System.out.println(iter.hasNext());
        System.out.println(iter.next());
        System.out.println(iter.hasNext());
        System.out.println(iter.next()); // << Still works here
        System.out.println(iter.hasNext()); // << Gives up
    }
}
