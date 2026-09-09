package de.kleinert.parsewisp.tests.typical.rules_available;

import de.kleinert.parsewisp.Parsewisp;
import de.kleinert.parsewisp.error.IllegalGrammarException;
import de.kleinert.parsewisp.error.ParserCreationFailure;
import de.kleinert.parsewisp.parser.Parser;
import de.kleinert.parsewisp.parser_options.ParserCreationOptions;
import de.kleinert.parsewisp.parser_options.RulesAvailable;
import de.kleinert.parsewisp.testutil.PT;
import org.junit.jupiter.api.Assertions;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

class RulesAvailableGeneralizedTests {
    static void abnfCore(ParserCreationOptions opts, boolean expectedAvailability) {
        abnfCoreAvailable(opts, expectedAvailability);
        abnfCoreUnavailable(opts, !expectedAvailability);
        Assertions.assertEquals(opts.usableRules().contains(RulesAvailable.ABNF_CORE), expectedAvailability);
    }

    static void alternation(ParserCreationOptions opts, boolean expectedAvailability) {
        alternationAvailable(opts, expectedAvailability);
        alternationUnavailable(opts, !expectedAvailability);
        Assertions.assertEquals(opts.usableRules().contains(RulesAvailable.ALTERNATION), expectedAvailability);
    }

    static void epsilon(ParserCreationOptions opts, Collection<String> epsilonNames) {
        Assertions.assertEquals(new HashSet<>(opts.epsilonNames()), new HashSet<>(epsilonNames));
    }

    static void exclusion(ParserCreationOptions opts, boolean expectedAvailability) {
        exclusionAvailable(opts, expectedAvailability);
        exclusionUnavailable(opts, !expectedAvailability);
        Assertions.assertEquals(opts.usableRules().contains(RulesAvailable.EXCLUSION), expectedAvailability);
    }

    static void explicitStringCaseSensitivity(ParserCreationOptions opts, boolean expectedAvailability) {
        explicitStringCaseSensitivityAvailable(opts, expectedAvailability);
        explicitStringCaseSensitivityUnavailable(opts, !expectedAvailability);
        Assertions.assertEquals(opts.usableRules().contains(RulesAvailable.STRING_CASE_SENSITIVITY_PREFIX), expectedAvailability);
    }

    static void extendedIdentifiers(ParserCreationOptions opts, boolean expectedAvailability) {
        extendedIdentifiersAvailable(opts, expectedAvailability);
        extendedIdentifiersUnavailable(opts, !expectedAvailability);
        Assertions.assertEquals(opts.usableRules().contains(RulesAvailable.EXTENDED_IDENTIFIERS), expectedAvailability);
    }

    static void lookahead(ParserCreationOptions opts, boolean expectedAvailability) {
        lookaheadAvailable(opts, expectedAvailability);
        lookaheadUnavailable(opts, !expectedAvailability);
        Assertions.assertEquals(opts.usableRules().contains(RulesAvailable.LOOKAHEAD), expectedAvailability);
    }

    static void negativeLookahead(ParserCreationOptions opts, boolean expectedAvailability) {
        negativeLookaheadAvailable(opts, expectedAvailability);
        negativeLookaheadUnavailable(opts, !expectedAvailability);
        Assertions.assertEquals(opts.usableRules().contains(RulesAvailable.NEGATIVE_LOOKAHEAD), expectedAvailability);
    }

    static void optional(ParserCreationOptions opts, boolean expectedAvailability) {
        optionalAvailable(opts, expectedAvailability);
        optionalUnavailable(opts, !expectedAvailability);
        Assertions.assertEquals(opts.usableRules().contains(RulesAvailable.OPTIONAL), expectedAvailability);
    }

    static void optionalQuery(ParserCreationOptions opts, boolean expectedAvailability) {
        optionalQueryAvailable(opts, expectedAvailability);
        optionalQueryUnavailable(opts, !expectedAvailability);
        Assertions.assertEquals(opts.usableRules().contains(RulesAvailable.OPTIONAL_QUERY), expectedAvailability);
    }

