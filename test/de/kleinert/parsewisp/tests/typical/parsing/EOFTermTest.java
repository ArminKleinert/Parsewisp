package de.kleinert.parsewisp.tests.typical.parsing;

import de.kleinert.parsewisp.Parsewisp;
import de.kleinert.parsewisp.Sym;
import de.kleinert.parsewisp.parsing.ConcatRule;
import de.kleinert.parsewisp.parsing.EOFTerm;
import de.kleinert.parsewisp.parsing.NonTerminal;
import de.kleinert.parsewisp.parsing.StringTerm;
import de.kleinert.parsewisp.reduction.Reduction;
import de.kleinert.parsewisp.testutil.Gr;
import de.kleinert.parsewisp.testutil.PT;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class EOFTermTest {
    @Test
    void getDefault() {
        Assertions.assertEquals(EOFTerm.getDefault(), EOFTerm.getDefault());
    }

    @Test
    void parse() {
        var p2 = Parsewisp.parser(Gr.grammar(
                "S", ConcatRule.createDirect(List.of(EOFTerm.getDefault(), EOFTerm.getDefault()))));
        Assertions.assertEquals(PT.create("S"), p2.parse(""));

        var p3 = Parsewisp.parser(Gr.grammar(
                "S", ConcatRule.createDirect(List.of(EOFTerm.getDefault(), EOFTerm.getDefault(), EOFTerm.getDefault()))));
        Assertions.assertEquals(PT.create("S"), p3.parse(""));
    }

    @Test
    void parseError() {
        var p = Parsewisp.parser(Gr.grammar(
                "S", ConcatRule.createDirect(List.of(EOFTerm.getDefault(), StringTerm.create("a", false)))));
        Assertions.assertTrue(p.parse("a").isFailure());
    }

    @Test
    void fullParseSingle() {
        var p = Parsewisp.parser(Gr.grammar("S", EOFTerm.getDefault()));
        Assertions.assertEquals(PT.create("S"), p.parse(""));
    }

    @Test
    void fullParseConcat() {
        var pE = Parsewisp.parser(Gr.grammar(
                "S", ConcatRule.createDirect(List.of(EOFTerm.getDefault(), EOFTerm.getDefault()))));
        Assertions.assertEquals(PT.create("S"), pE.parse(""));

        var pA = Parsewisp.parser(Gr.grammar(
                "S", ConcatRule.createDirect(List.of(StringTerm.create("a", false), EOFTerm.getDefault()))));
        Assertions.assertEquals(PT.create("S", "a"), pA.parse("a"));
    }

    @Test
    void fullParseError() {
        var p = Parsewisp.parser(Gr.grammar("S", EOFTerm.getDefault()));
        Assertions.assertTrue(p.parse("a").isFailure());
    }

    @Test
    void withHideTag() {
        // By default, not hidden in result. May create a new object, but the output must be equal to the original.
        Assertions.assertEquals(EOFTerm.getDefault(), EOFTerm.getDefault().withHideTag(false));
        Assertions.assertEquals(EOFTerm.getDefault(), EOFTerm.getDefault().includeInResult());

        // Same input -> Equivalent output
        Assertions.assertEquals(EOFTerm.getDefault().withHideTag(false), EOFTerm.getDefault().withHideTag(false));
        Assertions.assertEquals(EOFTerm.getDefault().withHideTag(true), EOFTerm.getDefault().withHideTag(true));

        // Alternative names give same output for isHiddenInResult()
        Assertions.assertFalse(EOFTerm.getDefault().withHideTag(false).isHiddenInResult());
        Assertions.assertFalse(EOFTerm.getDefault().includeInResult().isHiddenInResult());
        Assertions.assertTrue(EOFTerm.getDefault().withHideTag(true).isHiddenInResult());
        Assertions.assertTrue(EOFTerm.getDefault().hideInResult().isHiddenInResult());
    }

    @Test
    void withHideTagParse() {
        // Alternative names give equivalent output
        Assertions.assertEquals(EOFTerm.getDefault().withHideTag(false), EOFTerm.getDefault().includeInResult());
        Assertions.assertEquals(EOFTerm.getDefault().withHideTag(true), EOFTerm.getDefault().hideInResult());

        // By default, not hidden in output, but this type adds nothing to the tree
        var pDefault = Parsewisp.parser(Gr.grammar("S", EOFTerm.getDefault()));
        Assertions.assertEquals(PT.create("S"), pDefault.parse(""));

        // With explicit show, also not hidden, but this type adds nothing to the tree
        var pShow = Parsewisp.parser(Gr.grammar("S", EOFTerm.getDefault().withHideTag(false)));
        Assertions.assertEquals(PT.create("S"), pShow.parse(""));

        // With explicit hide
        var pHide = Parsewisp.parser(Gr.grammar("S", EOFTerm.getDefault().withHideTag(true)));
        Assertions.assertEquals(PT.create("S"), pHide.parse(""));
    }

    @Test
    void withReduction() {
        // Default.
        Assertions.assertEquals(
                EOFTerm.getDefault(),
                EOFTerm.getDefault().withReduction(Reduction.initial()));

        // Default: Don't merge up
        var pDefault = Parsewisp.parser(
                Gr.grammarS("S",
                        "S", NonTerminal.create(Sym.sym("A")),
                        "A", EOFTerm.getDefault()));
        Assertions.assertEquals(PT.create("S", PT.create("A")), pDefault.parse(""));
    }

    @Test
    void withReductionMergeUp() {
        // Merge upwards
        var pMerge = Parsewisp.parser(
                Gr.grammarS("S",
                        "S", NonTerminal.create(Sym.sym("A")),
                        "A", EOFTerm.getDefault().withReduction(Reduction.merging())));
        Assertions.assertEquals(PT.create("S"), pMerge.parse(""));
    }

    @Test
    void withReductionTaggedTree() {
        // Add to parse tree with potentially different name (Top-level)
        var pMerge2 = Parsewisp.parser(
                Gr.grammarS("S",
                        "S", EOFTerm.getDefault().withReduction(Reduction.taggedTree(Sym.sym("B")))));
        Assertions.assertEquals(PT.create("B"), pMerge2.parse(""));

        // Add to parse tree with potentially different name (Nested)
        var pMerge3 = Parsewisp.parser(
                Gr.grammarS("S",
                        "S", NonTerminal.create(Sym.sym("A")),
                        "A", EOFTerm.getDefault().withReduction(Reduction.taggedTree(Sym.sym("B")))));
        Assertions.assertEquals(PT.create("S", PT.create("B")), pMerge3.parse(""));
    }

    @Test
    void withReductionNotDirectRhs() {
        // Add parse tree from a rule which doesn't appear in the grammar.
        var pMerge4 = Parsewisp.parser(
                Gr.grammarS("S",
                        "S", NonTerminal.create(Sym.sym("A")),
                        "A", ConcatRule.create(List.of(
                                EOFTerm.getDefault().withReduction(Reduction.taggedTree(Sym.sym("B"))),
                                EOFTerm.getDefault()
                        ))));
        Assertions.assertEquals(
                PT.create("S", PT.create("A", PT.create("B"))),
                pMerge4.parse(""));
    }
}