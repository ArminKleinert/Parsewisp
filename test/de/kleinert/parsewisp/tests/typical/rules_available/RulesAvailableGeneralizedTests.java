package de.kleinert.parsewisp.tests.typical.rules_available;

import de.kleinert.parsewisp.Parsewisp;
import de.kleinert.parsewisp.error.IllegalGrammarException;
import de.kleinert.parsewisp.error.ParserCreationFailure;
import de.kleinert.parsewisp.parser.Parser;
import de.kleinert.parsewisp.parser_options.ParserCreationOptions;
import de.kleinert.parsewisp.testutil.PT;
import org.junit.jupiter.api.Assertions;

import java.util.HashSet;
import java.util.Set;

class RulesAvailableGeneralizedTests {
    static void abnfCore(ParserCreationOptions opts, boolean expectedAvailability) {
        abnfCoreAvailable(opts, expectedAvailability);
        abnfCoreUnavailable(opts, !expectedAvailability);
    }

    static void alternation(ParserCreationOptions opts, boolean expectedAvailability) {
        alternationAvailable(opts, expectedAvailability);
        alternationUnavailable(opts, !expectedAvailability);
    }

    static void exclusion(ParserCreationOptions opts, boolean expectedAvailability) {
        exclusionAvailable(opts, expectedAvailability);
        exclusionUnavailable(opts, !expectedAvailability);
    }

    static void explicitStringCaseSensitivity(ParserCreationOptions opts, boolean expectedAvailability) {
        explicitStringCaseSensitivityAvailable(opts, expectedAvailability);
        explicitStringCaseSensitivityUnavailable(opts, !expectedAvailability);
    }

    static void extendedIdentifiers(ParserCreationOptions opts, boolean expectedAvailability) {
        extendedIdentifiersAvailable(opts, expectedAvailability);
        extendedIdentifiersUnavailable(opts, !expectedAvailability);
    }

    static void lookahead(ParserCreationOptions opts, boolean expectedAvailability) {
        lookaheadAvailable(opts, expectedAvailability);
        lookaheadUnavailable(opts, !expectedAvailability);
    }

    static void negativeLookahead(ParserCreationOptions opts, boolean expectedAvailability) {
        negativeLookaheadAvailable(opts, expectedAvailability);
        negativeLookaheadUnavailable(opts, !expectedAvailability);
    }

    static void optional(ParserCreationOptions opts, boolean expectedAvailability) {
        optionalAvailable(opts, expectedAvailability);
        optionalUnavailable(opts, !expectedAvailability);
    }

    static void optionalQuery(ParserCreationOptions opts, boolean expectedAvailability) {
        optionalQueryAvailable(opts, expectedAvailability);
        optionalQueryUnavailable(opts, !expectedAvailability);
    }

    static void optionalRepetition(ParserCreationOptions opts, boolean expectedAvailability) {
        optionalRepetitionAvailable(opts, expectedAvailability);
        optionalRepetitionUnavailable(opts, !expectedAvailability);
    }

    static void optionalRepetitionStar(ParserCreationOptions opts, boolean expectedAvailability) {
        optionalRepetitionStarAvailable(opts, expectedAvailability);
        optionalRepetitionStarUnavailable(opts, !expectedAvailability);
    }

    static void orderedChoice(ParserCreationOptions opts, boolean expectedAvailability) {
        orderedChoiceAvailable(opts, expectedAvailability);
        orderedChoiceUnavailable(opts, !expectedAvailability);
    }

    static void plus(ParserCreationOptions opts, boolean expectedAvailability) {
        plusAvailable(opts, expectedAvailability);
        plusUnavailable(opts, !expectedAvailability);
    }

    static void regex(ParserCreationOptions opts, boolean expectedAvailability) {
        regexAvailable(opts, expectedAvailability);
        regexUnavailable(opts, !expectedAvailability);
    }

    static void singleQuotesForStringTerminals(ParserCreationOptions opts, boolean expectedAvailability) {
        singleQuotesForStringTerminalsAvailable(opts, expectedAvailability);
        singleQuotesForStringTerminalsUnavailable(opts, !expectedAvailability);
    }

