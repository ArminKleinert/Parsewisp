package de.kleinert.parsewisp.tests.typical;

import de.kleinert.parsewisp.Parsewisp;
import de.kleinert.parsewisp.Sym;
import de.kleinert.parsewisp.parser_options.ParserCreationOptions;
import de.kleinert.parsewisp.parser_options.ParsingOptions;
import de.kleinert.parsewisp.parsing.ConcatRule;
import de.kleinert.parsewisp.parsing.NonTerminal;
import de.kleinert.parsewisp.parsing.RegexTerm;
import de.kleinert.parsewisp.reduction.ReductionType;
import de.kleinert.parsewisp.testutil.Gr;
import de.kleinert.parsewisp.testutil.PT;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.regex.Pattern;

class RegexTest {
    @Test
    void testNum() {
        var opts = ParserCreationOptions.getDefault();
        var p = Parsewisp.parser("S = #\"[a-fA-F0-9]+\"", opts);
        Assertions.assertEquals(
                PT.create("S", "7F"),
                p.parse("7F")
        );
    }

    @Test
    void testUnicodeSequence() {
        // This used to cause a bug where "\\u" consumed one character too far.
        // E.g. "\\u0041-" would consume the minus as if it was part of the number sequence.
        var p = Parsewisp.parser("S = #'[\\u0041-\\u005A]+'");
        Assertions.assertEquals(PT.create("S", "ABC"), p.parse("ABC"));
    }

    RegexTerm digit = (RegexTerm) RegexTerm.create(Pattern.compile("[0-9]"));
    RegexTerm number = (RegexTerm) RegexTerm.create(Pattern.compile("[+\\-]?[0-9]+"));

    @Test
    void create() {
        Assertions.assertEquals(
                RegexTerm.create(Pattern.compile("abc")),
                RegexTerm.create(Pattern.compile("abc")));
        Assertions.assertEquals(
                RegexTerm.create(Pattern.compile("[0-9]+")),
                RegexTerm.create(Pattern.compile("[0-9]+")));
        Assertions.assertEquals(
                RegexTerm.create(Pattern.compile("[0-9]1")),
                RegexTerm.create(Pattern.compile("[0-9]1")));
    }

    @Test
    void parse() {
        var gD = Gr.grammar("S", ConcatRule.create(List.of(digit, digit)));
        var pD = Parsewisp.parser(gD);
        Assertions.assertEquals(PT.create("S", "1", "2"), pD.parse("12"));

        var gN = Gr.grammar("S", ConcatRule.create(List.of(digit, number)));
        var pN = Parsewisp.parser(gN);
        Assertions.assertEquals(PT.create("S", "1", "234"), pN.parse("1234"));
    }

    @Test
    void parseEager() {
        var gN = Gr.grammar("S", ConcatRule.create(List.of(number, digit)));
        var pN = Parsewisp.parser(gN);
        Assertions.assertTrue(pN.parse("1234").isFailure());
    }

    @Test
    void parseIncremental() {
        var gN = Gr.grammar("S", ConcatRule.create(List.of(number, digit)));
        var pN = Parsewisp.parser(gN);
        Assertions.assertEquals(
                PT.create("S", "123", "4"),
                pN.parse("1234", ParsingOptions.getDefault().withIterativeDeepening(true)));
    }

    @Test
    void parseError() {
        var g = Gr.grammar("S", ConcatRule.create(List.of(digit, digit)));
        var p = Parsewisp.parser(g);
        Assertions.assertTrue(p.parse("-1").isFailure());
        Assertions.assertTrue(p.parse("1").isFailure());
        Assertions.assertTrue(p.parse("b").isFailure());
    }

    @Test
    void fullParseSingle() {
        var g = Gr.grammar("S", number);
        var p = Parsewisp.parser(g);
        Assertions.assertEquals(PT.create("S", "1"), p.parse("1"));
        Assertions.assertEquals(PT.create("S", "123"), p.parse("123"));
        Assertions.assertEquals(PT.create("S", "+123"), p.parse("+123"));
        Assertions.assertEquals(PT.create("S", "-123"), p.parse("-123"));
    }

    @Test
    void fullParseConcat() {
        var gD = Gr.grammar("S", ConcatRule.create(List.of(digit, digit)));
        var pD = Parsewisp.parser(gD);
        Assertions.assertEquals(PT.create("S", "1", "2"), pD.parse("12"));

        var gN = Gr.grammar("S", ConcatRule.create(List.of(digit, number)));
        var pN = Parsewisp.parser(gN);
        Assertions.assertEquals(PT.create("S", "1", "234"), pN.parse("1234"));
    }

