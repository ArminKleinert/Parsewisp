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

class EpsilonTermTest {
    private Rule string_a() {
        return StringTerm.createDirect("a", false);
    }

    @Test
    void getDefault() {
        Assertions.assertEquals(EpsilonTerm.getDefault(), EpsilonTerm.getDefault());
    }

    @Test
    void parse() {
        var p1 = Parsewisp.parser(Gr.grammar(
                "S", ConcatRule.createDirect(List.of(EpsilonTerm.getDefault(), string_a()))));
        Assertions.assertEquals(PT.create("S", "a"), p1.parse("a"));

        var p2 = Parsewisp.parser(Gr.grammar(
                "S", ConcatRule.createDirect(List.of(EpsilonTerm.getDefault(), EpsilonTerm.getDefault()))));
        Assertions.assertEquals(PT.create("S"), p2.parse(""));
    }

    // Never happens. Eps can only fail on full parse.
    // @Test void parseError() {}

    @Test
    void fullParseSingle() {
        var p = Parsewisp.parser(Gr.grammar("S", EpsilonTerm.getDefault()));
        Assertions.assertEquals(PT.create("S"), p.parse(""));
    }

    @Test
    void fullParseConcat() {
        var pE = Parsewisp.parser(Gr.grammar(
                "S", ConcatRule.createDirect(List.of(EpsilonTerm.getDefault(), EpsilonTerm.getDefault()))));
        Assertions.assertEquals(PT.create("S"), pE.parse(""));

        var pA = Parsewisp.parser(Gr.grammar(
                "S", ConcatRule.createDirect(List.of(string_a(), EpsilonTerm.getDefault()))));
        Assertions.assertEquals(PT.create("S", "a"), pA.parse("a"));
    }

    @Test
    void fullParseError() {
        var p = Parsewisp.parser(Gr.grammar("S", EpsilonTerm.getDefault()));
        Assertions.assertTrue(p.parse("a").isFailure());
    }

    @Test
    void withHideTag() {
        // By default, not hidden in result. May create a new object, but the output must be equal to the original.
        Assertions.assertEquals(EpsilonTerm.getDefault(), EpsilonTerm.getDefault().withHideTag(false));
        Assertions.assertEquals(EpsilonTerm.getDefault(), EpsilonTerm.getDefault().includeInResult());

        // Same input -> Equivalent output
        Assertions.assertEquals(EpsilonTerm.getDefault().withHideTag(false), EpsilonTerm.getDefault().withHideTag(false));
        Assertions.assertEquals(EpsilonTerm.getDefault().withHideTag(true), EpsilonTerm.getDefault().withHideTag(true));

        // Alternative names give same output for isHiddenInResult()
        Assertions.assertFalse(EpsilonTerm.getDefault().withHideTag(false).isHiddenInResult());
        Assertions.assertFalse(EpsilonTerm.getDefault().includeInResult().isHiddenInResult());
        Assertions.assertTrue(EpsilonTerm.getDefault().withHideTag(true).isHiddenInResult());
        Assertions.assertTrue(EpsilonTerm.getDefault().hideInResult().isHiddenInResult());

        // Alternative names give equivalent output
        Assertions.assertEquals(EpsilonTerm.getDefault().withHideTag(false), EpsilonTerm.getDefault().includeInResult());
        Assertions.assertEquals(EpsilonTerm.getDefault().withHideTag(true), EpsilonTerm.getDefault().hideInResult());
    }

    @Test
    void withHideTagParse() {
        // By default, not hidden in output, but this type adds nothing to the tree
        var pDefault = Parsewisp.parser(Gr.grammar("S", EpsilonTerm.getDefault()));
        Assertions.assertEquals(PT.create("S"), pDefault.parse(""));

        // With explicit show, also not hidden, but this type adds nothing to the tree
        var pShow = Parsewisp.parser(Gr.grammar("S", EpsilonTerm.getDefault().withHideTag(false)));
        Assertions.assertEquals(PT.create("S"), pShow.parse(""));

        // With explicit hide
        var pHide = Parsewisp.parser(Gr.grammar("S", EpsilonTerm.getDefault().withHideTag(true)));
        Assertions.assertEquals(PT.create("S"), pHide.parse(""));
    }

    @Test
    void withReduction() {
        // Default.
        Assertions.assertEquals(
                EpsilonTerm.getDefault(),
                EpsilonTerm.getDefault().withReduction(Reduction.initial()));

        // Default: Don't merge up
        var pDefault = Parsewisp.parser(
                Gr.grammarS("S",
                        "S", NonTerminal.create(Sym.sym("A")),
                        "A", EpsilonTerm.getDefault()));
        Assertions.assertEquals(PT.create("S", PT.create("A")), pDefault.parse(""));
    }

    @Test
    void withReductionMergeUp() {
        // Merge upwards
        var pMerge = Parsewisp.parser(
                Gr.grammarS("S",
                        "S", NonTerminal.create(Sym.sym("A")),
                        "A", EpsilonTerm.getDefault().withReduction(Reduction.merging())));
        Assertions.assertEquals(PT.create("S"), pMerge.parse(""));
    }

    @Test
    void withReductionTaggedTree() {
        // Add to parse tree with potentially different name (Top-level)
        var pMerge2 = Parsewisp.parser(
                Gr.grammarS("S",
                        "S", EpsilonTerm.getDefault().withReduction(Reduction.taggedTree(Sym.sym("B")))));
        Assertions.assertEquals(PT.create("B"), pMerge2.parse(""));

        // Add to parse tree with potentially different name (Nested)
        var pMerge3 = Parsewisp.parser(
                Gr.grammarS("S",
                        "S", NonTerminal.create(Sym.sym("A")),
                        "A", EpsilonTerm.getDefault().withReduction(Reduction.taggedTree(Sym.sym("B")))));
        Assertions.assertEquals(PT.create("S", PT.create("B")), pMerge3.parse(""));
    }

    @Test
    void withReductionNotDirectRhs() {
        // Add parse tree from a rule which doesn't appear in the grammar.
        var pMerge4 = Parsewisp.parser(
                Gr.grammarS("S",
                        "S", NonTerminal.create(Sym.sym("A")),
                        "A", ConcatRule.create(List.of(
                                EpsilonTerm.getDefault().withReduction(Reduction.taggedTree(Sym.sym("B"))),
                                EpsilonTerm.getDefault()
                        ))));
        Assertions.assertEquals(
                PT.create("S", PT.create("A", PT.create("B"))),
                pMerge4.parse(""));
    }
}