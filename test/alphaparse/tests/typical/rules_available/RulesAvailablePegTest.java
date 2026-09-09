//package alphaparse.tests.typical.rules_available;
//
//import alphaparse.parser_options.ParserCreationOptions;
//import org.junit.jupiter.api.Test;
//
//import java.util.Set;
//
//class RulesAvailablePegTest {
//    /**
//     * @see ParserCreationOptions#peg()
//     */
//    private final ParserCreationOptions opts = ParserCreationOptions.peg();
//
//    /* Section: The actual tests. */
//
//    @Test
//    void abnfCore() {
//        RulesAvailableGeneralizedTests.abnfCore(opts, false);
//    }
//
//    @Test
//    void alternation() {
//        RulesAvailableGeneralizedTests.alternation(opts, false);
//    }
//
//    @Test
//    void epsilon() {
//        RulesAvailableGeneralizedTests.epsilon(opts, Set.of("ε"));
//    }
//
//    @Test
//    void exclusion() {
//        RulesAvailableGeneralizedTests.exclusion(opts, false);
//    }
//
//    @Test
//    void explicitStringCaseSensitivity() {
//        RulesAvailableGeneralizedTests.explicitStringCaseSensitivity(opts, false);
//    }
//
//    @Test
//    void extendedIdentifiers() {
//        RulesAvailableGeneralizedTests.extendedIdentifiers(opts, false);
//    }
//
//    @Test
//    void lookahead() {
//        RulesAvailableGeneralizedTests.lookahead(opts, true);
//    }
//
//    @Test
//    void negativeLookahead() {
//        RulesAvailableGeneralizedTests.negativeLookahead(opts, true);
//    }
//
//    @Test
//    void optional() {
//        RulesAvailableGeneralizedTests.optional(opts, false);
//    }
//
//    @Test
//    void optionalQuery() {
//        RulesAvailableGeneralizedTests.optionalQuery(opts, true);
//    }
//
//    @Test
//    void optionalRepetition() {
//        RulesAvailableGeneralizedTests.optionalRepetition(opts, false);
//    }
//
//    @Test
//    void optionalRepetitionStar() {
//        RulesAvailableGeneralizedTests.optionalRepetitionStar(opts, true);
//    }
//
//    @Test
//    void orderedChoice() {
//        RulesAvailableGeneralizedTests.orderedChoice(opts, true);
//    }
//
//    @Test
//    void plus() {
//        RulesAvailableGeneralizedTests.plus(opts, true);
//    }
//
//    @Test
//    void regex() {
//        RulesAvailableGeneralizedTests.regex(opts, true);
//    }
//
//    @Test
//    void singleQuotesForStringTerminals() {
//        RulesAvailableGeneralizedTests.singleQuotesForStringTerminals(opts, true);
//    }
//
//    @Test
//    void valueRange() {
//        RulesAvailableGeneralizedTests.valueRange(opts, false);
//    }
//
//    @Test
//    void variableRepetition() {
//        RulesAvailableGeneralizedTests.variableRepetition(opts, false);
//    }
//}