    static void valueRange(ParserCreationOptions opts, boolean expectedAvailability) {
        valueRangeAvailable(opts, expectedAvailability);
        valueRangeUnavailable(opts, !expectedAvailability);
    }

    static void variableRepetition(ParserCreationOptions opts, boolean expectedAvailability) {
        variableRepetitionAvailable(opts, expectedAvailability);
        variableRepetitionUnavailable(opts, !expectedAvailability);
    }

//    static void semicolonLineComment(ParserCreationOptions opts, boolean expectedAvailability) {
//        semicolonLineComment1(opts, expectedAvailability);
//        semicolonLineComment1(opts, !expectedAvailability);
//    }

    // Concrete tests start here.

    private static Parser ParsewispParser(String gr, ParserCreationOptions opts) {
        return Parsewisp.parser(gr, opts);
    }

    private static void alternationAvailable(ParserCreationOptions opts, boolean run) {
        if (!run) return;


        Assertions.assertDoesNotThrow(() -> ParsewispParser("S = \"a\" | \"b\"", opts));

        Assertions.assertEquals(
                Set.of(PT.create("S", PT.create("A", "a")),
                        PT.create("S", PT.create("B", "a"))),
                new HashSet<>(ParsewispParser("S = A | B\nA = \"a\"\nB = \"a\"", opts).parses("a"))
        );
    }

    private static void alternationUnavailable(ParserCreationOptions opts, boolean run) {
        if (!run) return;
        Assertions.assertThrows(ParserCreationFailure.class, () -> ParsewispParser("S = \"a\" | \"b\"", opts));
    }

    private static void explicitStringCaseSensitivityAvailable(ParserCreationOptions opts, boolean run) {
        if (!run) return;

        Assertions.assertDoesNotThrow(() -> ParsewispParser("S = %i\"a\"", opts));
        Assertions.assertDoesNotThrow(() -> ParsewispParser("S = %s\"a\"", opts));

        var parserI = ParsewispParser("S = %i\"a\"", opts);
        Assertions.assertEquals(PT.create("S", "a"), parserI.parse("a"));
        Assertions.assertEquals(PT.create("S", "a"), parserI.parse("A"));

        var parserS = ParsewispParser("S = %s\"a\"", opts);
        Assertions.assertEquals(PT.create("S", "a"), parserS.parse("a"));
        Assertions.assertTrue(parserS.parse("A").isFailure());
    }

    private static void explicitStringCaseSensitivityUnavailable(ParserCreationOptions opts, boolean run) {
        if (!run) return;
        Assertions.assertThrows(ParserCreationFailure.class, () -> ParsewispParser("S = %i\"a\"", opts));
        Assertions.assertThrows(ParserCreationFailure.class, () -> ParsewispParser("S = %s\"a\"", opts));
    }

    private static void extendedIdentifiersAvailable(ParserCreationOptions opts, boolean run) {
        if (!run) return;
        Assertions.assertDoesNotThrow(() -> ParsewispParser("S = \"a\"", opts));
        Assertions.assertDoesNotThrow(() -> ParsewispParser("\uD83C\uDF81 = \"a\"", opts));
    }

    private static void extendedIdentifiersUnavailable(ParserCreationOptions opts, boolean run) {
        if (!run) return;
        Assertions.assertDoesNotThrow(() -> ParsewispParser("S = \"a\"", opts));
        Assertions.assertThrows(ParserCreationFailure.class, () -> ParsewispParser("\uD83C\uDF81 = \"a\"", opts));
    }

    private static void lookaheadAvailable(ParserCreationOptions opts, boolean run) {
        if (!run) return;

        Assertions.assertDoesNotThrow(() -> ParsewispParser("S = &\"a\" \"a\"", opts));

        Assertions.assertEquals(
                PT.create("S", "a"),
                ParsewispParser("S = &\"a\" \"a\"", opts).parse("a"));
        Assertions.assertTrue(
                ParsewispParser("S = &\"b\" \"a\"", opts).parse("a").isFailure());
    }

