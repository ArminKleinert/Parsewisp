package de.kleinert.parsewisp.tests.typical.parsing;

import de.kleinert.parsewisp.Parsewisp;
import de.kleinert.parsewisp.Sym;
import de.kleinert.parsewisp.parsing.ConcatRule;
import de.kleinert.parsewisp.parsing.NonTerminal;
import de.kleinert.parsewisp.parsing.SpecialSequenceRule;
import de.kleinert.parsewisp.parsing.StringTerm;
import de.kleinert.parsewisp.reduction.ReductionType;
import de.kleinert.parsewisp.testutil.Gr;
import de.kleinert.parsewisp.testutil.PT;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

class SpecialSequenceRuleTest {
    private SpecialSequenceRule simpleRuleForTests() {
        return (SpecialSequenceRule) SpecialSequenceRule.create("match \"a\"",
                (s) -> s.startsWith("a") ? Optional.of("a") : Optional.empty());
    }

    private SpecialSequenceRule simple_a() {
        return simpleRuleForTests();
    }

    private SpecialSequenceRule simple_b() {
        return (SpecialSequenceRule) SpecialSequenceRule.create("match \"b\"",
                (s) -> s.startsWith("b") ? Optional.of("b") : Optional.empty());
    }

    @Test
    void getDescription() {
    }

    @Test
    void getFunction() {
        Function<String, Optional<String>> f =
                (s) -> s.startsWith("a") ? Optional.of("a") : Optional.empty();
        Assertions.assertEquals(
                f,
                ((SpecialSequenceRule) SpecialSequenceRule.create("match \"a\"", f)).getFunction());
    }

    @Test
    void create() {
        Function<String, Optional<String>> f =
                (s) -> s.startsWith("a") ? Optional.of("a") : Optional.empty();

        Assertions.assertEquals(
                SpecialSequenceRule.create("match \"a\"", f),
                SpecialSequenceRule.create("match \"a\"", f));

        Function<String, Optional<String>> f2 =
                (s) -> !s.startsWith("a") ? Optional.empty() : Optional.of("a");

        Assertions.assertNotEquals(
                SpecialSequenceRule.create("match \"a\"", f),
                SpecialSequenceRule.create("match \"a\"", f2));
    }

    @Test
    void parse() {
        var p = Parsewisp.parser(Gr.grammar("S", ConcatRule.create(List.of(simple_a(), simple_b()))));
        Assertions.assertEquals(PT.create("S", "a", "b"), p.parse("ab"));
    }

    @Test
    void parseError() {
        var p = Parsewisp.parser(Gr.grammar("S", ConcatRule.create(List.of(simple_a(), simple_b()))));
        Assertions.assertTrue(p.parse("b").isFailure());
    }

    @Test
    void fullParseSingle() {
        var p = Parsewisp.parser(Gr.grammar("S", simple_a()));
        Assertions.assertEquals(PT.create("S", "a"), p.parse("a"));
    }

    @Test
    void fullParseConcat() {
        var p = Parsewisp.parser(Gr.grammar(
                "S", ConcatRule.create(List.of(simple_a(), simple_b()))));
        Assertions.assertEquals(PT.create("S", "a", "b"), p.parse("ab"));
    }

    @Test
    void fullParseError() {
        var p = Parsewisp.parser(Gr.grammar("S", simple_a()));
        Assertions.assertTrue(p.parse("").isFailure());
    }

    @Test
    void withHideTag() {
        // By default, not hidden in result. May create a new object, but the output must be equal to the original.
        Assertions.assertEquals(simpleRuleForTests(), simpleRuleForTests().withHideTag(false));
        Assertions.assertEquals(simpleRuleForTests(), simpleRuleForTests().includeInResult());

        // Same input -> Equivalent output
        Assertions.assertEquals(simpleRuleForTests().withHideTag(false), simpleRuleForTests().withHideTag(false));
        Assertions.assertEquals(simpleRuleForTests().withHideTag(true), simpleRuleForTests().withHideTag(true));

        // Alternative names give same output for isHiddenInResult()
        Assertions.assertFalse(simpleRuleForTests().withHideTag(false).isHiddenInResult());
        Assertions.assertFalse(simpleRuleForTests().includeInResult().isHiddenInResult());
        Assertions.assertTrue(simpleRuleForTests().withHideTag(true).isHiddenInResult());
        Assertions.assertTrue(simpleRuleForTests().hideInResult().isHiddenInResult());

        // Alternative names give equivalent output
        Assertions.assertEquals(simpleRuleForTests().withHideTag(false), simpleRuleForTests().includeInResult());
        Assertions.assertEquals(simpleRuleForTests().withHideTag(true), simpleRuleForTests().hideInResult());
    }

    @Test
    void withHideTagParse() {
        // By default, not hidden in output
        var pDefault = Parsewisp.parser(
                Gr.grammar("S", simple_a()));
        Assertions.assertEquals(PT.create("S", "a"), pDefault.parse("a"));

        // With explicit show, also not hidden
        var pShow = Parsewisp.parser(
                Gr.grammar("S", simple_a().withHideTag(false)));
        Assertions.assertEquals(PT.create("S", "a"), pShow.parse("a"));

        // With explicit hide
        var pHide = Parsewisp.parser(
                Gr.grammar("S", simple_a().withHideTag(true)));
        Assertions.assertEquals(PT.create("S"), pHide.parse("a"));
    }

    @Test
    void withReduction() {
        // Default.
        Assertions.assertEquals(
                simpleRuleForTests(),
                simpleRuleForTests().withReduction(ReductionType.initial()));

        // Default: Don't merge up
        var pDefault = Parsewisp.parser(
                Gr.grammarS("S",
                        "S", NonTerminal.create(Sym.sym("A")),
                        "A", simple_a()));
        Assertions.assertEquals(PT.create("S", PT.create("A", "a")), pDefault.parse("a"));
    }

    @Test
    void withReductionMergeUp() {
        // Merge upwards
        var pMerge = Parsewisp.parser(
                Gr.grammarS("S",
                        "S", NonTerminal.create(Sym.sym("A")),
                        "A", simple_a().withReduction(ReductionType.merging())));
        Assertions.assertEquals(PT.create("S", "a"), pMerge.parse("a"));
    }

    @Test
    void withReductionTaggedTree() {
        // Add to parse tree with potentially different name (Top-level)
        var pMerge2 = Parsewisp.parser(
                Gr.grammarS("S",
                        "S", simple_a().withReduction(ReductionType.taggedTree(Sym.sym("B")))));
        Assertions.assertEquals(PT.create("B", "a"), pMerge2.parse("a"));

        // Add to parse tree with potentially different name (Nested)
        var pMerge3 = Parsewisp.parser(
                Gr.grammarS("S",
                        "S", NonTerminal.create(Sym.sym("A")),
                        "A", simple_a().withReduction(ReductionType.taggedTree(Sym.sym("B")))));
        Assertions.assertEquals(PT.create("S", PT.create("B", "a")), pMerge3.parse("a"));
    }

    @Test
    void withReductionNotDirectRhs() {
        // Add parse tree from a rule which doesn't appear in the grammar.
        var pMerge4 = Parsewisp.parser(
                Gr.grammarS("S",
                        "S", NonTerminal.create(Sym.sym("A")),
                        "A", ConcatRule.create(List.of(
                                simple_a().withReduction(ReductionType.taggedTree(Sym.sym("B"))),
                                StringTerm.createDirect("b", false)
                        ))));
        Assertions.assertEquals(
                PT.create("S", PT.create("A", PT.create("B", "a"), "b")),
                pMerge4.parse("ab"));
    }
}