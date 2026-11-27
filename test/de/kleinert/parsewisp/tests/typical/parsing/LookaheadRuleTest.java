package de.kleinert.parsewisp.tests.typical.parsing;

import de.kleinert.parsewisp.Parsewisp;
import de.kleinert.parsewisp.Sym;
import de.kleinert.parsewisp.parsing.*;
import de.kleinert.parsewisp.reduction.Reduction;
import de.kleinert.parsewisp.testutil.Gr;
import de.kleinert.parsewisp.testutil.PT;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.regex.Pattern;

class LookaheadRuleTest {
    @Test
    void create() {
        var laEps = LookaheadRule.create(EpsilonTerm.getDefault());
        Assertions.assertTrue(laEps instanceof EpsilonTerm || laEps instanceof LookaheadRule);

        Assertions.assertEquals(
                LookaheadRule.create(StringTerm.create("a", false)),
                LookaheadRule.create(StringTerm.create("a", false)));
    }

    @Test
    void parse() {
        var p = Parsewisp.parser(Gr.grammar(
                "S", ConcatRule.create(List.of(
                        LookaheadRule.create(RegexTerm.create(Pattern.compile("[a-zA-Z]"))), // Look for any letter
                        LookaheadRule.create(RegexTerm.create(Pattern.compile("[a-fA-F0-9]"))), // Look for any hex digit
                        RegexTerm.create(Pattern.compile("[\\x00-\\xFF]+")) // Any 8-bit ascii
                ))));
        for (char c = 0; c <= 255; c++) {
            var text = "" + c;
            if ((c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F')) {
                Assertions.assertEquals(PT.create("S", text), p.parse(text));
            } else {
                Assertions.assertTrue(p.parse(text).isFailure());
            }
        }
    }

    @Test
    void parseDoubledLook() {
        // This parser includes a doubled lookahead for the terminal "a".
        var p = Parsewisp.parser(Gr.grammar(
                "S", ConcatRule.create(List.of(
                        LookaheadRule.create(StringTerm.create("a", false)),
                        LookaheadRule.create(StringTerm.create("a", false)),
                        RegexTerm.create(Pattern.compile("[a-z]"))
                ))));
        Assertions.assertEquals(PT.create("S", "a"), p.parse("a"));
        Assertions.assertTrue(p.parse("b").isFailure()); // Nope. Was looking for "a".
    }

    @Test
    void parseError() {
        var p = Parsewisp.parser(Gr.grammar(
                "S", ConcatRule.create(List.of(
                        LookaheadRule.create(StringTerm.create("a", false)),
                        RegexTerm.create(Pattern.compile("[a-z]"))
                ))));
        Assertions.assertTrue(p.parse("b").isFailure()); // Nope. Was looking for "a".
    }

    @Test
    void parseContradictoryLookahead() {
        // This parser has lookaheads for two rules that can not be there simultaneously. It looks for both "a" and "b".
        var p = Parsewisp.parser(Gr.grammar(
                "S", ConcatRule.create(List.of(
                        LookaheadRule.create(StringTerm.create("a", false)),
                        LookaheadRule.create(StringTerm.create("b", false)),
                        RegexTerm.create(Pattern.compile("[a-z]"))
                ))));
        Assertions.assertTrue(p.parse("a").isFailure()); // Nope. Was looking for "b".
        Assertions.assertTrue(p.parse("b").isFailure()); // Nope. Was looking for "a".
        Assertions.assertTrue(p.parse("c").isFailure()); // Fits neither lookahead.
    }

    @Test
    void fullParseSingle() {
        Assertions.assertEquals(
                PT.create("S"),
                Parsewisp.parser(Gr.grammar(
                        "S", LookaheadRule.create(EpsilonTerm.getDefault()))
                ).parse(""));
        Assertions.assertEquals(
                PT.create("S"),
                Parsewisp.parser(Gr.grammar(
                        "S", LookaheadRule.create(EOFTerm.getDefault()))
                ).parse(""));
    }

    @Test
    void fullParseConcat() {
        var p = Parsewisp.parser(Gr.grammar(
                "S", ConcatRule.create(List.of(
                        StringTerm.create("a", false),
                        LookaheadRule.create(EpsilonTerm.getDefault())))));
        Assertions.assertEquals(PT.create("S", "a"), p.parse("a"));
    }

    @Test
    void fullParseError() {
        var p = Parsewisp.parser(Gr.grammar(
                "S", LookaheadRule.create(StringTerm.create("a", false))));
        Assertions.assertTrue(p.parse("a").isFailure()); // Nope. Was looking for "b".
        Assertions.assertTrue(p.parse("b").isFailure()); // Nope. Was looking for "a".
        Assertions.assertTrue(p.parse("c").isFailure()); // Fits neither lookahead.
    }

    @Test
    void getRule() {
        var i = StringTerm.create("a", false);
        var r = (LookaheadRule) LookaheadRule.create(i);
        Assertions.assertEquals(i, r.getRule());
    }

    @Test
    void withRule() {
        var i = StringTerm.create("a", false);
        var r = (LookaheadRule) LookaheadRule.create(i);

        // Expected
        Assertions.assertEquals(i, r.getRule());

        var ni = StringTerm.createDirect("c", false);
        var nr = r.withRule(ni);

        // The new rule.
        Assertions.assertEquals(ni, nr.getRule());

        // Original left untouched.
        Assertions.assertEquals(i, r.getRule());
    }

    @Test
    void withRulePreservesHideAndRed() {
        var i = StringTerm.create("a", false);
        var r = (LookaheadRule) LookaheadRule
                .create(i)
                .withHideTag(true)
                .withReduction(Reduction.taggedTree(Sym.sym("A")));

        // As expected.
        Assertions.assertEquals(i, r.getRule());

        // Create a new rule, check whether hide and reduction are still unchanged.
        var ni = StringTerm.create("b", false);
        var nr = r.withRule(ni);
        Assertions.assertEquals(ni, nr.getRule());
        Assertions.assertEquals(r.isHiddenInResult(), nr.isHiddenInResult());
        Assertions.assertEquals(r.getReduction(), nr.getReduction());

        // Original left untouched.
        Assertions.assertEquals(i, r.getRule());
    }

    @Test
    void withHideTag() {
        var r = (LookaheadRule) LookaheadRule.create(StringTerm.create("a", false));

        // By default, not hidden in result. May create a new object, but the output must be equal to the original.
        Assertions.assertEquals(r, r.withHideTag(false));
        Assertions.assertEquals(r, r.includeInResult());

        // Same input -> Equivalent output
        Assertions.assertEquals(r.withHideTag(false), r.withHideTag(false));
        Assertions.assertEquals(r.withHideTag(true), r.withHideTag(true));

        // Alternative names give same output for isHiddenInResult()
        Assertions.assertFalse(r.withHideTag(false).isHiddenInResult());
        Assertions.assertFalse(r.includeInResult().isHiddenInResult());
        Assertions.assertTrue(r.withHideTag(true).isHiddenInResult());
        Assertions.assertTrue(r.hideInResult().isHiddenInResult());

        // Alternative names give equivalent output
        Assertions.assertEquals(r.withHideTag(false), r.includeInResult());
        Assertions.assertEquals(r.withHideTag(true), r.hideInResult());
    }

    @Test
    void withHideTagParse() {
        var r = (LookaheadRule) LookaheadRule.create(StringTerm.create("a", false));

        // By default, not hidden in output, but this type adds nothing to the tree
        var pDefault = Parsewisp.parser(Gr.grammar(
                "S", ConcatRule.create(List.of(
                        r,
                        RegexTerm.create(Pattern.compile("[a-z]"))))));
        Assertions.assertEquals(PT.create("S", "a"), pDefault.parse("a"));

        // With explicit show, also not hidden, but this type adds nothing to the tree
        var pShow = Parsewisp.parser(Gr.grammar(
                "S", ConcatRule.create(List.of(
                        r.withHideTag(false),
                        RegexTerm.create(Pattern.compile("[a-z]"))))));
        Assertions.assertEquals(PT.create("S", "a"), pShow.parse("a"));

        // With explicit hide
        var pHide = Parsewisp.parser(Gr.grammar(
                "S", ConcatRule.create(List.of(
                        r.withHideTag(true),
                        RegexTerm.create(Pattern.compile("[a-z]"))))));
        Assertions.assertEquals(PT.create("S", "a"), pHide.parse("a"));
    }

    @Test
    void withReduction() {
        var r = (LookaheadRule) LookaheadRule.create(StringTerm.create("a", false));

        // Default.
        Assertions.assertEquals(
                r,
                r.withReduction(Reduction.initial()));

        // Default: Don't merge up
        var pDefault = Parsewisp.parser(
                Gr.grammarS("S",
                        "S", NonTerminal.create(Sym.sym("A")),
                        "A", ConcatRule.create(List.of(
                                r,
                                RegexTerm.create(Pattern.compile("[a-z]"))))));
        Assertions.assertEquals(PT.create("S", PT.create("A", "a")), pDefault.parse("a"));
        Assertions.assertTrue(pDefault.parse("b").isFailure());
    }

    @Test
    void withReductionMergeUp() {
        // Merge upwards
        var pMerge = Parsewisp.parser(
                Gr.grammarS("S",
                        "S", NonTerminal.create(Sym.sym("A")),
                        "A", LookaheadRule.create(EpsilonTerm.getDefault())
                                .withReduction(Reduction.merging())
                ));
        Assertions.assertEquals(PT.create("S"), pMerge.parse(""));
    }

    @Test
    void withReductionTaggedTree() {
        // Add to parse tree with potentially different name (Top-level)
        var pMerge2 = Parsewisp.parser(
                Gr.grammarS("S",
                        "S", LookaheadRule.create(EpsilonTerm.getDefault())
                                .withReduction(Reduction.taggedTree(Sym.sym("B")))
                ));
        Assertions.assertEquals(PT.create("B"), pMerge2.parse(""));

        // Add to parse tree with potentially different name (Nested)
        var pMerge3 = Parsewisp.parser(
                Gr.grammarS("S",
                        "S", NonTerminal.create(Sym.sym("A")),
                        "A", LookaheadRule.create(EpsilonTerm.getDefault())
                                .withReduction(Reduction.taggedTree(Sym.sym("B")))
                ));
        Assertions.assertEquals(PT.create("S", PT.create("B")), pMerge3.parse(""));
    }

    @Test
    void withReductionNotDirectRhs() {
        var r = (LookaheadRule) LookaheadRule.create(StringTerm.create("a", false));

        // Add parse tree from a rule which doesn't appear in the grammar.
        var pMerge4 = Parsewisp.parser(
                Gr.grammarS("S",
                        "S", NonTerminal.create(Sym.sym("A")),
                        "A", ConcatRule.create(List.of(
                                r.withReduction(Reduction.taggedTree(Sym.sym("B"))),
                                RegexTerm.create(Pattern.compile("[a-z]"))
                        ))));
        Assertions.assertEquals(
                PT.create("S", PT.create("A", PT.create("B"), "a")),
                pMerge4.parse("a"));
    }
}