    private static void lookaheadUnavailable(ParserCreationOptions opts, boolean run) {
        if (!run) return;
        Assertions.assertThrows(ParserCreationFailure.class, () -> ParsewispParser("S = &\"a\" \"a\"", opts));
    }

    private static void negativeLookaheadAvailable(ParserCreationOptions opts, boolean run) {
        if (!run) return;

        Assertions.assertDoesNotThrow(() -> ParsewispParser("S = !\"b\" \"a\"", opts));

        Assertions.assertEquals(
                PT.create("S", "a"),
                ParsewispParser("S = !\"b\" \"a\"", opts).parse("a"));
        Assertions.assertTrue(
                ParsewispParser("S = !\"a\" \"a\"", opts).parse("a").isFailure());
    }

    private static void negativeLookaheadUnavailable(ParserCreationOptions opts, boolean run) {
        if (!run) return;
        Assertions.assertThrows(ParserCreationFailure.class, () -> ParsewispParser("S = !\"b\" \"a\"", opts));
    }

    private static void optionalAvailable(ParserCreationOptions opts, boolean run) {
        if (!run) return;

        Assertions.assertDoesNotThrow(() -> ParsewispParser("S = [\"a\"]", opts));

        var p = ParsewispParser("S = [\"a\"]", opts);
        Assertions.assertEquals(PT.create("S"), p.parse(""));
        Assertions.assertEquals(PT.create("S", "a"), p.parse("a"));
    }

    private static void optionalUnavailable(ParserCreationOptions opts, boolean run) {
        if (!run) return;
        Assertions.assertThrows(ParserCreationFailure.class, () -> ParsewispParser("S = [\"a\"]", opts));
    }

    private static void optionalQueryAvailable(ParserCreationOptions opts, boolean run) {
        if (!run) return;

        Assertions.assertDoesNotThrow(() -> ParsewispParser("S = \"a\"?", opts));

        var p = ParsewispParser("S = \"a\"?", opts);
        Assertions.assertEquals(PT.create("S"), p.parse(""));
        Assertions.assertEquals(PT.create("S", "a"), p.parse("a"));
    }

    private static void optionalQueryUnavailable(ParserCreationOptions opts, boolean run) {
        if (!run) return;
        Assertions.assertThrows(ParserCreationFailure.class, () -> ParsewispParser("S = \"a\"?", opts));
    }

    private static void optionalRepetitionAvailable(ParserCreationOptions opts, boolean run) {
        if (!run) return;

        Assertions.assertDoesNotThrow(() -> ParsewispParser("S = {\"a\"}", opts));

        var p = ParsewispParser("S = {\"a\"}", opts);
        Assertions.assertEquals(PT.create("S"), p.parse(""));
        Assertions.assertEquals(PT.create("S", "a"), p.parse("a"));
        Assertions.assertEquals(PT.create("S", "a", "a"), p.parse("aa"));
    }

    private static void optionalRepetitionUnavailable(ParserCreationOptions opts, boolean run) {
        if (!run) return;
        Assertions.assertThrows(ParserCreationFailure.class, () -> ParsewispParser("S = {\"a\"}", opts));
    }

    private static void optionalRepetitionStarAvailable(ParserCreationOptions opts, boolean run) {
        if (!run) return;

        Assertions.assertDoesNotThrow(() -> ParsewispParser("S = \"a\"*", opts));

        var p = ParsewispParser("S = \"a\"*", opts);
        Assertions.assertEquals(PT.create("S"), p.parse(""));
        Assertions.assertEquals(PT.create("S", "a"), p.parse("a"));
        Assertions.assertEquals(PT.create("S", "a", "a"), p.parse("aa"));
    }

    private static void optionalRepetitionStarUnavailable(ParserCreationOptions opts, boolean run) {
        if (!run) return;
        Assertions.assertThrows(ParserCreationFailure.class, () -> ParsewispParser("S = \"a\"*", opts));
    }

