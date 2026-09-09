//package alphaparse.tests.typical.rules_available;
//
//import alphaparse.parser_options.ParserCreationOptions;
//import org.junit.jupiter.api.Test;
//
//import java.util.Set;
//
//class RulesAvailablePureEbnfTest {
//    /**
//     * @see ParserCreationOptions#pureEbnf()
//     */
//    private final ParserCreationOptions opts = ParserCreationOptions.pureEbnf();
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
//        RulesAvailableGeneralizedTests.alternation(opts, true);
//    }
//
//    @Test
//    void epsilon() {
//        RulesAvailableGeneralizedTests.epsilon(opts, Set.of("ε"));
//    }
//
//    @Test
//    void exclusion() {
//        RulesAvailableGeneralizedTests.exclusion(opts, true);
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
//        RulesAvailableGeneralizedTests.lookahead(opts, false);
//    }
//
//    @Test
//    void negativeLookahead() {
//        RulesAvailableGeneralizedTests.negativeLookahead(opts, false);
//    }
//
//    @Test
//    void optional() {
//        RulesAvailableGeneralizedTests.optional(opts, true);
//    }
//
//    @Test
//    void optionalQuery() {
//        RulesAvailableGeneralizedTests.optionalQuery(opts, false);
//    }
//
//    @Test
//    void optionalRepetition() {
//        RulesAvailableGeneralizedTests.optionalRepetition(opts, true);
//    }
//
//    @Test
//    void optionalRepetitionStar() {
//        RulesAvailableGeneralizedTests.optionalRepetitionStar(opts, false);
//    }
//
//    @Test
//    void orderedChoice() {
//        RulesAvailableGeneralizedTests.orderedChoice(opts, false);
//    }
//
//    @Test
//    void plus() {
//        RulesAvailableGeneralizedTests.plus(opts, false);
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
