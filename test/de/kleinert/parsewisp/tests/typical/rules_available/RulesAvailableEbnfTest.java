package de.kleinert.parsewisp.tests.typical.rules_available;

import de.kleinert.parsewisp.parser_options.ParserCreationOptions;
import org.junit.jupiter.api.Test;

import java.util.Set;

class RulesAvailableEbnfTest {
    /**
     * @see ParserCreationOptions#ebnf()
     */
    private final ParserCreationOptions opts = ParserCreationOptions.ebnf();

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
    void epsilon() {
        RulesAvailableGeneralizedTests.epsilon(opts, Set.of("ε"));
    }

    @Test
    void exclusion() {
        RulesAvailableGeneralizedTests.exclusion(opts, true);
    }

    @Test
    void explicitStringCaseSensitivity() {
        RulesAvailableGeneralizedTests.explicitStringCaseSensitivity(opts, false);
    }

    @Test
    void extendedIdentifiers() {
        RulesAvailableGeneralizedTests.extendedIdentifiers(opts, false);
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
        RulesAvailableGeneralizedTests.orderedChoice(opts, false);
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
        RulesAvailableGeneralizedTests.valueRange(opts, false);
    }

    @Test
    void variableRepetition() {
        RulesAvailableGeneralizedTests.variableRepetition(opts, false);
    }
}
