package de.kleinert.parsewisp.tests.typical.parsing;

import de.kleinert.parsewisp.Parsewisp;
import de.kleinert.parsewisp.Sym;
import de.kleinert.parsewisp.parsing.ConcatRule;
import de.kleinert.parsewisp.parsing.EOFTerm;
import de.kleinert.parsewisp.parsing.NonTerminal;
import de.kleinert.parsewisp.parsing.StringTerm;
import de.kleinert.parsewisp.reduction.ReductionType;
import de.kleinert.parsewisp.testutil.Gr;
import de.kleinert.parsewisp.testutil.PT;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class EOFTermTest {
    private final EOFTerm eof = EOFTerm.getDefault();

    @Test
    void getDefault() {
        Assertions.assertEquals(EOFTerm.getDefault(), EOFTerm.getDefault());
    }

    @Test
    void parse() {
        var p2 = Parsewisp.parser(Gr.grammar(
                "S", ConcatRule.createDirect(List.of(eof, eof))));
        Assertions.assertEquals(PT.create("S"), p2.parse(""));

        var p3 = Parsewisp.parser(Gr.grammar(
                "S", ConcatRule.createDirect(List.of(eof, eof, eof))));
        Assertions.assertEquals(PT.create("S"), p3.parse(""));
    }

    @Test
    void parseError() {
        var p = Parsewisp.parser(Gr.grammar(
                "S", ConcatRule.createDirect(List.of(eof, StringTerm.create("a", false)))));
        Assertions.assertTrue(p.parse("a").isFailure());
    }

    @Test
    void fullParseSingle() {
        var p = Parsewisp.parser(Gr.grammar("S", eof));
        Assertions.assertEquals(PT.create("S"), p.parse(""));
    }

    @Test
    void fullParseConcat() {
        var pE = Parsewisp.parser(Gr.grammar(
                "S", ConcatRule.createDirect(List.of(eof, eof))));
        Assertions.assertEquals(PT.create("S"), pE.parse(""));

        var pA = Parsewisp.parser(Gr.grammar(
                "S", ConcatRule.createDirect(List.of(StringTerm.create("a", false), eof))));
        Assertions.assertEquals(PT.create("S", "a"), pA.parse("a"));
    }

    @Test
    void fullParseError() {
        var p = Parsewisp.parser(Gr.grammar("S", eof));
        Assertions.assertTrue(p.parse("a").isFailure());
    }

    @Test
    void withHideTag() {
        // By default, not hidden in result. May create a new object, but the output must be equal to the original.
        Assertions.assertEquals(eof, eof.withHideTag(false));
        Assertions.assertEquals(eof, eof.includeInResult());

        // Same input -> Equivalent output
        Assertions.assertEquals(eof.withHideTag(false), eof.withHideTag(false));
        Assertions.assertEquals(eof.withHideTag(true), eof.withHideTag(true));

        // Alternative names give same output for isHiddenInResult()
        Assertions.assertFalse(eof.withHideTag(false).isHiddenInResult());
        Assertions.assertFalse(eof.includeInResult().isHiddenInResult());
        Assertions.assertTrue(eof.withHideTag(true).isHiddenInResult());
        Assertions.assertTrue(eof.hideInResult().isHiddenInResult());

        // Alternative names give equivalent output
        Assertions.assertEquals(eof.withHideTag(false), eof.includeInResult());
        Assertions.assertEquals(eof.withHideTag(true), eof.hideInResult());

        // By default, not hidden in output, but this type adds nothing to the tree
        var pDefault = Parsewisp.parser(Gr.grammar("S", eof));
        Assertions.assertEquals(PT.create("S"), pDefault.parse(""));

        // With explicit show, also not hidden, but this type adds nothing to the tree
        var pShow = Parsewisp.parser(Gr.grammar("S", eof.withHideTag(false)));
        Assertions.assertEquals(PT.create("S"), pShow.parse(""));

        // With explicit hide
        var pHide = Parsewisp.parser(Gr.grammar("S", eof.withHideTag(true)));
        Assertions.assertEquals(PT.create("S"), pHide.parse(""));
    }

    @Test
    void withReduction() {
        // Default.
        Assertions.assertEquals(
                eof,
                eof.withReduction(ReductionType.initial()));

        // Default: Don't merge up
        var pDefault = Parsewisp.parser(
                Gr.grammarS("S",
                        "S", NonTerminal.create(Sym.sym("A")),
                        "A", eof));
        Assertions.assertEquals(PT.create("S", PT.create("A")), pDefault.parse(""));

        // Merge upwards
        var pMerge = Parsewisp.parser(
                Gr.grammarS("S",
                        "S", NonTerminal.create(Sym.sym("A")),
                        "A", eof.withReduction(ReductionType.merging())));
        Assertions.assertEquals(PT.create("S"), pMerge.parse(""));

        // Add to parse tree with potentially different name (Top-level)
        var pMerge2 = Parsewisp.parser(
                Gr.grammarS("S",
                        "S", eof.withReduction(ReductionType.taggedTree(Sym.sym("B")))));
        Assertions.assertEquals(PT.create("B"), pMerge2.parse(""));

        // Add to parse tree with potentially different name (Nested)
        var pMerge3 = Parsewisp.parser(
                Gr.grammarS("S",
                        "S", NonTerminal.create(Sym.sym("A")),
                        "A", eof.withReduction(ReductionType.taggedTree(Sym.sym("B")))));
        Assertions.assertEquals(PT.create("S", PT.create("B")), pMerge3.parse(""));

        // Add parse tree from a rule which doesn't appear in the grammar.
        var pMerge4 = Parsewisp.parser(
                Gr.grammarS("S",
                        "S", NonTerminal.create(Sym.sym("A")),
                        "A", ConcatRule.create(List.of(
                                eof.withReduction(ReductionType.taggedTree(Sym.sym("B"))),
                                eof
                        ))));
        Assertions.assertEquals(
                PT.create("S", PT.create("A", PT.create("B"))),
                pMerge4.parse(""));
    }
}