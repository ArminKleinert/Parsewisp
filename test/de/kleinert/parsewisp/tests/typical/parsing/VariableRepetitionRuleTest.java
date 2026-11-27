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

class VariableRepetitionRuleTest {
    private VariableRepetitionRule rep_a14() {
        return (VariableRepetitionRule) VariableRepetitionRule.create(StringTerm.createDirect("a", false), 1, 4);
    }

    private Rule string_a() {
        return StringTerm.createDirect("a", false);
    }

    @Test
    void create() {
        // Epsilon as input may return Epsilon
        Assertions.assertEquals(
                VariableRepetitionRule.create(EpsilonTerm.getDefault(), 2, 4),
                VariableRepetitionRule.create(EpsilonTerm.getDefault(), 2, 4)
        );

        // Zero min and zero max -> may create epsilon
        Assertions.assertEquals(
                VariableRepetitionRule.create(string_a(), 0, 0),
                VariableRepetitionRule.create(string_a(), 0, 0));

        // 1 min and 1 max -> may return the rule itself
        Assertions.assertEquals(
                VariableRepetitionRule.create(string_a(), 1, 1),
                VariableRepetitionRule.create(string_a(), 1, 1));

        // The usual
        Assertions.assertEquals(
                VariableRepetitionRule.create(string_a(), 2, 4),
                VariableRepetitionRule.create(string_a(), 2, 4));

        // Exact
        Assertions.assertEquals(
                VariableRepetitionRule.create(string_a(), 4, 4),
                VariableRepetitionRule.create(string_a(), 4, 4));

        // Repetition of repetition may create a nested repetition or just a single one
        var r = VariableRepetitionRule.create(string_a(), 2, 4);
        Assertions.assertEquals(
                VariableRepetitionRule.create(r, 1, 3),
                VariableRepetitionRule.create(r, 1, 3));

        // Negative min
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> VariableRepetitionRule.create(string_a(), -4, 4));

