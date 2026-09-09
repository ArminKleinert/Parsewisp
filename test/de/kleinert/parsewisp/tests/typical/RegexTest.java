package de.kleinert.parsewisp.tests.typical;

import de.kleinert.parsewisp.Parsewisp;
import de.kleinert.parsewisp.error.ParserCreationFailure;
import de.kleinert.parsewisp.parser_options.ParserCreationOptions;
import de.kleinert.parsewisp.parser_options.RulesAvailable;
import de.kleinert.parsewisp.testutil.PT;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Set;

class RegexTest {
    @Test
    void testNum() {
        var ruleTypes = Set.of(RulesAvailable.REGEX);
        var opts = ParserCreationOptions.getDefault().withRulesAvailable(ruleTypes);
        var p = Parsewisp.parser("S = #\"[a-fA-F0-9]+\"", opts);
        Assertions.assertEquals(
                PT.create("S", "7F"),
                p.parse("7F")
        );
    }

    @Test
    void testInvalid() {
        var ruleTypes = Set.<RulesAvailable>of();
        var opts = ParserCreationOptions.getDefault().withRulesAvailable(ruleTypes);
        Assertions.assertThrows(
                ParserCreationFailure.class,
                () -> Parsewisp.parser("S = #\"[a-fA-F0-9]+\"", opts));
    }
}
