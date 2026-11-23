package de.kleinert.parsewisp.tests.typical.parsing;

import de.kleinert.parsewisp.Parsewisp;
import de.kleinert.parsewisp.Sym;
import de.kleinert.parsewisp.parsing.*;
import de.kleinert.parsewisp.reduction.ReductionType;
import de.kleinert.parsewisp.testutil.Gr;
import de.kleinert.parsewisp.testutil.PT;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

class AlternationRuleTest {
    private final Rule string_a = StringTerm.createDirect("a", false);
    private final Rule string_b = StringTerm.createDirect("b", false);

    @Test
    void create() {
        Assertions.assertEquals(
                AlternationRule.create(List.of(string_a, string_b)),
                AlternationRule.create(List.of(string_a, string_b)));
    }

    @Test
    void createEmpty() {
        // create(...) called with an empty input is free to create an Epsilon or something equivalent instead.
        Assertions.assertEquals(
                AlternationRule.create(List.of()),
                AlternationRule.create(List.of()));

        Assertions.assertEquals(
                Parsewisp.parser(Gr.grammar("S", EpsilonTerm.getDefault())).parse(""),
                Parsewisp.parser(Gr.grammar("S", AlternationRule.create(List.of()))).parse(""));
    }

    @Test
    void createOnlyOne() {
        // create(...) called with only one element in the input list, it may return only that one element.
        // This behavior is not mandatory.
        Assertions.assertEquals(
                AlternationRule.create(List.of(string_a)),
                AlternationRule.create(List.of(string_a)));

        Assertions.assertEquals(
                Parsewisp.parser(Gr.grammar("S", string_a)).parse("a"),
                Parsewisp.parser(Gr.grammar("S", AlternationRule.create(List.of(string_a)))).parse("a"));
    }

    @Test
    void createManySimilar() {
        // If an element in the input is repeated, create(...) can (and should) make the inputs distinct.
        var l = List.of(string_a, string_b, string_a, string_b, string_a, string_b, string_a, string_b);
        var lDistinct = l.stream().distinct().toList();
        Assertions.assertTrue(
                Objects.equals(AlternationRule.altUnsafe(lDistinct), AlternationRule.create(l))
                        || Objects.equals(AlternationRule.create(lDistinct), AlternationRule.create(l)));

        Assertions.assertEquals(
                Parsewisp.parser(Gr.grammar("S", AlternationRule.create(lDistinct))).parses("a"),
                Parsewisp.parser(Gr.grammar("S", AlternationRule.create(l))).parses("a"));
    }

    @Test
    void altUnsafe() {
        Assertions.assertEquals(
                AlternationRule.altUnsafe(List.of(string_a, string_a, string_a)),
                AlternationRule.altUnsafe(List.of(string_a, string_a, string_a)));
        Assertions.assertEquals(
                AlternationRule.create(List.of(string_a, string_b)),
                AlternationRule.altUnsafe(List.of(string_a, string_b)));

        Assertions.assertEquals(
                AlternationRule.altUnsafe(List.of()),
                AlternationRule.altUnsafe(List.of()));

        Assertions.assertEquals(
                AlternationRule.altUnsafe(List.of(EpsilonTerm.getDefault(), EpsilonTerm.getDefault())),
                AlternationRule.altUnsafe(List.of(EpsilonTerm.getDefault(), EpsilonTerm.getDefault())));
        Assertions.assertNotEquals(
                AlternationRule.altUnsafe(List.of(EpsilonTerm.getDefault())),
                AlternationRule.altUnsafe(List.of(EpsilonTerm.getDefault(), EpsilonTerm.getDefault())));
        Assertions.assertEquals(
                2,
                AlternationRule.altUnsafe(List.of(EpsilonTerm.getDefault(), EpsilonTerm.getDefault())).getRules().size());

        var flat = AlternationRule.altUnsafe(List.of(string_a, string_a, string_a, string_a));
        var nest1 = AlternationRule.altUnsafe(List.of(AlternationRule.altUnsafe(List.of(string_a, string_a)), string_a, string_a));
        var nest2 = AlternationRule.altUnsafe(List.of(string_a, AlternationRule.altUnsafe(List.of(string_a, string_a)), string_a));
        var nest3 = AlternationRule.altUnsafe(List.of(string_a, string_a, AlternationRule.altUnsafe(List.of(string_a, string_a))));
        Assertions.assertNotEquals(flat, nest1);
        Assertions.assertNotEquals(flat, nest2);
        Assertions.assertNotEquals(flat, nest3);
    }