    static void optionalRepetition(ParserCreationOptions opts, boolean expectedAvailability) {
        optionalRepetitionAvailable(opts, expectedAvailability);
        optionalRepetitionUnavailable(opts, !expectedAvailability);
        Assertions.assertEquals(opts.usableRules().contains(RulesAvailable.OPTIONAL_REPETITION), expectedAvailability);
    }

    static void optionalRepetitionStar(ParserCreationOptions opts, boolean expectedAvailability) {
        optionalRepetitionStarAvailable(opts, expectedAvailability);
        optionalRepetitionStarUnavailable(opts, !expectedAvailability);
        Assertions.assertEquals(opts.usableRules().contains(RulesAvailable.OPTIONAL_REPETITION_STAR), expectedAvailability);
    }

    static void orderedChoice(ParserCreationOptions opts, boolean expectedAvailability) {
        orderedChoiceAvailable(opts, expectedAvailability);
        orderedChoiceUnavailable(opts, !expectedAvailability);
        Assertions.assertEquals(opts.usableRules().contains(RulesAvailable.ORDERED_CHOICE), expectedAvailability);
    }

    static void plus(ParserCreationOptions opts, boolean expectedAvailability) {
        plusAvailable(opts, expectedAvailability);
        plusUnavailable(opts, !expectedAvailability);
        Assertions.assertEquals(opts.usableRules().contains(RulesAvailable.PLUS), expectedAvailability);
    }

    static void regex(ParserCreationOptions opts, boolean expectedAvailability) {
        regexAvailable(opts, expectedAvailability);
        regexUnavailable(opts, !expectedAvailability);
        Assertions.assertEquals(opts.usableRules().contains(RulesAvailable.REGEX), expectedAvailability);
    }

    static void singleQuotesForStringTerminals(ParserCreationOptions opts, boolean expectedAvailability) {
        singleQuotesForStringTerminalsAvailable(opts, expectedAvailability);
        singleQuotesForStringTerminalsUnavailable(opts, !expectedAvailability);
        Assertions.assertEquals(opts.usableRules().contains(RulesAvailable.SINGLY_QUOTED), expectedAvailability);
    }

    static void valueRange(ParserCreationOptions opts, boolean expectedAvailability) {
        valueRangeAvailable(opts, expectedAvailability);
        valueRangeUnavailable(opts, !expectedAvailability);
        Assertions.assertEquals(opts.usableRules().contains(RulesAvailable.VALUE_RANGE), expectedAvailability);
    }

    static void variableRepetition(ParserCreationOptions opts, boolean expectedAvailability) {
        variableRepetitionAvailable(opts, expectedAvailability);
        variableRepetitionUnavailable(opts, !expectedAvailability);
        Assertions.assertEquals(opts.usableRules().contains(RulesAvailable.VARIABLE_REPEAT), expectedAvailability);
    }

//    static void semicolonLineComment(ParserCreationOptions opts, boolean expectedAvailability) {
//        semicolonLineComment1(opts, expectedAvailability);
//        semicolonLineComment1(opts, !expectedAvailability);
//        //Assertions.assertEquals(opts.usableRules().contains(RulesAvailable.SEMICOLON_AS_LINECOMMENT), expectedAvailability);
//    }

    // Concrete tests start here.

    private static Parser AlphaParser(String gr, ParserCreationOptions opts) {
        return Parsewisp.parser(gr.replace("=", opts.ruleDefinitionOpts().iterator().next()), opts);
    }

    private static void alternationAvailable(ParserCreationOptions opts, boolean run) {
        if (!run) return;


        Assertions.assertDoesNotThrow(() -> AlphaParser("S = \"a\" | \"b\"", opts));

        Assertions.assertEquals(
                Set.of(PT.create("S", PT.create("A", "a")),
                        PT.create("S", PT.create("B", "a"))),
                new HashSet<>(AlphaParser("S = A | B\nA = \"a\"\nB = \"a\"", opts).parses("a"))
        );
    }