    @Test
    void fullParseError() {
        var g = Gr.grammar("S", number);
        var p = Parsewisp.parser(g);
        Assertions.assertTrue(p.parse("").isFailure());
        Assertions.assertTrue(p.parse("a").isFailure());
        Assertions.assertTrue(p.parse("&1").isFailure());
    }

    @Test
    void getRegexp() {
        var pattern = Pattern.compile("[0-9]");
        Assertions.assertEquals(
                pattern,
                ((RegexTerm)RegexTerm.create(pattern)).getRegexp());
        Assertions.assertEquals(
                pattern.pattern(),
                ((RegexTerm)RegexTerm.create(pattern)).getRegexp().pattern());
    }

    @Test
    void withHideTag() {
        // By default, not hidden in result. May create a new object, but the output must be equal to the original.
        Assertions.assertEquals(digit, digit.withHideTag(false));
        Assertions.assertEquals(digit, digit.includeInResult());

        // Same input -> Equivalent output
        Assertions.assertEquals(digit.withHideTag(false), digit.withHideTag(false));
        Assertions.assertEquals(digit.withHideTag(true), digit.withHideTag(true));

        // Alternative names give same output for isHiddenInResult()
        Assertions.assertFalse(digit.withHideTag(false).isHiddenInResult());
        Assertions.assertFalse(digit.includeInResult().isHiddenInResult());
        Assertions.assertTrue(digit.withHideTag(true).isHiddenInResult());
        Assertions.assertTrue(digit.hideInResult().isHiddenInResult());

        // Alternative names give equivalent output
        Assertions.assertEquals(digit.withHideTag(false), digit.includeInResult());
        Assertions.assertEquals(digit.withHideTag(true), digit.hideInResult());

        // By default, not hidden in output
        var pDefault = Parsewisp.parser(
                Gr.grammar("S", digit));
        Assertions.assertEquals(PT.create("S", "1"), pDefault.parse("1"));

        // With explicit show, also not hidden
        var pShow = Parsewisp.parser(
                Gr.grammar("S", digit.withHideTag(false)));
        Assertions.assertEquals(PT.create("S", "1"), pShow.parse("1"));

        // With explicit hide, also not hidden
        var pHide = Parsewisp.parser(
                Gr.grammar("S", digit.withHideTag(true)));
        Assertions.assertEquals(PT.create("S"), pHide.parse("1"));
    }

    @Test
    void withReduction() {
        // Default.
        Assertions.assertEquals(
                digit,
                digit.withReduction(ReductionType.standardInitialReduction()));

        // Default: Don't merge up
        var pDefault = Parsewisp.parser(
                Gr.grammarS("S",
                        "S", NonTerminal.create(Sym.sym("A")),
                        "A", digit));
        Assertions.assertEquals(PT.create("S", PT.create("A", "1")), pDefault.parse("1"));

        // Merge upwards
        var pMerge = Parsewisp.parser(
                Gr.grammarS("S",
                        "S", NonTerminal.create(Sym.sym("A")),
                        "A", digit.withReduction(ReductionType.standardIntermediateReduction())));
        Assertions.assertEquals(PT.create("S", "1"), pMerge.parse("1"));

        // Add to parse tree with potentially different name (Top-level)
        var pMerge2 = Parsewisp.parser(
                Gr.grammarS("S",
                        "S", digit.withReduction(ReductionType.taggedTreeReduction(Sym.sym("B")))));
        Assertions.assertEquals(PT.create("B", "1"), pMerge2.parse("1"));

        // Add to parse tree with potentially different name (Nested)
        var pMerge3 = Parsewisp.parser(
                Gr.grammarS("S",
                        "S", NonTerminal.create(Sym.sym("A")),
                        "A", digit.withReduction(ReductionType.taggedTreeReduction(Sym.sym("B")))));
        Assertions.assertEquals(PT.create("S", PT.create("B", "1")), pMerge3.parse("1"));

        var pMerge4 = Parsewisp.parser(
                Gr.grammarS("S",
                        "S", NonTerminal.create(Sym.sym("A")),
                        "A", ConcatRule.create(List.of(
                                digit.withReduction(ReductionType.taggedTreeReduction(Sym.sym("B"))),
                                digit
                        ))));
        Assertions.assertEquals(
                PT.create("S", PT.create("A", PT.create("B", "1"), "2")),
                pMerge4.parse("12"));
    }
}