    private static void orderedChoiceAvailable(ParserCreationOptions opts, boolean run) {
        if (!run) return;

        Assertions.assertDoesNotThrow(() -> ParsewispParser("S = \"a\" / \"b\"", opts));

        Assertions.assertEquals(
                Set.of(PT.create("S", PT.create("A", "a")),
                        PT.create("S", PT.create("B", "a"))),
                new HashSet<>(ParsewispParser("S = A / B\nA = \"a\"\nB = \"a\"", opts).parses("a"))
        );
    }

    private static void orderedChoiceUnavailable(ParserCreationOptions opts, boolean run) {
        if (!run) return;
        Assertions.assertThrows(ParserCreationFailure.class, () -> ParsewispParser("S = \"a\" / \"b\"", opts));
    }

    private static void plusAvailable(ParserCreationOptions opts, boolean run) {
        if (!run) return;

        Assertions.assertDoesNotThrow(() -> ParsewispParser("S = \"a\"+", opts));

        var p = ParsewispParser("S = \"a\"+", opts);
        Assertions.assertTrue(p.parse("").isFailure());
        Assertions.assertEquals(PT.create("S", "a"), p.parse("a"));
        Assertions.assertEquals(PT.create("S", "a", "a"), p.parse("aa"));
    }

    private static void plusUnavailable(ParserCreationOptions opts, boolean run) {
        if (!run) return;
        Assertions.assertThrows(ParserCreationFailure.class, () -> ParsewispParser("S = \"a\"+", opts));
    }

    private static void regexAvailable(ParserCreationOptions opts, boolean run) {
        if (!run) return;

        Assertions.assertDoesNotThrow(() -> ParsewispParser("S = #\"a\"", opts));
        Assertions.assertDoesNotThrow(() -> ParsewispParser("S = #'a'", opts));

        var p = ParsewispParser("S = #\"[0-9]\"", opts);
        Assertions.assertTrue(p.parse("").isFailure());
        Assertions.assertEquals(PT.create("S", "0"), p.parse("0"));
        Assertions.assertEquals(PT.create("S", "5"), p.parse("5"));
    }

    private static void regexUnavailable(ParserCreationOptions opts, boolean run) {
        if (!run) return;
        Assertions.assertThrows(ParserCreationFailure.class, () -> ParsewispParser("S = #\"a\"", opts));
        Assertions.assertThrows(ParserCreationFailure.class, () -> ParsewispParser("S = #'a'", opts));
    }

    private static void singleQuotesForStringTerminalsAvailable(ParserCreationOptions opts, boolean run) {
        if (!run) return;

        Assertions.assertDoesNotThrow(() -> ParsewispParser("S = 'abc'", opts));

        Assertions.assertEquals(
                ParsewispParser("S = \"abc\"", opts),
                ParsewispParser("S = 'abc'", opts));
    }

    private static void singleQuotesForStringTerminalsUnavailable(ParserCreationOptions opts, boolean run) {
        if (!run) return;
        Assertions.assertThrows(ParserCreationFailure.class, () -> ParsewispParser("S = 'abc'", opts));
    }

    private static void valueRangeAvailable(ParserCreationOptions opts, boolean run) {
        if (!run) return;

        Assertions.assertDoesNotThrow(() -> ParsewispParser("S = %x41-5a", opts));
        Assertions.assertDoesNotThrow(() -> ParsewispParser("S = %d65-90", opts));
        Assertions.assertDoesNotThrow(() -> ParsewispParser("S = %b1000001-1011010", opts));

        var pHex = ParsewispParser("S = %x41-5a", opts);
        var pDec = ParsewispParser("S = %d65-90", opts);
        var pBin = ParsewispParser("S = %b1000001-1011010", opts);
        Assertions.assertEquals(pHex, pDec);
        Assertions.assertEquals(pHex, pBin);
    }

    private static void valueRangeUnavailable(ParserCreationOptions opts, boolean run) {
        if (!run) return;
        Assertions.assertThrows(ParserCreationFailure.class, () -> ParsewispParser("S = %x41-5a", opts));
        Assertions.assertThrows(ParserCreationFailure.class, () -> ParsewispParser("S = %d65-90", opts));
        Assertions.assertThrows(ParserCreationFailure.class, () -> ParsewispParser("S = %b1000001-1011010", opts));
    }