    @Test
    void parse() {
        var p_ab = Parsewisp.parser(Gr.grammar("S", ConcatRule.create(List.of(AlternationRule.create(List.of(string_a, string_b)), string_a))));
        Assertions.assertEquals(PT.create("S", "a", "a"), p_ab.parse("aa"));
        Assertions.assertEquals(PT.create("S", "b", "a"), p_ab.parse("ba"));
    }

    @Test
    void parseRepeatedCasesDoNotMakeDuplicateTrees() {
        // Tests the grammar `S = "a" | "a" | "a"` on input "a". The expectation is that it creates exactly one tree, not three different ones.
        var p_a = Parsewisp.parser(Gr.grammar("S", ConcatRule.create(List.of(AlternationRule.create(List.of(string_a, string_a, string_a)), string_a))));
        Assertions.assertEquals(
                List.of(PT.create("S", "a", "a")),
                p_a.parses("aa"));
    }

    @Test
    void parseError() {
        var p_ab = Parsewisp.parser(Gr.grammar("S", ConcatRule.create(List.of(AlternationRule.create(List.of(string_a, string_b)), string_a))));
        Assertions.assertTrue(p_ab.parse("ca").isFailure());
    }

    @Test
    void fullParseSingle() {
        var p_ab = Parsewisp.parser(Gr.grammar("S", AlternationRule.create(List.of(string_a, string_b))));
        Assertions.assertEquals(PT.create("S", "a"), p_ab.parse("a"));
        Assertions.assertEquals(PT.create("S", "b"), p_ab.parse("b"));
    }

    @Test
    void fullParseConcat() {
        var p_ab = Parsewisp.parser(Gr.grammar("S", ConcatRule.create(List.of(string_a, AlternationRule.create(List.of(string_a, string_b))))));
        Assertions.assertEquals(PT.create("S", "a", "a"), p_ab.parse("aa"));
        Assertions.assertEquals(PT.create("S", "a", "b"), p_ab.parse("ab"));
    }

    @Test
    void fullParseRepeatedCasesDoNotMakeDuplicateTrees() {
        // Tests the grammar `S = "a" | "a" | "a"` on input "a". The expectation is that it creates exactly one tree, not three different ones.
        var p_a = Parsewisp.parser(Gr.grammar("S", AlternationRule.altUnsafe(List.of(string_a, string_a, string_a))));
        Assertions.assertEquals(
                List.of(PT.create("S", "a")),
                p_a.parses("a"));
    }

    @Test
    void fullParseError() {
        var p_ab = Parsewisp.parser(Gr.grammar("S", AlternationRule.create(List.of(string_a, string_b))));
        Assertions.assertTrue(p_ab.parse("c").isFailure());
    }

    @Test
    void getRules() {
        var flat = AlternationRule.altUnsafe(List.of(string_a, string_a, string_a, string_a));
        var nest1 = AlternationRule.altUnsafe(List.of(AlternationRule.altUnsafe(List.of(string_a, string_a)), string_a, string_a));
        var nest2 = AlternationRule.altUnsafe(List.of(string_a, AlternationRule.altUnsafe(List.of(string_a, string_a)), string_a));
        var nest3 = AlternationRule.altUnsafe(List.of(string_a, string_a, AlternationRule.altUnsafe(List.of(string_a, string_a))));

        Assertions.assertEquals(List.of(string_a, string_a, string_a, string_a), flat.getRules());
        Assertions.assertEquals(List.of(AlternationRule.altUnsafe(List.of(string_a, string_a)), string_a, string_a), nest1.getRules());
        Assertions.assertEquals(List.of(string_a, AlternationRule.altUnsafe(List.of(string_a, string_a)), string_a), nest2.getRules());
        Assertions.assertEquals(List.of(string_a, string_a, AlternationRule.altUnsafe(List.of(string_a, string_a))), nest3.getRules());
    }