    private static void alternationUnavailable(ParserCreationOptions opts, boolean run) {
        if (!run) return;
        Assertions.assertThrows(ParserCreationFailure.class, () -> AlphaParser("S = \"a\" | \"b\"", opts));
    }

    private static void explicitStringCaseSensitivityAvailable(ParserCreationOptions opts, boolean run) {
        if (!run) return;

        Assertions.assertDoesNotThrow(() -> AlphaParser("S = %i\"a\"", opts));
        Assertions.assertDoesNotThrow(() -> AlphaParser("S = %s\"a\"", opts));

        var parserI = AlphaParser("S = %i\"a\"", opts);
        Assertions.assertEquals(PT.create("S", "a"), parserI.parse("a"));
        Assertions.assertEquals(PT.create("S", "a"), parserI.parse("A"));

        var parserS = AlphaParser("S = %s\"a\"", opts);
        Assertions.assertEquals(PT.create("S", "a"), parserS.parse("a"));
        Assertions.assertTrue(parserS.parse("A").isFailure());
    }

    private static void explicitStringCaseSensitivityUnavailable(ParserCreationOptions opts, boolean run) {
        if (!run) return;
        Assertions.assertThrows(ParserCreationFailure.class, () -> AlphaParser("S = %i\"a\"", opts));
        Assertions.assertThrows(ParserCreationFailure.class, () -> AlphaParser("S = %s\"a\"", opts));
    }

    private static void extendedIdentifiersAvailable(ParserCreationOptions opts, boolean run) {
        if (!run) return;
        Assertions.assertDoesNotThrow(() -> AlphaParser("S = \"a\"", opts));
        Assertions.assertDoesNotThrow(() -> AlphaParser("\uD83C\uDF81 = \"a\"", opts));
    }

    private static void extendedIdentifiersUnavailable(ParserCreationOptions opts, boolean run) {
        if (!run) return;
        Assertions.assertDoesNotThrow(() -> AlphaParser("S = \"a\"", opts));
        Assertions.assertThrows(ParserCreationFailure.class, () -> AlphaParser("\uD83C\uDF81 = \"a\"", opts));
    }

    private static void lookaheadAvailable(ParserCreationOptions opts, boolean run) {
        if (!run) return;

        Assertions.assertDoesNotThrow(() -> AlphaParser("S = &\"a\" \"a\"", opts));

        Assertions.assertEquals(
                PT.create("S", "a"),
                AlphaParser("S = &\"a\" \"a\"", opts).parse("a"));
        Assertions.assertTrue(
                AlphaParser("S = &\"b\" \"a\"", opts).parse("a").isFailure());
    }

    private static void lookaheadUnavailable(ParserCreationOptions opts, boolean run) {
        if (!run) return;
        Assertions.assertThrows(ParserCreationFailure.class, () -> AlphaParser("S = &\"a\" \"a\"", opts));
    }

    private static void negativeLookaheadAvailable(ParserCreationOptions opts, boolean run) {
        if (!run) return;

        Assertions.assertDoesNotThrow(() -> AlphaParser("S = !\"b\" \"a\"", opts));

        Assertions.assertEquals(
                PT.create("S", "a"),
                AlphaParser("S = !\"b\" \"a\"", opts).parse("a"));
        Assertions.assertTrue(
                AlphaParser("S = !\"a\" \"a\"", opts).parse("a").isFailure());
    }

    private static void negativeLookaheadUnavailable(ParserCreationOptions opts, boolean run) {
        if (!run) return;
        Assertions.assertThrows(ParserCreationFailure.class, () -> AlphaParser("S = !\"b\" \"a\"", opts));
    }

