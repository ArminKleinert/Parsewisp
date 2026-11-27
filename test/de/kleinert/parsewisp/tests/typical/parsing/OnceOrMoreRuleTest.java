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

class OnceOrMoreRuleTest {
    private OnceOrMoreRule rep_a() {
        return (OnceOrMoreRule) OnceOrMoreRule.create(StringTerm.createDirect("a", false));
    }

    private Rule string_a() {
        return StringTerm.createDirect("a", false);
    }

    @Test
    void create() {
        // If the rule is a EpsilonTerm, the #create(...) method is free to create something other than a OnceOrMoreRule,
        // but the same input still must mean equivalent output.
        Assertions.assertEquals(
                OnceOrMoreRule.create(EpsilonTerm.getDefault()),
                OnceOrMoreRule.create(EpsilonTerm.getDefault())
        );

        // If the rule is a OnceOrMoreRule, the #create(...) method is free to not create a new rule,
        // but the same input still must mean equivalent output.
        Assertions.assertEquals(
                OnceOrMoreRule.create(OnceOrMoreRule.create(StringTerm.createDirect("a", false))),
                OnceOrMoreRule.create(OnceOrMoreRule.create(StringTerm.createDirect("a", false))));

        // As expected.
        Assertions.assertEquals(
                OnceOrMoreRule.create(StringTerm.createDirect("a", false)),
                OnceOrMoreRule.create(StringTerm.createDirect("a", false)));
    }

    @Test
    void parse() {
        var p = Parsewisp.parser(Gr.grammar("S", ConcatRule.create(List.of(rep_a(), string_a()))));
        Assertions.assertEquals(PT.create("S", "a", "a"), p.parse("aa"));
        Assertions.assertEquals(PT.create("S", "a", "a", "a"), p.parse("aaa"));

        var pN = Parsewisp.parser(Gr.grammar("S", ConcatRule.create(List.of(string_a(), rep_a(), string_a()))));
        Assertions.assertEquals(PT.create("S", "a", "a", "a"), pN.parse("aaa"));
        Assertions.assertEquals(PT.create("S", "a", "a", "a", "a", "a"), pN.parse("aaaaa"));

        var pM = Parsewisp.parser(Gr.grammar(
                "S", ConcatRule.create(List.of(rep_a(), rep_a(), rep_a(), rep_a(), rep_a(), string_a()))));
        Assertions.assertEquals(PT.create("S", "a", "a", "a", "a", "a", "a"), pM.parse("aaaaaa"));
        Assertions.assertEquals(PT.create("S", "a", "a", "a", "a", "a", "a", "a", "a", "a"), pM.parse("aaaaaaaaa"));
    }

    @Test
    void parseError() {
        var p = Parsewisp.parser(Gr.grammar("S", ConcatRule.create(List.of(rep_a(), string_a()))));
        Assertions.assertTrue(p.parse("a").isFailure());
        Assertions.assertTrue(p.parse("ba").isFailure());
    }

    @Test
    void fullParseSingle() {
        var p = Parsewisp.parser(Gr.grammar("S", rep_a()));
        Assertions.assertEquals(PT.create("S", "a"), p.parse("a"));
        Assertions.assertEquals(PT.create("S", "a", "a", "a"), p.parse("aaa"));
    }

    @Test
    void fullParseConcat() {
        var pN = Parsewisp.parser(Gr.grammar("S", ConcatRule.create(List.of(string_a(), rep_a()))));
        Assertions.assertEquals(PT.create("S", "a", "a"), pN.parse("aa"));
        Assertions.assertEquals(PT.create("S", "a", "a", "a"), pN.parse("aaa"));

        var pM = Parsewisp.parser(Gr.grammar("S", ConcatRule.create(List.of(rep_a(), rep_a()))));
        Assertions.assertEquals(PT.create("S", "a", "a"), pM.parse("aa"));
        Assertions.assertEquals(PT.create("S", "a", "a", "a"), pM.parse("aaa"));
    }

    @Test
    void fullParseError() {
        var p = Parsewisp.parser(Gr.grammar("S", rep_a()));
        Assertions.assertTrue(p.parse("b").isFailure());
    }

    @Test
    void getRule() {
        var r = (OnceOrMoreRule) OnceOrMoreRule.create(string_a());
        Assertions.assertEquals(string_a(), r.getRule());
    }