    @Test
    void withRules() {
        var i = List.of(string_a, string_b);
        var r = AlternationRule.altUnsafe(i);

        // Expected
        Assertions.assertEquals(i, r.getRules());

        var ni = List.of(string_a, StringTerm.createDirect("c", false));
        var nr = r.withRules(ni);

        // The new rule.
        Assertions.assertEquals(ni, nr.getRules());

        // Original left untouched.
        Assertions.assertEquals(i, r.getRules());
    }

    @Test
    void withRulesPreservesHideAndRed() {
        var i = List.of(string_a, string_b);
        var r = AlternationRule
                .altUnsafe(i)
                .withHideTag(true)
                .withReduction(ReductionType.taggedTree(Sym.sym("A")));

        // Expected
        Assertions.assertEquals(i, r.getRules());

        // Create a new rule, check whether hide and reduction are still unchanged.
        var ni = List.of(string_a, StringTerm.createDirect("c", false));
        var nr = r.withRules(ni);
        Assertions.assertEquals(ni, nr.getRules());
        Assertions.assertEquals(r.isHiddenInResult(), nr.isHiddenInResult());
        Assertions.assertEquals(r.getReduction(), nr.getReduction());

        // Original left untouched.
        Assertions.assertEquals(i, r.getRules());
    }

    @Test
    void withHideTag() {
        var simpleRuleForTests = AlternationRule.altUnsafe(List.of(string_a, string_b));

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

        // By default, not hidden in output
        var pDefault = Parsewisp.parser(
                Gr.grammar("S", simpleRuleForTests));
        Assertions.assertEquals(PT.create("S", "a"), pDefault.parse("a"));

        // With explicit show, also not hidden
        var pShow = Parsewisp.parser(
                Gr.grammar("S", simpleRuleForTests.withHideTag(false)));
        Assertions.assertEquals(PT.create("S", "a"), pShow.parse("a"));

        // With explicit hide
        var pHide = Parsewisp.parser(
                Gr.grammar("S", simpleRuleForTests.withHideTag(true)));
        Assertions.assertEquals(PT.create("S"), pHide.parse("a"));
    }

    @Test
    void withReduction() {
        var simpleRuleForTests = AlternationRule.altUnsafe(List.of(string_a, string_b));

        // Default.
        Assertions.assertEquals(
                simpleRuleForTests,
                simpleRuleForTests.withReduction(ReductionType.initial()));

        // Default: Don't merge up
        var pDefault = Parsewisp.parser(
                Gr.grammarS("S",
                        "S", NonTerminal.create(Sym.sym("A")),
                        "A", simpleRuleForTests));
        Assertions.assertEquals(PT.create("S", PT.create("A", "a")), pDefault.parse("a"));

        // Merge upwards
        var pMerge = Parsewisp.parser(
                Gr.grammarS("S",
                        "S", NonTerminal.create(Sym.sym("A")),
                        "A", simpleRuleForTests.withReduction(ReductionType.merging())));
        Assertions.assertEquals(PT.create("S", "a"), pMerge.parse("a"));

        // Add to parse tree with potentially different name (Top-level)
        var pMerge2 = Parsewisp.parser(
                Gr.grammarS("S",
                        "S", simpleRuleForTests.withReduction(ReductionType.taggedTree(Sym.sym("B")))));
        Assertions.assertEquals(PT.create("B", "a"), pMerge2.parse("a"));

        // Add to parse tree with potentially different name (Nested)
        var pMerge3 = Parsewisp.parser(
                Gr.grammarS("S",
                        "S", NonTerminal.create(Sym.sym("A")),
                        "A", simpleRuleForTests.withReduction(ReductionType.taggedTree(Sym.sym("B")))));
        Assertions.assertEquals(PT.create("S", PT.create("B", "a")), pMerge3.parse("a"));

        // Add parse tree from a rule which doesn't appear in the grammar.
        var pMerge4 = Parsewisp.parser(
                Gr.grammarS("S",
                        "S", NonTerminal.create(Sym.sym("A")),
                        "A", ConcatRule.create(List.of(
                                simpleRuleForTests.withReduction(ReductionType.taggedTree(Sym.sym("B"))),
                                StringTerm.create("b", false)
                        ))));
        Assertions.assertEquals(
                PT.create("S", PT.create("A", PT.create("B", "a"), "b")),
                pMerge4.parse("ab"));
    }


