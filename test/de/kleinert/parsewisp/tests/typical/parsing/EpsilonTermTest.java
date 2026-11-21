package de.kleinert.parsewisp.tests.typical.parsing;

import de.kleinert.parsewisp.Parsewisp;
import de.kleinert.parsewisp.Sym;
import de.kleinert.parsewisp.parsing.*;
import de.kleinert.parsewisp.reduction.ReductionType;
import de.kleinert.parsewisp.testutil.Gr;
import de.kleinert.parsewisp.testutil.PT;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class EpsilonTermTest {
    private final EpsilonTerm eps = EpsilonTerm.getDefault();
    private final StringTerm str_a = StringTerm.createDirect("a", false);

    @Test
    void getDefault() {
        Assertions.assertEquals(EpsilonTerm.getDefault(), EpsilonTerm.getDefault());
    }

    @Test
    void parse() {
        var p1 = Parsewisp.parser(Gr.grammar(
                "S", ConcatRule.createDirect(List.of(eps, str_a))));
        Assertions.assertEquals(PT.create("S", "a"), p1.parse("a"));

        var p2 = Parsewisp.parser(Gr.grammar(
                "S", ConcatRule.createDirect(List.of(eps, eps))));
        Assertions.assertEquals(PT.create("S"), p2.parse(""));
    }

    // Never happens. Eps can only fail on full parse.
    // @Test void parseError() {}

    @Test
    void fullParseSingle() {
        var p = Parsewisp.parser(Gr.grammar("S", eps));
        Assertions.assertEquals(PT.create("S"), p.parse(""));
    }

    @Test
    void fullParseConcat() {
        var pE = Parsewisp.parser(Gr.grammar(
                "S", ConcatRule.createDirect(List.of(eps, eps))));
        Assertions.assertEquals(PT.create("S"), pE.parse(""));

        var pA = Parsewisp.parser(Gr.grammar(
                "S", ConcatRule.createDirect(List.of(str_a, eps))));
        Assertions.assertEquals(PT.create("S", "a"), pA.parse("a"));
    }

    @Test
    void fullParseError() {
        var p = Parsewisp.parser(Gr.grammar("S", eps));
        Assertions.assertTrue(p.parse("a").isFailure());
    }

    @Test
    void withHideTag() {
        // By default, not hidden in result. May create a new object, but the output must be equal to the original.
        Assertions.assertEquals(eps, eps.withHideTag(false));
        Assertions.assertEquals(eps, eps.includeInResult());

        // Same input -> Equivalent output
        Assertions.assertEquals(eps.withHideTag(false), eps.withHideTag(false));
        Assertions.assertEquals(eps.withHideTag(true), eps.withHideTag(true));

        // Alternative names give same output for isHiddenInResult()
        Assertions.assertFalse(eps.withHideTag(false).isHiddenInResult());
        Assertions.assertFalse(eps.includeInResult().isHiddenInResult());
        Assertions.assertTrue(eps.withHideTag(true).isHiddenInResult());
        Assertions.assertTrue(eps.hideInResult().isHiddenInResult());

        // Alternative names give equivalent output
        Assertions.assertEquals(eps.withHideTag(false), eps.includeInResult());
        Assertions.assertEquals(eps.withHideTag(true), eps.hideInResult());

        // By default, not hidden in output, but this type adds nothing to the tree
        var pDefault = Parsewisp.parser(Gr.grammar("S", eps));
        Assertions.assertEquals(PT.create("S"), pDefault.parse(""));

        // With explicit show, also not hidden, but this type adds nothing to the tree
        var pShow = Parsewisp.parser(Gr.grammar("S", eps.withHideTag(false)));
        Assertions.assertEquals(PT.create("S"), pShow.parse(""));

        // With explicit hide
        var pHide = Parsewisp.parser(Gr.grammar("S", eps.withHideTag(true)));
        Assertions.assertEquals(PT.create("S"), pHide.parse(""));
    }

    @Test
    void withReduction() {
        // Default.
        Assertions.assertEquals(
                eps,
                eps.withReduction(ReductionType.standardInitialReduction()));

        // Default: Don't merge up
        var pDefault = Parsewisp.parser(
                Gr.grammarS("S",
                        "S", NonTerminal.create(Sym.sym("A")),
                        "A", eps));
        Assertions.assertEquals(PT.create("S", PT.create("A")), pDefault.parse(""));

        // Merge upwards
        var pMerge = Parsewisp.parser(
                Gr.grammarS("S",
                        "S", NonTerminal.create(Sym.sym("A")),
                        "A", eps.withReduction(ReductionType.standardIntermediateReduction())));
        Assertions.assertEquals(PT.create("S"), pMerge.parse(""));

        // Add to parse tree with potentially different name (Top-level)
        var pMerge2 = Parsewisp.parser(
                Gr.grammarS("S",
                        "S", eps.withReduction(ReductionType.taggedTreeReduction(Sym.sym("B")))));
        Assertions.assertEquals(PT.create("B"), pMerge2.parse(""));

        // Add to parse tree with potentially different name (Nested)
        var pMerge3 = Parsewisp.parser(
                Gr.grammarS("S",
                        "S", NonTerminal.create(Sym.sym("A")),
                        "A", eps.withReduction(ReductionType.taggedTreeReduction(Sym.sym("B")))));
        Assertions.assertEquals(PT.create("S", PT.create("B")), pMerge3.parse(""));

        // Add parse tree from a rule which doesn't appear in the grammar.
        var pMerge4 = Parsewisp.parser(
                Gr.grammarS("S",
                        "S", NonTerminal.create(Sym.sym("A")),
                        "A", ConcatRule.create(List.of(
                                eps.withReduction(ReductionType.taggedTreeReduction(Sym.sym("B"))),
                                eps
                        ))));
        Assertions.assertEquals(
                PT.create("S", PT.create("A", PT.create("B"))),
                pMerge4.parse(""));
    }
}