    private static void optionalAvailable(ParserCreationOptions opts, boolean run) {
        if (!run) return;

        Assertions.assertDoesNotThrow(() -> AlphaParser("S = [\"a\"]", opts));

        var p = AlphaParser("S = [\"a\"]", opts);
        Assertions.assertEquals(PT.create("S"), p.parse(""));
        Assertions.assertEquals(PT.create("S", "a"), p.parse("a"));
    }

    private static void optionalUnavailable(ParserCreationOptions opts, boolean run) {
        if (!run) return;
        Assertions.assertThrows(ParserCreationFailure.class, () -> AlphaParser("S = [\"a\"]", opts));
    }

    private static void optionalQueryAvailable(ParserCreationOptions opts, boolean run) {
        if (!run) return;

        Assertions.assertDoesNotThrow(() -> AlphaParser("S = \"a\"?", opts));

        var p = AlphaParser("S = \"a\"?", opts);
        Assertions.assertEquals(PT.create("S"), p.parse(""));
        Assertions.assertEquals(PT.create("S", "a"), p.parse("a"));
    }

    private static void optionalQueryUnavailable(ParserCreationOptions opts, boolean run) {
        if (!run) return;
        Assertions.assertThrows(ParserCreationFailure.class, () -> AlphaParser("S = \"a\"?", opts));
    }

    private static void optionalRepetitionAvailable(ParserCreationOptions opts, boolean run) {
        if (!run) return;

        Assertions.assertDoesNotThrow(() -> AlphaParser("S = {\"a\"}", opts));

        var p = AlphaParser("S = {\"a\"}", opts);
        Assertions.assertEquals(PT.create("S"), p.parse(""));
        Assertions.assertEquals(PT.create("S", "a"), p.parse("a"));
        Assertions.assertEquals(PT.create("S", "a", "a"), p.parse("aa"));
    }

    private static void optionalRepetitionUnavailable(ParserCreationOptions opts, boolean run) {
        if (!run) return;
        Assertions.assertThrows(ParserCreationFailure.class, () -> AlphaParser("S = {\"a\"}", opts));
    }

    private static void optionalRepetitionStarAvailable(ParserCreationOptions opts, boolean run) {
        if (!run) return;

        Assertions.assertDoesNotThrow(() -> AlphaParser("S = \"a\"*", opts));

        var p = AlphaParser("S = \"a\"*", opts);
        Assertions.assertEquals(PT.create("S"), p.parse(""));
        Assertions.assertEquals(PT.create("S", "a"), p.parse("a"));
        Assertions.assertEquals(PT.create("S", "a", "a"), p.parse("aa"));
    }

    private static void optionalRepetitionStarUnavailable(ParserCreationOptions opts, boolean run) {
        if (!run) return;
        Assertions.assertThrows(ParserCreationFailure.class, () -> AlphaParser("S = \"a\"*", opts));
    }

    private static void orderedChoiceAvailable(ParserCreationOptions opts, boolean run) {
        if (!run) return;

        Assertions.assertDoesNotThrow(() -> AlphaParser("S = \"a\" / \"b\"", opts));

        Assertions.assertEquals(
                Set.of(PT.create("S", PT.create("A", "a")),
                        PT.create("S", PT.create("B", "a"))),
                new HashSet<>(AlphaParser("S = A / B\nA = \"a\"\nB = \"a\"", opts).parses("a"))
        );
    }

    private static void orderedChoiceUnavailable(ParserCreationOptions opts, boolean run) {
        if (!run) return;
        Assertions.assertThrows(ParserCreationFailure.class, () -> AlphaParser("S = \"a\" / \"b\"", opts));
    }

    private static void plusAvailable(ParserCreationOptions opts, boolean run) {
        if (!run) return;

        Assertions.assertDoesNotThrow(() -> AlphaParser("S = \"a\"+", opts));

        var p = AlphaParser("S = \"a\"+", opts);
        Assertions.assertTrue(p.parse("").isFailure());
        Assertions.assertEquals(PT.create("S", "a"), p.parse("a"));
        Assertions.assertEquals(PT.create("S", "a", "a"), p.parse("aa"));
    }

