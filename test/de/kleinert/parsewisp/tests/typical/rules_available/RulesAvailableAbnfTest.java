package de.kleinert.parsewisp.tests.typical.rules_available;

import de.kleinert.parsewisp.parser_options.ParserCreationOptions;
import org.junit.jupiter.api.Test;

import java.util.Set;

class RulesAvailableAbnfTest {
    /**
     * @see ParserCreationOptions#ebnf()
     */
    private final ParserCreationOptions opts = ParserCreationOptions.abnf();

    /* Section: The actual tests. */

    @Test
    void abnfCore() {
        RulesAvailableGeneralizedTests.abnfCore(opts, true);
    }

    @Test
    void alternation() {
        RulesAvailableGeneralizedTests.alternation(opts, false);
    }

    @Test
    void epsilon() {
        RulesAvailableGeneralizedTests.epsilon(opts, Set.of("ε"));
    }

    @Test
    void exclusion() {
        RulesAvailableGeneralizedTests.exclusion(opts, false);
    }

    @Test
    void explicitStringCaseSensitivity() {
        RulesAvailableGeneralizedTests.explicitStringCaseSensitivity(opts, true);
    }

    @Test
    void extendedIdentifiers() {
        RulesAvailableGeneralizedTests.extendedIdentifiers(opts, false);
    }

    @Test
    void lookahead() {
        RulesAvailableGeneralizedTests.lookahead(opts, false);
    }

    @Test
    void negativeLookahead() {
        RulesAvailableGeneralizedTests.negativeLookahead(opts, false);
    }

    @Test
    void optional() {
        RulesAvailableGeneralizedTests.optional(opts, true);
    }

    @Test
    void optionalQuery() {
        RulesAvailableGeneralizedTests.optionalQuery(opts, false);
    }

    @Test
    void optionalRepetition() {
        RulesAvailableGeneralizedTests.optionalRepetition(opts, false);
    }

    @Test
    void optionalRepetitionStar() {
        RulesAvailableGeneralizedTests.optionalRepetitionStar(opts, false);
    }

    @Test
    void orderedChoice() {
        RulesAvailableGeneralizedTests.orderedChoice(opts, true);
    }

    @Test
    void plus() {
        RulesAvailableGeneralizedTests.plus(opts, false);
    }

    @Test
    void regex() {
        RulesAvailableGeneralizedTests.regex(opts, true);
    }

    @Test
    void singleQuotesForStringTerminals() {
        RulesAvailableGeneralizedTests.singleQuotesForStringTerminals(opts, false);
    }

    @Test
    void valueRange() {
        RulesAvailableGeneralizedTests.valueRange(opts, true);
    }

    @Test
    void variableRepetition() {
        RulesAvailableGeneralizedTests.variableRepetition(opts, true);
    }

    @Test void semicolonAsComment() {
        //RulesAvailableGeneralizedTests.semicolonLineComment(opts, true);
    }
}

