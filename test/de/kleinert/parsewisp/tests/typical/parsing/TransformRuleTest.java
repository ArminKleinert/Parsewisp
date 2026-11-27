package de.kleinert.parsewisp.tests.typical.parsing;

import de.kleinert.parsewisp.Parsewisp;
import de.kleinert.parsewisp.Sym;
import de.kleinert.parsewisp.parsing.*;
import de.kleinert.parsewisp.reduction.Reduction;
import de.kleinert.parsewisp.testutil.Gr;
import de.kleinert.parsewisp.testutil.PT;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

class TransformRuleTest {
    private final BiFunction<List<Object>, Integer, List<Object>> simple_f =
            (r, i) -> List.of(((String) r.get(0)).repeat(2));

    @Test
    void create() {
        Assertions.assertEquals(
                TransformRule.create(StringTerm.create("a", false), simple_f),
                TransformRule.create(StringTerm.create("a", false), simple_f));
    }

    @Test
    void parse() {
        var p = Parsewisp.parser(Gr.grammar(
                "S", ConcatRule.create(List.of(
                        TransformRule.create(
                                StringTerm.create("abc", false),
                                (r, i) -> Arrays.asList(((String) r.get(0)).split(""))),
                        StringTerm.create("def", false)))
        ));
        Assertions.assertEquals(
                PT.create("S", "a", "b", "c", "def"),
                p.parse("abcdef")
        );
    }

    @Test
    void fullParseSingle() {
        var p = Parsewisp.parser(Gr.grammar(
                "S", TransformRule.create(
                        StringTerm.create("abc", false),
                        (r, i) -> List.of("a", "b", "c")
                )));
        Assertions.assertEquals(
                PT.create("S", "a", "b", "c"),
                p.parse("abc")
        );
    }

    @Test
    void fullParseConcat() {
        var p = Parsewisp.parser(Gr.grammar(
                "S", ConcatRule.create(List.of(
                        StringTerm.create("abc", false),
                        TransformRule.create(
                                StringTerm.create("def", false),
                                (r, i) -> Arrays.asList(((String) r.get(0)).split("")))))
        ));
        Assertions.assertEquals(
                PT.create("S", "abc", "d", "e", "f"),
                p.parse("abcdef")
        );
    }

    @Test
    void getRule() {
        var inner = StringTerm.create("a", false);
        var r = (TransformRule) TransformRule.create(inner, simple_f);
        Assertions.assertEquals(inner, r.getRule());
    }

    @Test
    void withRule() {
        var i = StringTerm.create("a", false);
        var r = (TransformRule) TransformRule.create(i, simple_f);

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
        var r = (TransformRule) TransformRule.create(i, simple_f)
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
        var r = (TransformRule) TransformRule.create(StringTerm.create("a", false), simple_f);

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
        final TransformRule simple_a = (TransformRule) TransformRule.create(
                StringTerm.createDirect("a", false), simple_f);

        // By default, not hidden in output
        var pDefault = Parsewisp.parser(
                Gr.grammar("S", simple_a));
        Assertions.assertEquals(PT.create("S", "aa"), pDefault.parse("a"));

        // With explicit show, also not hidden
        var pShow = Parsewisp.parser(
                Gr.grammar("S", simple_a.withHideTag(false)));
        Assertions.assertEquals(PT.create("S", "aa"), pShow.parse("a"));

        // With explicit hide
        var pHide = Parsewisp.parser(
                Gr.grammar("S", simple_a.withHideTag(true)));
        Assertions.assertEquals(PT.create("S"), pHide.parse("a"));
    }

    @Test
    void withReduction() {
        final TransformRule simple_a = (TransformRule) TransformRule.create(
                StringTerm.createDirect("a", false), simple_f);

        // Default.
        Assertions.assertEquals(
                simple_a,
                simple_a.withReduction(Reduction.initial()));

        // Default: Don't merge up
        var pDefault = Parsewisp.parser(
                Gr.grammarS("S",
                        "S", NonTerminal.create(Sym.sym("A")),
                        "A", simple_a));
        Assertions.assertEquals(PT.create("S", PT.create("A", "aa")), pDefault.parse("a"));
    }

    @Test
    void withReductionMergeUp() {
        final TransformRule simple_a = (TransformRule) TransformRule.create(
                StringTerm.createDirect("a", false), simple_f);

        // Merge upwards
        var pMerge = Parsewisp.parser(
                Gr.grammarS("S",
                        "S", NonTerminal.create(Sym.sym("A")),
                        "A", simple_a.withReduction(Reduction.merging())));
        Assertions.assertEquals(PT.create("S", "aa"), pMerge.parse("a"));
    }

    @Test
    void withReductionTaggedTree() {
        final TransformRule simple_a = (TransformRule) TransformRule.create(
                StringTerm.createDirect("a", false), simple_f);

        // Add to parse tree with potentially different name (Top-level)
        var pMerge2 = Parsewisp.parser(
                Gr.grammarS("S",
                        "S", simple_a.withReduction(Reduction.taggedTree(Sym.sym("B")))));
        Assertions.assertEquals(PT.create("B", "aa"), pMerge2.parse("a"));

        // Add to parse tree with potentially different name (Nested)
        var pMerge3 = Parsewisp.parser(
                Gr.grammarS("S",
                        "S", NonTerminal.create(Sym.sym("A")),
                        "A", simple_a.withReduction(Reduction.taggedTree(Sym.sym("B")))));
        Assertions.assertEquals(PT.create("S", PT.create("B", "aa")), pMerge3.parse("a"));
    }

    @Test
    void withReductionNotDirectRhs() {
        final TransformRule simple_a = (TransformRule) TransformRule.create(
                StringTerm.createDirect("a", false), simple_f);

        // Add parse tree from a rule which doesn't appear in the grammar.
        var pMerge4 = Parsewisp.parser(
                Gr.grammarS("S",
                        "S", NonTerminal.create(Sym.sym("A")),
                        "A", ConcatRule.create(List.of(
                                simple_a.withReduction(Reduction.taggedTree(Sym.sym("B"))),
                                StringTerm.createDirect("b", false)
                        ))));
        Assertions.assertEquals(
                PT.create("S", PT.create("A", PT.create("B", "aa"), "b")),
                pMerge4.parse("ab"));
    }
}