    @Test
    void multipleParsesTest1() {
        final @NotNull var p = Parsewisp.parser(Gr.grammar(
                "S", AlternationRule.altUnsafe(List.of(
                        NonTerminal.create(Sym.sym("A")),
                        NonTerminal.create(Sym.sym("B")),
                        NonTerminal.create(Sym.sym("C")),
                        NonTerminal.create(Sym.sym("D")),
                        NonTerminal.create(Sym.sym("E")))),
                "A", EpsilonTerm.getDefault(),
                "B", EpsilonTerm.getDefault(),
                "C", EpsilonTerm.getDefault(),
                "D", EpsilonTerm.getDefault(),
                "E", EpsilonTerm.getDefault()
        ));

        Assertions.assertEquals(
                Set.of(PT.create("S", PT.create("A")),
                        PT.create("S", PT.create("B")),
                        PT.create("S", PT.create("C")),
                        PT.create("S", PT.create("D")),
                        PT.create("S", PT.create("E"))),
                new HashSet<>(p.parses("")));
    }

    @Test
    void recursiveParsesTest1() {
        // Tests the following grammar: `S = "a" S "a" | "a"
        final @NotNull var p = Parsewisp.parser(Gr.grammar(
                "S", AlternationRule.altUnsafe(List.of(
                        ConcatRule.create(List.of(
                                string_a, NonTerminal.create(Sym.sym("S")), string_a
                        )),
                        string_a
                ))));

        Assertions.assertEquals(
                Set.of(PT.create("S",
                        "a",
                        PT.create("S",
                                "a",
                                PT.create("S", "a"),
                                "a"),
                        "a")),
                new HashSet<>(p.parses("aaaaa")));
    }