    private static void plusUnavailable(ParserCreationOptions opts, boolean run) {
        if (!run) return;
        Assertions.assertThrows(ParserCreationFailure.class, () -> AlphaParser("S = \"a\"+", opts));
    }

    private static void regexAvailable(ParserCreationOptions opts, boolean run) {
        if (!run) return;

        Assertions.assertDoesNotThrow(() -> AlphaParser("S = #\"a\"", opts));
        Assertions.assertDoesNotThrow(() -> AlphaParser("S = #'a'", opts));

        var p = AlphaParser("S = #\"[0-9]\"", opts);
        Assertions.assertTrue(p.parse("").isFailure());
        Assertions.assertEquals(PT.create("S", "0"), p.parse("0"));
        Assertions.assertEquals(PT.create("S", "5"), p.parse("5"));
    }

    private static void regexUnavailable(ParserCreationOptions opts, boolean run) {
        if (!run) return;
        Assertions.assertThrows(ParserCreationFailure.class, () -> AlphaParser("S = #\"a\"", opts));
        Assertions.assertThrows(ParserCreationFailure.class, () -> AlphaParser("S = #'a'", opts));
    }

    private static void singleQuotesForStringTerminalsAvailable(ParserCreationOptions opts, boolean run) {
        if (!run) return;

        Assertions.assertDoesNotThrow(() -> AlphaParser("S = 'abc'", opts));

        Assertions.assertEquals(
                AlphaParser("S = \"abc\"", opts),
                AlphaParser("S = 'abc'", opts));
    }

    private static void singleQuotesForStringTerminalsUnavailable(ParserCreationOptions opts, boolean run) {
        if (!run) return;
        Assertions.assertThrows(ParserCreationFailure.class, () -> AlphaParser("S = 'abc'", opts));
    }

    private static void valueRangeAvailable(ParserCreationOptions opts, boolean run) {
        if (!run) return;

        Assertions.assertDoesNotThrow(() -> AlphaParser("S = %x41-5a", opts));
        Assertions.assertDoesNotThrow(() -> AlphaParser("S = %d65-90", opts));
        Assertions.assertDoesNotThrow(() -> AlphaParser("S = %b1000001-1011010", opts));

        var pHex = AlphaParser("S = %x41-5a", opts);
        var pDec = AlphaParser("S = %d65-90", opts);
        var pBin = AlphaParser("S = %b1000001-1011010", opts);
        Assertions.assertEquals(pHex, pDec);
        Assertions.assertEquals(pHex, pBin);
    }

    private static void valueRangeUnavailable(ParserCreationOptions opts, boolean run) {
        if (!run) return;
        Assertions.assertThrows(ParserCreationFailure.class, () -> AlphaParser("S = %x41-5a", opts));
        Assertions.assertThrows(ParserCreationFailure.class, () -> AlphaParser("S = %d65-90", opts));
        Assertions.assertThrows(ParserCreationFailure.class, () -> AlphaParser("S = %b1000001-1011010", opts));
    }

