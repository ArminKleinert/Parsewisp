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

class OptionalRuleTest {
    private OptionalRule a_or_nay() {
        return (OptionalRule) OptionalRule.create(StringTerm.createDirect("a", false));
    }

    private Rule string_a() {
        return StringTerm.createDirect("a", false);
    }

    @Test
    void create() {
        // If the rule is a EpsilonTerm, the #create(...) method is free to create something other than a OptionalRule,
        // but the same input still must mean equivalent output.
        Assertions.assertEquals(
                OptionalRule.create(EpsilonTerm.getDefault()),
                OptionalRule.create(EpsilonTerm.getDefault())
        );

        // If the rule is a OptionalRule, the #create(...) method is free to not create a new rule,
        // but the same input still must mean equivalent output.
        Assertions.assertEquals(
                OptionalRule.create(OptionalRule.create(StringTerm.createDirect("a", false))),
                OptionalRule.create(OptionalRule.create(StringTerm.createDirect("a", false))));

        // As expected.
        Assertions.assertEquals(
                OptionalRule.create(StringTerm.createDirect("a", false)),
                OptionalRule.create(StringTerm.createDirect("a", false)));
    }

    @Test
    void parse() {
        var p = Parsewisp.parser(Gr.grammar("S", ConcatRule.create(List.of(a_or_nay(), string_a()))));
        Assertions.assertEquals(PT.create("S", "a"), p.parse("a"));
        Assertions.assertEquals(PT.create("S", "a", "a"), p.parse("aa"));

        var pN = Parsewisp.parser(Gr.grammar("S", ConcatRule.create(List.of(string_a(), a_or_nay(), string_a()))));
        Assertions.assertEquals(PT.create("S", "a", "a"), pN.parse("aa"));
        Assertions.assertEquals(PT.create("S", "a", "a", "a"), pN.parse("aaa"));

        var pM = Parsewisp.parser(Gr.grammar(
                "S", ConcatRule.create(List.of(a_or_nay(), a_or_nay(), a_or_nay(), a_or_nay(), a_or_nay(), string_a()))));
        Assertions.assertEquals(PT.create("S", "a", "a"), pM.parse("aa"));
    }

    // Never happens. Can only fail on full parse.
    // @Test void parseError() {}

    @Test
    void fullParseSingle() {
        var p = Parsewisp.parser(Gr.grammar("S", a_or_nay()));
        Assertions.assertEquals(PT.create("S"), p.parse(""));
        Assertions.assertEquals(PT.create("S", "a"), p.parse("a"));
    }

    @Test
    void fullParseConcat() {
        var pN = Parsewisp.parser(Gr.grammar("S", ConcatRule.create(List.of(string_a(), a_or_nay(), a_or_nay()))));
        Assertions.assertEquals(PT.create("S", "a"), pN.parse("a"));
        Assertions.assertEquals(PT.create("S", "a", "a"), pN.parse("aa"));
        Assertions.assertEquals(PT.create("S", "a", "a", "a"), pN.parse("aaa"));

        var pM = Parsewisp.parser(Gr.grammar("S", ConcatRule.create(List.of(a_or_nay(), a_or_nay(), a_or_nay()))));
        Assertions.assertEquals(PT.create("S", "a"), pM.parse("a"));
        Assertions.assertEquals(PT.create("S", "a", "a"), pM.parse("aa"));
        Assertions.assertEquals(PT.create("S", "a", "a", "a"), pM.parse("aaa"));
    }

    @Test
    void fullParseError() {
        var p = Parsewisp.parser(Gr.grammar("S", a_or_nay()));
        Assertions.assertTrue(p.parse("b").isFailure());
    }

    @Test
    void getRule() {
        var r = (OptionalRule) OptionalRule.create(string_a());
        Assertions.assertEquals(string_a(), r.getRule());
    }

    @Test
    void withRule() {
        var i = StringTerm.create("a", false);
        var r = (OptionalRule) OptionalRule
                .create(i)
                .withHideTag(true)
                .withReduction(ReductionType.taggedTree(Sym.sym("A")));

        // As expected.
        Assertions.assertEquals(i, r.getRule());

        // Create a new rule, check whether hide and reduction are still unchanged.
        var ni = StringTerm.create("b", false);
        var nr = r.withRule(ni);
        Assertions.assertEquals(ni, nr.getRule());

        // Original left untouched.
        Assertions.assertEquals(i, r.getRule());
    }

