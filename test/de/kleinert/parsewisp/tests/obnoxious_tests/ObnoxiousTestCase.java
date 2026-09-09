package de.kleinert.parsewisp.tests.obnoxious_tests;

import de.kleinert.parsewisp.Parsewisp;
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
    public Timeout timeout = Timeout.millis(1000);

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
}