        // Negative max
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> VariableRepetitionRule.create(string_a(), 4, -4));

        // min > max
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> VariableRepetitionRule.create(string_a(), 9, 1));
    }

    @Test
    void parse() {
        var p = Parsewisp.parser(Gr.grammar("S", ConcatRule.create(List.of(rep_a14(), string_a()))));
        Assertions.assertEquals(PT.create("S", "a", "a"), p.parse("aa"));
        Assertions.assertEquals(PT.create("S", "a", "a", "a"), p.parse("aaa"));
        Assertions.assertEquals(PT.create("S", "a", "a", "a", "a"), p.parse("aaaa"));
        Assertions.assertEquals(PT.create("S", "a", "a", "a", "a", "a"), p.parse("aaaaa"));
    }

    @Test
    void parseError() {
        var p = Parsewisp.parser(Gr.grammar("S", ConcatRule.create(List.of(rep_a14(), string_a()))));
        Assertions.assertTrue(p.parse("a").isFailure());
        Assertions.assertTrue(p.parse("aaaaaa").isFailure());
    }

    @Test
    void fullParseSingle() {
        var p = Parsewisp.parser(Gr.grammar("S", rep_a14()));
        Assertions.assertEquals(PT.create("S", "a"), p.parse("a"));
        Assertions.assertEquals(PT.create("S", "a", "a"), p.parse("aa"));
        Assertions.assertEquals(PT.create("S", "a", "a", "a"), p.parse("aaa"));
        Assertions.assertEquals(PT.create("S", "a", "a", "a", "a"), p.parse("aaaa"));
    }

    @Test
    void fullParseConcat() {
        var p = Parsewisp.parser(Gr.grammar("S", ConcatRule.create(List.of(string_a(), rep_a14()))));
        Assertions.assertEquals(PT.create("S", "a", "a"), p.parse("aa"));
        Assertions.assertEquals(PT.create("S", "a", "a", "a"), p.parse("aaa"));
        Assertions.assertEquals(PT.create("S", "a", "a", "a", "a"), p.parse("aaaa"));
        Assertions.assertEquals(PT.create("S", "a", "a", "a", "a", "a"), p.parse("aaaaa"));

        var pRR = Parsewisp.parser(Gr.grammar("S", ConcatRule.create(List.of(rep_a14(), rep_a14()))));
        Assertions.assertEquals(PT.create("S", "a", "a"), pRR.parse("aa"));
        Assertions.assertEquals(PT.create("S", "a", "a", "a"), pRR.parse("aaa"));
        Assertions.assertEquals(PT.create("S", "a", "a", "a", "a"), pRR.parse("aaaa"));
        Assertions.assertEquals(PT.create("S", "a", "a", "a", "a", "a"), pRR.parse("aaaaa"));
        Assertions.assertEquals(PT.create("S", "a", "a", "a", "a", "a", "a"), pRR.parse("aaaaaa"));
        Assertions.assertEquals(PT.create("S", "a", "a", "a", "a", "a", "a", "a"), pRR.parse("aaaaaaa"));
        Assertions.assertEquals(PT.create("S", "a", "a", "a", "a", "a", "a", "a", "a"), pRR.parse("aaaaaaaa"));
    }

    @Test
    void fullParseError() {
        var p = Parsewisp.parser(Gr.grammar("S", rep_a14()));
        Assertions.assertTrue(p.parse("").isFailure());
        Assertions.assertTrue(p.parse("aaaaa").isFailure());
    }

    @Test
    void getRule() {
        var r = (VariableRepetitionRule) VariableRepetitionRule.create(string_a(), 2, 4);
        Assertions.assertEquals(string_a(), r.getRule());
    }

    @Test
    void getMin() {
        Assertions.assertEquals(0,
                ((VariableRepetitionRule) VariableRepetitionRule.create(string_a(), 0, 1)).getMin());
        Assertions.assertEquals(0,
                ((VariableRepetitionRule) VariableRepetitionRule.create(string_a(), 0, 2)).getMin());
        Assertions.assertEquals(2,
                ((VariableRepetitionRule) VariableRepetitionRule.create(string_a(), 2, 4)).getMin());
        Assertions.assertEquals(4,
                ((VariableRepetitionRule) VariableRepetitionRule.create(string_a(), 4, 4)).getMin());
    }

    @Test
    void getMax() {
        Assertions.assertEquals(1,
                ((VariableRepetitionRule) VariableRepetitionRule.create(string_a(), 0, 1)).getMax());
        Assertions.assertEquals(2,
                ((VariableRepetitionRule) VariableRepetitionRule.create(string_a(), 0, 2)).getMax());
        Assertions.assertEquals(4,
                ((VariableRepetitionRule) VariableRepetitionRule.create(string_a(), 2, 4)).getMax());
        Assertions.assertEquals(4,
                ((VariableRepetitionRule) VariableRepetitionRule.create(string_a(), 4, 4)).getMax());
    }

    @Test
    void withRule() {
        var i = string_a();
        var r = (VariableRepetitionRule) VariableRepetitionRule.create(i, 1, 3);

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
        var r = (VariableRepetitionRule) VariableRepetitionRule
                .create(i, 1, 4)
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
        // By default, not hidden in result. May create a new object, but the output must be equal to the original.
        Assertions.assertEquals(rep_a14(), rep_a14().withHideTag(false));
        Assertions.assertEquals(rep_a14(), rep_a14().includeInResult());

        // Same input -> Equivalent output
        Assertions.assertEquals(rep_a14().withHideTag(false), rep_a14().withHideTag(false));
        Assertions.assertEquals(rep_a14().withHideTag(true), rep_a14().withHideTag(true));

        // Alternative names give same output for isHiddenInResult()
        Assertions.assertFalse(rep_a14().withHideTag(false).isHiddenInResult());
        Assertions.assertFalse(rep_a14().includeInResult().isHiddenInResult());
        Assertions.assertTrue(rep_a14().withHideTag(true).isHiddenInResult());
        Assertions.assertTrue(rep_a14().hideInResult().isHiddenInResult());

        // Alternative names give equivalent output
        Assertions.assertEquals(rep_a14().withHideTag(false), rep_a14().includeInResult());
        Assertions.assertEquals(rep_a14().withHideTag(true), rep_a14().hideInResult());
    }

    @Test
    void withHideTagParse() {
        // By default, not hidden in output
        var pDefault = Parsewisp.parser(
                Gr.grammar("S", rep_a14()));
        Assertions.assertEquals(PT.create("S", "a"), pDefault.parse("a"));

        // With explicit show, also not hidden
        var pShow = Parsewisp.parser(
                Gr.grammar("S", rep_a14().withHideTag(false)));
        Assertions.assertEquals(PT.create("S", "a"), pShow.parse("a"));

        // With explicit hide
        var pHide = Parsewisp.parser(
                Gr.grammar("S", rep_a14().withHideTag(true)));
        Assertions.assertEquals(PT.create("S"), pHide.parse("a"));
    }

    @Test
    void withReduction() {
        // Default.
        Assertions.assertEquals(
                rep_a14(),
                rep_a14().withReduction(Reduction.initial()));

        // Default: Don't merge up
        var pDefault = Parsewisp.parser(
                Gr.grammarS("S",
                        "S", NonTerminal.create(Sym.sym("A")),
                        "A", rep_a14()));
        Assertions.assertEquals(PT.create("S", PT.create("A", "a")), pDefault.parse("a"));
    }

    @Test
    void withReductionMergeUp() {
        // Merge upwards
        var pMerge = Parsewisp.parser(
                Gr.grammarS("S",
                        "S", NonTerminal.create(Sym.sym("A")),
                        "A", rep_a14().withReduction(Reduction.merging())));
        Assertions.assertEquals(PT.create("S", "a"), pMerge.parse("a"));
    }

    @Test
    void withReductionTaggedTree() {
        // Add to parse tree with potentially different name (Top-level)
        var pMerge2 = Parsewisp.parser(
                Gr.grammarS("S",
                        "S", rep_a14().withReduction(Reduction.taggedTree(Sym.sym("B")))));
        Assertions.assertEquals(PT.create("B", "a"), pMerge2.parse("a"));

        // Add to parse tree with potentially different name (Nested)
        var pMerge3 = Parsewisp.parser(
                Gr.grammarS("S",
                        "S", NonTerminal.create(Sym.sym("A")),
                        "A", rep_a14().withReduction(Reduction.taggedTree(Sym.sym("B")))));
        Assertions.assertEquals(PT.create("S", PT.create("B", "a")), pMerge3.parse("a"));
    }

    @Test
    void withReductionNotDirectRhs() {
        // Add parse tree from a rule which doesn't appear in the grammar.
        var pMerge4 = Parsewisp.parser(
                Gr.grammarS("S",
                        "S", NonTerminal.create(Sym.sym("A")),
                        "A", ConcatRule.create(List.of(
                                rep_a14().withReduction(Reduction.taggedTree(Sym.sym("B"))),
                                OptionalRule.create(StringTerm.create("b", false))
                        ))));
        Assertions.assertEquals(
                PT.create("S", PT.create("A", PT.create("B", "a"), "b")),
                pMerge4.parse("ab"));
    }
}