    @Test
    void recursiveMultipleParsesTest() {
        // Tests the following grammar: `S = "a" | S S
        final @NotNull var p = Parsewisp.parser(Gr.grammar(
                "S", AlternationRule.altUnsafe(List.of(
                        string_a,
                        ConcatRule.create(List.of(
                                NonTerminal.create(Sym.sym("S")), NonTerminal.create(Sym.sym("S"))))
                ))));

        // Results for parsing "aaa"
        Assertions.assertEquals(Set.of(
                        PT.create("S",
                                PT.create("S", "a"),
                                PT.create("S",
                                        PT.create("S", "a"),
                                        PT.create("S", "a"))),
                        PT.create("S",
                                PT.create("S",
                                        PT.create("S", "a"),
                                        PT.create("S", "a")),
                                PT.create("S", "a"))),
                new HashSet<>(p.parses("aaa")));

        // Results for parsing "aaaaa" (5x "a")
        Assertions.assertEquals(
                Set.of(PT.create("S",
                                PT.create("S", "a"),
                                PT.create("S",
                                        PT.create("S", "a"),
                                        PT.create("S",
                                                PT.create("S", "a"),
                                                PT.create("S",
                                                        PT.create("S", "a"),
                                                        PT.create("S", "a"))))),
                        PT.create("S",
                                PT.create("S",
                                        PT.create("S", "a"),
                                        PT.create("S", "a")),
                                PT.create("S",
                                        PT.create("S", "a"),
                                        PT.create("S",
                                                PT.create("S", "a"),
                                                PT.create("S", "a")))),
                        PT.create("S",
                                PT.create("S",
                                        PT.create("S", "a"),
                                        PT.create("S",
                                                PT.create("S", "a"),
                                                PT.create("S",
                                                        PT.create("S", "a"),
                                                        PT.create("S", "a")))),
                                PT.create("S", "a")),
                        PT.create("S",
                                PT.create("S",
                                        PT.create("S", "a"),
                                        PT.create("S",
                                                PT.create("S", "a"),
                                                PT.create("S", "a"))),
                                PT.create("S",
                                        PT.create("S", "a"),
                                        PT.create("S", "a"))),
                        PT.create("S",
                                PT.create("S",
                                        PT.create("S", "a"),
                                        PT.create("S", "a")),
                                PT.create("S",
                                        PT.create("S",
                                                PT.create("S", "a"),
                                                PT.create("S", "a")),
                                        PT.create("S", "a"))),
                        PT.create("S",
                                PT.create("S", "a"),
                                PT.create("S",
                                        PT.create("S",
                                                PT.create("S", "a"),
                                                PT.create("S", "a")),
                                        PT.create("S",
                                                PT.create("S", "a"),
                                                PT.create("S", "a")))),
                        PT.create("S",
                                PT.create("S",
                                        PT.create("S",
                                                PT.create("S", "a"),
                                                PT.create("S", "a")),
                                        PT.create("S",
                                                PT.create("S", "a"),
                                                PT.create("S", "a"))),
                                PT.create("S", "a")),
                        PT.create("S",
                                PT.create("S",
                                        PT.create("S",
                                                PT.create("S", "a"),
                                                PT.create("S", "a")),
                                        PT.create("S", "a")),
                                PT.create("S",
                                        PT.create("S", "a"),
                                        PT.create("S", "a"))),
                        PT.create("S",
                                PT.create("S",
                                        PT.create("S",
                                                PT.create("S", "a"),
                                                PT.create("S",
                                                        PT.create("S", "a"),
                                                        PT.create("S", "a"))),
                                        PT.create("S", "a")),
                                PT.create("S", "a")),
                        PT.create("S",
                                PT.create("S", "a"),
                                PT.create("S",
                                        PT.create("S",
                                                PT.create("S", "a"),
                                                PT.create("S",
                                                        PT.create("S", "a"),
                                                        PT.create("S", "a"))),
                                        PT.create("S", "a"))),
                        PT.create("S",
                                PT.create("S", "a"),
                                PT.create("S",
                                        PT.create("S",
                                                PT.create("S",
                                                        PT.create("S", "a"),
                                                        PT.create("S", "a")),
                                                PT.create("S", "a")),
                                        PT.create("S", "a"))),
                        PT.create("S",
                                PT.create("S",
                                        PT.create("S",
                                                PT.create("S",
                                                        PT.create("S", "a"),
                                                        PT.create("S", "a")),
                                                PT.create("S", "a")),
                                        PT.create("S", "a")),
                                PT.create("S", "a")),
                        PT.create("S",
                                PT.create("S",
                                        PT.create("S", "a"),
                                        PT.create("S",
                                                PT.create("S",
                                                        PT.create("S", "a"),
                                                        PT.create("S", "a")),
                                                PT.create("S", "a"))),
                                PT.create("S", "a")),
                        PT.create("S",
                                PT.create("S", "a"),
                                PT.create("S",
                                        PT.create("S", "a"),
                                        PT.create("S",
                                                PT.create("S",
                                                        PT.create("S", "a"),
                                                        PT.create("S", "a")),
                                                PT.create("S", "a"))))
                ),
                new HashSet<>(p.parses("aaaaa")));
    }
}