    private static void variableRepetitionAvailable(ParserCreationOptions opts, boolean run) {
        if (!run) return;

        Assertions.assertDoesNotThrow(() -> ParsewispParser("S = 1*5 \"a\"", opts));
        Assertions.assertDoesNotThrow(() -> ParsewispParser("S = 1* \"a\"", opts));
        Assertions.assertDoesNotThrow(() -> ParsewispParser("S = *5 \"a\"", opts));

        Assertions.assertThrows(ParserCreationFailure.class, () -> ParsewispParser("S = * \"a\"", opts));

        var p = ParsewispParser("S = 1*3 \"a\" \"b\"", opts);
        Assertions.assertTrue(p.parse("a").isFailure());
        Assertions.assertTrue(p.parse("b").isFailure());
        Assertions.assertEquals(PT.create("S", "a", "b"), p.parse("ab"));
        Assertions.assertEquals(PT.create("S", "a", "a", "b"), p.parse("aab"));
        Assertions.assertEquals(PT.create("S", "a", "a", "a", "b"), p.parse("aaab"));
        Assertions.assertTrue(p.parse("aaaab").isFailure());
    }

    private static void variableRepetitionUnavailable(ParserCreationOptions opts, boolean run) {
        if (!run) return;

        Assertions.assertThrows(ParserCreationFailure.class, () -> ParsewispParser("S = 1*5 \"a\"", opts));
        Assertions.assertThrows(ParserCreationFailure.class, () -> ParsewispParser("S = 1* \"a\"", opts));
        Assertions.assertThrows(ParserCreationFailure.class, () -> ParsewispParser("S = *5 \"a\"", opts));
        Assertions.assertThrows(ParserCreationFailure.class, () -> ParsewispParser("S = * \"a\"", opts));
    }

    private static void exclusionAvailable(ParserCreationOptions opts, boolean run) {
        if (!run) return;

        Assertions.assertDoesNotThrow(() -> ParsewispParser("S = #'[0-9]+' - '1'", opts));
        Assertions.assertDoesNotThrow(() -> ParsewispParser("S = #'[0-9]+' - ('1' | '11' | '111')", opts));
        Assertions.assertDoesNotThrow(() -> ParsewispParser("S = #'[0-9]+' - (#'[1]+' - '11')", opts));

        var p = ParsewispParser("S = #'[0-9]+' - '1'", opts);
        Assertions.assertTrue(p.parse("1").isFailure());
        Assertions.assertEquals(PT.create("S", "11"), p.parse("11"));
        Assertions.assertEquals(PT.create("S", "1111"), p.parse("1111"));
        Assertions.assertEquals(PT.create("S", "2"), p.parse("2"));

        p = ParsewispParser("S = #'[0-9]+' - ('1' | '11' | '111')", opts);
        Assertions.assertTrue(p.parse("1").isFailure());
        Assertions.assertTrue(p.parse("11").isFailure());
        Assertions.assertEquals(PT.create("S", "1111"), p.parse("1111"));
        Assertions.assertEquals(PT.create("S", "2"), p.parse("2"));

        p = ParsewispParser("S = #'[0-9]+' - (#'[1]+' - '11')", opts);
        Assertions.assertTrue(p.parse("1").isFailure());
        Assertions.assertEquals(PT.create("S", "11"), p.parse("11"));
        Assertions.assertTrue(p.parse("1111").isFailure());
        Assertions.assertEquals(PT.create("S", "2"), p.parse("2"));
    }

    private static void exclusionUnavailable(ParserCreationOptions opts, boolean run) {
        if (!run) return;

        Assertions.assertThrows(ParserCreationFailure.class, () ->
                ParsewispParser("S = #'[0-9]+' - '1'", opts));
        Assertions.assertThrows(ParserCreationFailure.class, () ->
                ParsewispParser("S = #'[0-9]+' - ('1' | '11' | '111')", opts));
        Assertions.assertThrows(ParserCreationFailure.class, () ->
                ParsewispParser("S = #'[0-9]+' - (#'[1]+' - '11')", opts));
    }

