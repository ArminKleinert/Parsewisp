package de.kleinert.parsewisp.tests.typical.rules_available;

import de.kleinert.parsewisp.parser_options.ParserCreationOptions;
import org.junit.jupiter.api.Test;

import java.util.Set;

class RulesAvailableDefaultTest {
    /**
     * @see ParserCreationOptions#getDefault()
     */
    private final ParserCreationOptions opts = ParserCreationOptions.getDefault();

    /* Section: The actual tests. */

    @Test
    void abnfCore() {
        RulesAvailableGeneralizedTests.abnfCore(opts, false);
    }

    @Test
    void alternation() {
        RulesAvailableGeneralizedTests.alternation(opts, true);
    }

    @Test
    void exclusion() {
        RulesAvailableGeneralizedTests.exclusion(opts, true);
    }

    @Test
    void explicitStringCaseSensitivity() {
        RulesAvailableGeneralizedTests.explicitStringCaseSensitivity(opts, true);
    }

    @Test
    void extendedIdentifiers() {
        RulesAvailableGeneralizedTests.extendedIdentifiers(opts, true);
    }

    @Test
    void lookahead() {
        RulesAvailableGeneralizedTests.lookahead(opts, true);
    }

    @Test
    void negativeLookahead() {
        RulesAvailableGeneralizedTests.negativeLookahead(opts, true);
    }

    @Test
    void optional() {
        RulesAvailableGeneralizedTests.optional(opts, true);
    }

    @Test
    void optionalQuery() {
        RulesAvailableGeneralizedTests.optionalQuery(opts, true);
    }

    @Test
    void optionalRepetition() {
        RulesAvailableGeneralizedTests.optionalRepetition(opts, true);
    }

    @Test
    void optionalRepetitionStar() {
        RulesAvailableGeneralizedTests.optionalRepetitionStar(opts, true);
    }

    @Test
    void orderedChoice() {
        RulesAvailableGeneralizedTests.orderedChoice(opts, true);
    }

    @Test
    void plus() {
        RulesAvailableGeneralizedTests.plus(opts, true);
    }

    @Test
    void regex() {
        RulesAvailableGeneralizedTests.regex(opts, true);
    }

    @Test
    void singleQuotesForStringTerminals() {
        RulesAvailableGeneralizedTests.singleQuotesForStringTerminals(opts, true);
    }

    @Test
    void valueRange() {
        RulesAvailableGeneralizedTests.valueRange(opts, true);
    }

    @Test
    void variableRepetition() {
        RulesAvailableGeneralizedTests.variableRepetition(opts, true);
    }
}
