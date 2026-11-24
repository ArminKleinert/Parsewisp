package de.kleinert.parsewisp.tests.typical.parsing;

import de.kleinert.parsewisp.Parsewisp;
import de.kleinert.parsewisp.Sym;
import de.kleinert.parsewisp.parsing.ConcatRule;
import de.kleinert.parsewisp.parsing.NonTerminal;
import de.kleinert.parsewisp.parsing.StringTerm;
import de.kleinert.parsewisp.parsing.ValueRangeTerm;
import de.kleinert.parsewisp.reduction.ReductionType;
import de.kleinert.parsewisp.testutil.Gr;
import de.kleinert.parsewisp.testutil.PT;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class ValueRangeTermTest {
    @Test
    void create() {
        Assertions.assertEquals(
                ValueRangeTerm.create(0, 255),
                ValueRangeTerm.create(0, 255));
    }

    @Test
    void createFailure() {
        // Condition broken: lo <= hi
        Assertions.assertThrows(IllegalArgumentException.class, () -> ValueRangeTerm.create(66, 65));

        // Condition broken: lo >= 0
        Assertions.assertThrows(IllegalArgumentException.class, () -> ValueRangeTerm.create(-1, 255));

        // Condition broken: hi <= 0x10FFFF
        Assertions.assertThrows(IllegalArgumentException.class, () -> ValueRangeTerm.create(0, 0x110000));
    }

    @Test
    void parse() {
        final ValueRangeTerm simple_a = (ValueRangeTerm) ValueRangeTerm.create('a', 'a');
        final ValueRangeTerm fullUnicode = (ValueRangeTerm) ValueRangeTerm.create(0, 0x10FFFF);

        // Case-sensitive.
        // Will use parse(...) for "a" and fullParse(...) for "b"
        var p = Parsewisp.parser(Gr.grammar("S", ConcatRule.create(List.of(simple_a, fullUnicode))));
        Assertions.assertEquals(PT.create("S", "a", "b"), p.parse("ab"));
    }

    @Test
    void parseError() {
        final ValueRangeTerm simple_a = (ValueRangeTerm) ValueRangeTerm.create('a', 'a');
        final ValueRangeTerm fullUnicode = (ValueRangeTerm) ValueRangeTerm.create(0, 0x10FFFF);

        // Will use parse(...) for "a" and fullParse(...) for "b"
        var p = Parsewisp.parser(Gr.grammar("S", ConcatRule.create(List.of(simple_a, fullUnicode))));
        Assertions.assertTrue(p.parse("b").isFailure());
    }

    @Test
    void fullParseSingle() {
        final ValueRangeTerm simple_a = (ValueRangeTerm) ValueRangeTerm.create('a', 'a');

        // Case-sensitive.
        var p = Parsewisp.parser(Gr.grammar("S", simple_a));
        Assertions.assertEquals(PT.create("S", "a"), p.parse("a"));
    }

    @Test
    void fullParseConcat() {
        final ValueRangeTerm simple_a = (ValueRangeTerm) ValueRangeTerm.create('a', 'a');
        final ValueRangeTerm fullUnicode = (ValueRangeTerm) ValueRangeTerm.create(0, 0x10FFFF);

        var p = Parsewisp.parser(Gr.grammar(
                "S", ConcatRule.create(List.of(simple_a, fullUnicode))));
        Assertions.assertEquals(PT.create("S", "a", "b"), p.parse("ab"));
    }

    @Test
    void fullParseError() {
        final ValueRangeTerm simple_a = (ValueRangeTerm) ValueRangeTerm.create('a', 'a');

        var p = Parsewisp.parser(Gr.grammar("S", simple_a)); // Will use parse(...) for "a" and fullParse(...) for "b"
        Assertions.assertTrue(p.parse("").isFailure());
    }

    @Test
    void getLo() {
        final ValueRangeTerm r1 = (ValueRangeTerm) ValueRangeTerm.create(0, 0x10FFFF);
        final ValueRangeTerm r2 = (ValueRangeTerm) ValueRangeTerm.create(0x10FFFF, 0x10FFFF);
        Assertions.assertEquals(0, r1.getLo());
        Assertions.assertEquals(0x10FFFF, r2.getLo());
    }

    @Test
    void getHi() {
        final ValueRangeTerm r1 = (ValueRangeTerm) ValueRangeTerm.create(0, 0x10FFFF);
        final ValueRangeTerm r2 = (ValueRangeTerm) ValueRangeTerm.create(0, 0);
        Assertions.assertEquals(0x10FFFF, r1.getHi());
        Assertions.assertEquals(0, r2.getHi());
    }

    @Test
    void withHideTag() {
        final ValueRangeTerm simpleRuleForTests = (ValueRangeTerm) ValueRangeTerm.create('a', 'z');

        // By default, not hidden in result. May create a new object, but the output must be equal to the original.
        Assertions.assertEquals(simpleRuleForTests, simpleRuleForTests.withHideTag(false));
        Assertions.assertEquals(simpleRuleForTests, simpleRuleForTests.includeInResult());

        // Same input -> Equivalent output
        Assertions.assertEquals(simpleRuleForTests.withHideTag(false), simpleRuleForTests.withHideTag(false));
        Assertions.assertEquals(simpleRuleForTests.withHideTag(true), simpleRuleForTests.withHideTag(true));

        // Alternative names give same output for isHiddenInResult()
        Assertions.assertFalse(simpleRuleForTests.withHideTag(false).isHiddenInResult());
        Assertions.assertFalse(simpleRuleForTests.includeInResult().isHiddenInResult());
        Assertions.assertTrue(simpleRuleForTests.withHideTag(true).isHiddenInResult());
        Assertions.assertTrue(simpleRuleForTests.hideInResult().isHiddenInResult());

        // Alternative names give equivalent output
        Assertions.assertEquals(simpleRuleForTests.withHideTag(false), simpleRuleForTests.includeInResult());
        Assertions.assertEquals(simpleRuleForTests.withHideTag(true), simpleRuleForTests.hideInResult());
    }

    @Test
    void withHideTagParse() {
        final ValueRangeTerm simple_a = (ValueRangeTerm) ValueRangeTerm.create('a', 'a');

        // By default, not hidden in output
        var pDefault = Parsewisp.parser(
                Gr.grammar("S", simple_a));
        Assertions.assertEquals(PT.create("S", "a"), pDefault.parse("a"));

        // With explicit show, also not hidden
        var pShow = Parsewisp.parser(
                Gr.grammar("S", simple_a.withHideTag(false)));
        Assertions.assertEquals(PT.create("S", "a"), pShow.parse("a"));

        // With explicit hide
        var pHide = Parsewisp.parser(
                Gr.grammar("S", simple_a.withHideTag(true)));
        Assertions.assertEquals(PT.create("S"), pHide.parse("a"));
    }

    @Test
    void withReduction() {
        final ValueRangeTerm simple_a = (ValueRangeTerm) ValueRangeTerm.create('a', 'a');

        // Default.
        Assertions.assertEquals(
                simple_a,
                simple_a.withReduction(ReductionType.initial()));

        // Default: Don't merge up
        var pDefault = Parsewisp.parser(
                Gr.grammarS("S",
                        "S", NonTerminal.create(Sym.sym("A")),
                        "A", simple_a));
        Assertions.assertEquals(PT.create("S", PT.create("A", "a")), pDefault.parse("a"));
    }

    @Test
    void withReductionMergeUp() {
        final ValueRangeTerm simple_a = (ValueRangeTerm) ValueRangeTerm.create('a', 'a');

        // Merge upwards
        var pMerge = Parsewisp.parser(
                Gr.grammarS("S",
                        "S", NonTerminal.create(Sym.sym("A")),
                        "A", simple_a.withReduction(ReductionType.merging())));
        Assertions.assertEquals(PT.create("S", "a"), pMerge.parse("a"));
    }

    @Test
    void withReductionTaggedTree() {
        final ValueRangeTerm simple_a = (ValueRangeTerm) ValueRangeTerm.create('a', 'a');

        // Add to parse tree with potentially different name (Top-level)
        var pMerge2 = Parsewisp.parser(
                Gr.grammarS("S",
                        "S", simple_a.withReduction(ReductionType.taggedTree(Sym.sym("B")))));
        Assertions.assertEquals(PT.create("B", "a"), pMerge2.parse("a"));

        // Add to parse tree with potentially different name (Nested)
        var pMerge3 = Parsewisp.parser(
                Gr.grammarS("S",
                        "S", NonTerminal.create(Sym.sym("A")),
                        "A", simple_a.withReduction(ReductionType.taggedTree(Sym.sym("B")))));
        Assertions.assertEquals(PT.create("S", PT.create("B", "a")), pMerge3.parse("a"));
    }

    @Test
    void withReductionNotDirectRhs() {
        final ValueRangeTerm simple_a = (ValueRangeTerm) ValueRangeTerm.create('a', 'a');

        // Add parse tree from a rule which doesn't appear in the grammar.
        var pMerge4 = Parsewisp.parser(
                Gr.grammarS("S",
                        "S", NonTerminal.create(Sym.sym("A")),
                        "A", ConcatRule.create(List.of(
                                simple_a.withReduction(ReductionType.taggedTree(Sym.sym("B"))),
                                StringTerm.createDirect("b", false)
                        ))));
        Assertions.assertEquals(
                PT.create("S", PT.create("A", PT.create("B", "a"), "b")),
                pMerge4.parse("ab"));
    }
}