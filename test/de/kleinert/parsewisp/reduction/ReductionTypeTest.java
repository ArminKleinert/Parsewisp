package de.kleinert.parsewisp.reduction;

import de.kleinert.parsewisp.Parsewisp;
import de.kleinert.parsewisp.Sym;
import de.kleinert.parsewisp.parsing.ConcatRule;
import de.kleinert.parsewisp.parsing.NonTerminal;
import de.kleinert.parsewisp.parsing.StringTerm;
import de.kleinert.parsewisp.testutil.Gr;
import de.kleinert.parsewisp.testutil.PT;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReductionTypeTest {

    @Test
    void standardIntermediateReduction() {
        // Merge upwards
        var pMerge = Parsewisp.parser(
                Gr.grammarS("S",
                        "S", NonTerminal.create(Sym.sym("A")),
                        "A", StringTerm.createDirect("a", false)
                                .withReduction(ReductionType.standardIntermediateReduction())));
        Assertions.assertEquals(PT.create("S", "a"), pMerge.parse("a"));
    }

    @Test
    void standardInitialReduction() {
        // Default: Don't merge up
        var pDefault = Parsewisp.parser(
                Gr.grammarS("S",
                        "S", NonTerminal.create(Sym.sym("A")),
                        "A", StringTerm.createDirect("a", false)));
        Assertions.assertEquals(PT.create("S", PT.create("A", "a")), pDefault.parse("a"));

        // Same as default, but explicit
        var pMerge = Parsewisp.parser(
                Gr.grammarS("S",
                        "S", NonTerminal.create(Sym.sym("A")),
                        "A", StringTerm.createDirect("a", false)
                                .withReduction(ReductionType.standardInitialReduction())));
        Assertions.assertEquals(PT.create("S", "a"), pMerge.parse("a"));
    }

    @Test
    void taggedTreeReduction() {
        // Add to parse tree with potentially different name (Top-level)
        var pMerge2 = Parsewisp.parser(
                Gr.grammarS("S",
                        "S", StringTerm.createDirect("a", false)
                                .withReduction(ReductionType.taggedTreeReduction(Sym.sym("B")))));
        Assertions.assertEquals(PT.create("B", "a"), pMerge2.parse("a"));

        // Add to parse tree with potentially different name (Nested)
        var pMerge3 = Parsewisp.parser(
                Gr.grammarS("S",
                        "S", NonTerminal.create(Sym.sym("A")),
                        "A", StringTerm.createDirect("a", false)
                                .withReduction(ReductionType.taggedTreeReduction(Sym.sym("B")))));
        Assertions.assertEquals(PT.create("S", PT.create("B", "a")), pMerge3.parse("a"));

        // Create a parse tree which wouldn't be obvious from the grammar.
        var pMerge4 = Parsewisp.parser(
                Gr.grammarS("S",
                        "S", NonTerminal.create(Sym.sym("A")),
                        "A", ConcatRule.create(List.of(
                                StringTerm.createDirect("a", false)
                                        .withReduction(ReductionType.taggedTreeReduction(Sym.sym("B"))),
                                StringTerm.createDirect("b", false)
                        ))));
        Assertions.assertEquals(
                PT.create("S", PT.create("A", PT.create("B", "a"), "b")),
                pMerge4.parse("ab"));
    }
}