    @Test
    void withRulePreservesHideAndRed() {
        var i = StringTerm.create("a", false);
        var r = (OptionalRule) OptionalRule
                .create(i)
                .withHideTag(true)
                .withReduction(ReductionType.taggedTree(Sym.sym("A")));

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
        Assertions.assertEquals(a_or_nay(), a_or_nay().withHideTag(false));
        Assertions.assertEquals(a_or_nay(), a_or_nay().includeInResult());

        // Same input -> Equivalent output
        Assertions.assertEquals(a_or_nay().withHideTag(false), a_or_nay().withHideTag(false));
        Assertions.assertEquals(a_or_nay().withHideTag(true), a_or_nay().withHideTag(true));

        // Alternative names give same output for isHiddenInResult()
        Assertions.assertFalse(a_or_nay().withHideTag(false).isHiddenInResult());
        Assertions.assertFalse(a_or_nay().includeInResult().isHiddenInResult());
        Assertions.assertTrue(a_or_nay().withHideTag(true).isHiddenInResult());
        Assertions.assertTrue(a_or_nay().hideInResult().isHiddenInResult());

        // Alternative names give equivalent output
        Assertions.assertEquals(a_or_nay().withHideTag(false), a_or_nay().includeInResult());
        Assertions.assertEquals(a_or_nay().withHideTag(true), a_or_nay().hideInResult());
    }

    @Test
    void withHideTagParse() {
        // By default, not hidden in output
        var pDefault = Parsewisp.parser(
                Gr.grammar("S", a_or_nay()));
        Assertions.assertEquals(PT.create("S"), pDefault.parse(""));
        Assertions.assertEquals(PT.create("S", "a"), pDefault.parse("a"));

        // With explicit show, also not hidden
        var pShow = Parsewisp.parser(
                Gr.grammar("S", a_or_nay().withHideTag(false)));
        Assertions.assertEquals(PT.create("S"), pShow.parse(""));
        Assertions.assertEquals(PT.create("S", "a"), pShow.parse("a"));

        // With explicit hide
        var pHide = Parsewisp.parser(
                Gr.grammar("S", a_or_nay().withHideTag(true)));
        Assertions.assertEquals(PT.create("S"), pHide.parse(""));
        Assertions.assertEquals(PT.create("S"), pHide.parse("a"));
    }

    @Test
    void withReduction() {
        // Default.
        Assertions.assertEquals(
                a_or_nay(),
                a_or_nay().withReduction(ReductionType.initial()));

        // Default: Don't merge up
        var pDefault = Parsewisp.parser(
                Gr.grammarS("S",
                        "S", NonTerminal.create(Sym.sym("A")),
                        "A", a_or_nay()));
        Assertions.assertEquals(PT.create("S", PT.create("A")), pDefault.parse(""));
        Assertions.assertEquals(PT.create("S", PT.create("A", "a")), pDefault.parse("a"));
    }

    @Test
    void withReductionMergeUp() {
        // Merge upwards
        var pMerge = Parsewisp.parser(
                Gr.grammarS("S",
                        "S", NonTerminal.create(Sym.sym("A")),
                        "A", a_or_nay().withReduction(ReductionType.merging())));
        Assertions.assertEquals(PT.create("S"), pMerge.parse(""));
        Assertions.assertEquals(PT.create("S", "a"), pMerge.parse("a"));
    }

    @Test
    void withReductionTaggedTree() {
        // Add to parse tree with potentially different name (Top-level)
        var pMerge2 = Parsewisp.parser(
                Gr.grammarS("S",
                        "S", a_or_nay().withReduction(ReductionType.taggedTree(Sym.sym("B")))));
        Assertions.assertEquals(PT.create("B"), pMerge2.parse(""));
        Assertions.assertEquals(PT.create("B", "a"), pMerge2.parse("a"));

        // Add to parse tree with potentially different name (Nested)
        var pMerge3 = Parsewisp.parser(
                Gr.grammarS("S",
                        "S", NonTerminal.create(Sym.sym("A")),
                        "A", a_or_nay().withReduction(ReductionType.taggedTree(Sym.sym("B")))));
        Assertions.assertEquals(PT.create("S", PT.create("B")), pMerge3.parse(""));
        Assertions.assertEquals(PT.create("S", PT.create("B", "a")), pMerge3.parse("a"));
    }

    @Test
    void withReductionNotDirectRhs() {
        // Add parse tree from a rule which doesn't appear in the grammar.
        var pMerge4 = Parsewisp.parser(
                Gr.grammarS("S",
                        "S", NonTerminal.create(Sym.sym("A")),
                        "A", ConcatRule.create(List.of(
                                a_or_nay().withReduction(ReductionType.taggedTree(Sym.sym("B"))),
                                OptionalRule.create(StringTerm.create("b", false))
                        ))));
        Assertions.assertEquals(
                PT.create("S", PT.create("A", PT.create("B"), "b")),
                pMerge4.parse("b"));
        Assertions.assertEquals(
                PT.create("S", PT.create("A", PT.create("B", "a"), "b")),
                pMerge4.parse("ab"));
    }
}