    private static void variableRepetitionAvailable(ParserCreationOptions opts, boolean run) {
        if (!run) return;

        Assertions.assertDoesNotThrow(() -> AlphaParser("S = 1*5 \"a\"", opts));
        Assertions.assertDoesNotThrow(() -> AlphaParser("S = 1* \"a\"", opts));
        Assertions.assertDoesNotThrow(() -> AlphaParser("S = *5 \"a\"", opts));

        var optsWithStar = opts.addAvailableRule(RulesAvailable.OPTIONAL_REPETITION_STAR);
        Assertions.assertThrows(ParserCreationFailure.class, () -> AlphaParser("S = * \"a\"", optsWithStar));

        var optsWithoutStar = opts.removeAvailableRule(RulesAvailable.OPTIONAL_REPETITION_STAR);
        Assertions.assertDoesNotThrow(() -> AlphaParser("S = * \"a\"", optsWithoutStar));

        var p = AlphaParser("S = 1*3 \"a\" \"b\"", opts);
        Assertions.assertTrue(p.parse("a").isFailure());
        Assertions.assertTrue(p.parse("b").isFailure());
        Assertions.assertEquals(PT.create("S", "a", "b"), p.parse("ab"));
        Assertions.assertEquals(PT.create("S", "a", "a", "b"), p.parse("aab"));
        Assertions.assertEquals(PT.create("S", "a", "a", "a", "b"), p.parse("aaab"));
        Assertions.assertTrue(p.parse("aaaab").isFailure());
    }

    private static void variableRepetitionUnavailable(ParserCreationOptions opts, boolean run) {
        if (!run) return;

        Assertions.assertThrows(ParserCreationFailure.class, () -> AlphaParser("S = 1*5 \"a\"", opts));
        Assertions.assertThrows(ParserCreationFailure.class, () -> AlphaParser("S = 1* \"a\"", opts));
        Assertions.assertThrows(ParserCreationFailure.class, () -> AlphaParser("S = *5 \"a\"", opts));
        Assertions.assertThrows(ParserCreationFailure.class, () -> AlphaParser("S = * \"a\"", opts));
    }

    private static void exclusionAvailable(ParserCreationOptions opts, boolean run) {
        if (!run) return;

        var optsAndRegex = opts.addAvailableRule(RulesAvailable.REGEX);

        Assertions.assertDoesNotThrow(() -> AlphaParser("S = #'[0-9]+' - '1'", optsAndRegex));
        Assertions.assertDoesNotThrow(() -> AlphaParser("S = #'[0-9]+' - ('1' | '11' | '111')", optsAndRegex));
        Assertions.assertDoesNotThrow(() -> AlphaParser("S = #'[0-9]+' - (#'[1]+' - '11')", optsAndRegex));

        var p = AlphaParser("S = #'[0-9]+' - '1'", optsAndRegex);
        Assertions.assertTrue(p.parse("1").isFailure());
        Assertions.assertEquals(PT.create("S", "11"), p.parse("11"));
        Assertions.assertEquals(PT.create("S", "1111"), p.parse("1111"));
        Assertions.assertEquals(PT.create("S", "2"), p.parse("2"));

        p = AlphaParser("S = #'[0-9]+' - ('1' | '11' | '111')", optsAndRegex);
        Assertions.assertTrue(p.parse("1").isFailure());
        Assertions.assertTrue(p.parse("11").isFailure());
        Assertions.assertEquals(PT.create("S", "1111"), p.parse("1111"));
        Assertions.assertEquals(PT.create("S", "2"), p.parse("2"));

        p = AlphaParser("S = #'[0-9]+' - (#'[1]+' - '11')", optsAndRegex);
        Assertions.assertTrue(p.parse("1").isFailure());
        Assertions.assertEquals(PT.create("S", "11"), p.parse("11"));
        Assertions.assertTrue(p.parse("1111").isFailure());
        Assertions.assertEquals(PT.create("S", "2"), p.parse("2"));
    }

    private static void exclusionUnavailable(ParserCreationOptions opts, boolean run) {
        if (!run) return;

        var optsAndRegex = opts.addAvailableRule(RulesAvailable.REGEX);
        Assertions.assertThrows(ParserCreationFailure.class, () ->
                AlphaParser("S = #'[0-9]+' - '1'", optsAndRegex));
        Assertions.assertThrows(ParserCreationFailure.class, () ->
                AlphaParser("S = #'[0-9]+' - ('1' | '11' | '111')", optsAndRegex));
        Assertions.assertThrows(ParserCreationFailure.class, () ->
                AlphaParser("S = #'[0-9]+' - (#'[1]+' - '11')", optsAndRegex));
    }