    @Test
    void withRule() {
        var i = string_a();
        var r = (OnceOrMoreRule) OnceOrMoreRule.create(i);

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
        var r = (OnceOrMoreRule) OnceOrMoreRule
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
        // By default, not hidden in result. May create a new object, but the output must be equal to the original.
        Assertions.assertEquals(rep_a(), rep_a().withHideTag(false));
        Assertions.assertEquals(rep_a(), rep_a().includeInResult());

        // Same input -> Equivalent output
        Assertions.assertEquals(rep_a().withHideTag(false), rep_a().withHideTag(false));
        Assertions.assertEquals(rep_a().withHideTag(true), rep_a().withHideTag(true));

        // Alternative names give same output for isHiddenInResult()
        Assertions.assertFalse(rep_a().withHideTag(false).isHiddenInResult());
        Assertions.assertFalse(rep_a().includeInResult().isHiddenInResult());
        Assertions.assertTrue(rep_a().withHideTag(true).isHiddenInResult());
        Assertions.assertTrue(rep_a().hideInResult().isHiddenInResult());

        // Alternative names give equivalent output
        Assertions.assertEquals(rep_a().withHideTag(false), rep_a().includeInResult());
        Assertions.assertEquals(rep_a().withHideTag(true), rep_a().hideInResult());
    }

    @Test
    void withHideTagParse() {
        // By default, not hidden in output
        var pDefault = Parsewisp.parser(
                Gr.grammar("S", rep_a()));
        Assertions.assertEquals(PT.create("S", "a"), pDefault.parse("a"));

        // With explicit show, also not hidden
        var pShow = Parsewisp.parser(
                Gr.grammar("S", rep_a().withHideTag(false)));
        Assertions.assertEquals(PT.create("S", "a"), pShow.parse("a"));

        // With explicit hide
        var pHide = Parsewisp.parser(
                Gr.grammar("S", rep_a().withHideTag(true)));
        Assertions.assertEquals(PT.create("S"), pHide.parse("a"));
    }

    @Test
    void withReduction() {
        // Default.
        Assertions.assertEquals(
                rep_a(),
                rep_a().withReduction(Reduction.initial()));

        // Default: Don't merge up
        var pDefault = Parsewisp.parser(
                Gr.grammarS("S",
                        "S", NonTerminal.create(Sym.sym("A")),
                        "A", rep_a()));
        Assertions.assertEquals(PT.create("S", PT.create("A", "a")), pDefault.parse("a"));
    }

    @Test
    void withReductionMergeUp() {
        // Merge upwards
        var pMerge = Parsewisp.parser(
                Gr.grammarS("S",
                        "S", NonTerminal.create(Sym.sym("A")),
                        "A", rep_a().withReduction(Reduction.merging())));
        Assertions.assertEquals(PT.create("S", "a"), pMerge.parse("a"));
    }

    @Test
    void withReductionTaggedTree() {
        // Add to parse tree with potentially different name (Top-level)
        var pMerge2 = Parsewisp.parser(
                Gr.grammarS("S",
                        "S", rep_a().withReduction(Reduction.taggedTree(Sym.sym("B")))));
        Assertions.assertEquals(PT.create("B", "a"), pMerge2.parse("a"));

        // Add to parse tree with potentially different name (Nested)
        var pMerge3 = Parsewisp.parser(
                Gr.grammarS("S",
                        "S", NonTerminal.create(Sym.sym("A")),
                        "A", rep_a().withReduction(Reduction.taggedTree(Sym.sym("B")))));
        Assertions.assertEquals(PT.create("S", PT.create("B", "a")), pMerge3.parse("a"));
    }

    @Test
    void withReductionNotDirectRhs() {
        // Add parse tree from a rule which doesn't appear in the grammar.
        var pMerge4 = Parsewisp.parser(
                Gr.grammarS("S",
                        "S", NonTerminal.create(Sym.sym("A")),
                        "A", ConcatRule.create(List.of(
                                rep_a().withReduction(Reduction.taggedTree(Sym.sym("B"))),
                                OptionalRule.create(StringTerm.create("b", false))
                        ))));
        Assertions.assertEquals(
                PT.create("S", PT.create("A", PT.create("B", "a"), "b")),
                pMerge4.parse("ab"));
    }
}