package de.kleinert.parsewisp.tests.typical;

import de.kleinert.parsewisp.Parsewisp;
import de.kleinert.parsewisp.parser_options.ParserCreationOptions;
import de.kleinert.parsewisp.parser_options.RulesAvailable;
import de.kleinert.parsewisp.testutil.PT;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class StringTest {
    @Test
    void explicitStringCaseInsensitivity() {
        var opts = ParserCreationOptions.getDefault()
                .addAvailableRule(RulesAvailable.STRING_CASE_SENSITIVITY_PREFIX);
        Assertions.assertEquals(
                PT.create("S", "A"),
                Parsewisp.parser("S = %i\"A\"", opts).parse("A"));
        Assertions.assertEquals(
                PT.create("S", "A"),
                Parsewisp.parser("S = %i\"A\"", opts).parse("a"));
    }

    @Test
    void explicitStringCaseSensitivity() {
        var opts = ParserCreationOptions.getDefault()
                .addAvailableRule(RulesAvailable.STRING_CASE_SENSITIVITY_PREFIX);
        Assertions.assertEquals(
                PT.create("S", "A"),
                Parsewisp.parser("S = %s\"A\"", opts).parse("A"));
        Assertions.assertTrue(
                Parsewisp.parser("S = %s\"A\"", opts).parse("a").isFailure());
    }
}