    private static void abnfCoreAvailable(ParserCreationOptions opts, boolean run) {
        if (!run) return;

        Assertions.assertDoesNotThrow(() -> AlphaParser(
                "S = ALPHA BIT CHAR CR CRLF CTL DIGIT DQUOTE HEXDIG HTAB LF LWSP OCTET SP VCHAR WSP",
                opts));

        var text = "a";
        Assertions.assertEquals(PT.create("S", PT.create("ALPHA", text)), AlphaParser("S = ALPHA", opts).parse(text));

        text = "0";
        Assertions.assertEquals(PT.create("S", PT.create("BIT", text)), AlphaParser("S = BIT", opts).parse(text));

        text = "b";
        Assertions.assertEquals(PT.create("S", PT.create("CHAR", text)), AlphaParser("S = CHAR", opts).parse(text));

        text = "\r";
        Assertions.assertEquals(PT.create("S", PT.create("CR", text)), AlphaParser("S = CR", opts).parse(text));

        text = "\r\n";
        Assertions.assertEquals(PT.create("S", PT.create("CRLF", text)), AlphaParser("S = CRLF", opts).parse(text));

        text = "\u0001";
        Assertions.assertEquals(PT.create("S", PT.create("CTL", text)), AlphaParser("S = CTL", opts).parse(text));

        text = "5";
        Assertions.assertEquals(PT.create("S", PT.create("DIGIT", text)), AlphaParser("S = DIGIT", opts).parse(text));

        text = "\"";
        Assertions.assertEquals(PT.create("S", PT.create("DQUOTE", text)), AlphaParser("S = DQUOTE", opts).parse(text));

        text = "F";
        Assertions.assertEquals(PT.create("S", PT.create("HEXDIG", text)), AlphaParser("S = HEXDIG", opts).parse(text));

        text = "\t";
        Assertions.assertEquals(PT.create("S", PT.create("HTAB", text)), AlphaParser("S = HTAB", opts).parse(text));

        text = "\n";
        Assertions.assertEquals(PT.create("S", PT.create("LF", text)), AlphaParser("S = LF", opts).parse(text));

        text = " ";
        Assertions.assertEquals(PT.create("S", PT.create("LWSP", text)), AlphaParser("S = LWSP", opts).parse(text));

        text = "A";
        Assertions.assertEquals(PT.create("S", PT.create("OCTET", text)), AlphaParser("S = OCTET", opts).parse(text));

        text = " ";
        Assertions.assertEquals(PT.create("S", PT.create("SP", text)), AlphaParser("S = SP", opts).parse(text));

        text = "!";
        Assertions.assertEquals(PT.create("S", PT.create("VCHAR", text)), AlphaParser("S = VCHAR", opts).parse(text));

        text = " ";
        Assertions.assertEquals(PT.create("S", PT.create("WSP", text)), AlphaParser("S = WSP", opts).parse(text));
    }

    private static void abnfCoreUnavailable(ParserCreationOptions opts, boolean run) {
        if (!run) return;

        Assertions.assertThrows(IllegalGrammarException.class, () -> AlphaParser(
                "S = ALPHA BIT CHAR CR CRLF CTL DIGIT DQUOTE HEXDIG HTAB LF LWSP OCTET SP VCHAR WSP",
                opts));
    }


    private static
    void semicolonLineComment1(ParserCreationOptions opts, boolean run) {
        if (!run) return;

        Assertions.assertEquals(
                PT.create("S", "a"),
                AlphaParser("S = \"a\" ; ignore", opts).parse("a")
        );
        Assertions.assertEquals(
                PT.create("S",
                        PT.create("A", "a")),
                AlphaParser(""" 
                        S = A ; ignore
                        A = "a"
                        """, opts)
                        .parse("a")
        );
    }
}