    private static void abnfCoreAvailable(ParserCreationOptions opts, boolean run) {
        if (!run) return;

        Assertions.assertDoesNotThrow(() -> ParsewispParser(
                "S = ALPHA BIT CHAR CR CRLF CTL DIGIT DQUOTE HEXDIG HTAB LF LWSP OCTET SP VCHAR WSP",
                opts));

        var text = "a";
        Assertions.assertEquals(PT.create("S", PT.create("ALPHA", text)), ParsewispParser("S = ALPHA", opts).parse(text));

        text = "0";
        Assertions.assertEquals(PT.create("S", PT.create("BIT", text)), ParsewispParser("S = BIT", opts).parse(text));

        text = "b";
        Assertions.assertEquals(PT.create("S", PT.create("CHAR", text)), ParsewispParser("S = CHAR", opts).parse(text));

        text = "\r";
        Assertions.assertEquals(PT.create("S", PT.create("CR", text)), ParsewispParser("S = CR", opts).parse(text));

        text = "\r\n";
        Assertions.assertEquals(PT.create("S", PT.create("CRLF", text)), ParsewispParser("S = CRLF", opts).parse(text));

        text = "\u0001";
        Assertions.assertEquals(PT.create("S", PT.create("CTL", text)), ParsewispParser("S = CTL", opts).parse(text));

        text = "5";
        Assertions.assertEquals(PT.create("S", PT.create("DIGIT", text)), ParsewispParser("S = DIGIT", opts).parse(text));

        text = "\"";
        Assertions.assertEquals(PT.create("S", PT.create("DQUOTE", text)), ParsewispParser("S = DQUOTE", opts).parse(text));

        text = "F";
        Assertions.assertEquals(PT.create("S", PT.create("HEXDIG", text)), ParsewispParser("S = HEXDIG", opts).parse(text));

        text = "\t";
        Assertions.assertEquals(PT.create("S", PT.create("HTAB", text)), ParsewispParser("S = HTAB", opts).parse(text));

        text = "\n";
        Assertions.assertEquals(PT.create("S", PT.create("LF", text)), ParsewispParser("S = LF", opts).parse(text));

        text = " ";
        Assertions.assertEquals(PT.create("S", PT.create("LWSP", text)), ParsewispParser("S = LWSP", opts).parse(text));

        text = "A";
        Assertions.assertEquals(PT.create("S", PT.create("OCTET", text)), ParsewispParser("S = OCTET", opts).parse(text));

        text = " ";
        Assertions.assertEquals(PT.create("S", PT.create("SP", text)), ParsewispParser("S = SP", opts).parse(text));

        text = "!";
        Assertions.assertEquals(PT.create("S", PT.create("VCHAR", text)), ParsewispParser("S = VCHAR", opts).parse(text));

        text = " ";
        Assertions.assertEquals(PT.create("S", PT.create("WSP", text)), ParsewispParser("S = WSP", opts).parse(text));
    }

    private static void abnfCoreUnavailable(ParserCreationOptions opts, boolean run) {
        if (!run) return;

        Assertions.assertThrows(IllegalGrammarException.class, () -> ParsewispParser(
                "S = ALPHA BIT CHAR CR CRLF CTL DIGIT DQUOTE HEXDIG HTAB LF LWSP OCTET SP VCHAR WSP",
                opts));
    }


    private static void semicolonLineComment1(ParserCreationOptions opts, boolean run) {
        if (!run) return;

        Assertions.assertEquals(
                PT.create("S", "a"),
                ParsewispParser("S = \"a\" ; ignore", opts).parse("a")
        );
        Assertions.assertEquals(
                PT.create("S",
                        PT.create("A", "a")),
                ParsewispParser(""" 
                        S = A ; ignore
                        A = "a"
                        """, opts)
                        .parse("a")
        );
    }
}
