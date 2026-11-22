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

class ConcatRuleTest {

    private final Rule string_a = StringTerm.createDirect("a", false);

    @Test
    void create() {
        Assertions.assertEquals(
                ConcatRule.create(List.of(string_a, string_a, string_a)),
                ConcatRule.create(List.of(string_a, string_a, string_a)));
    }

    @Test
    void createEmpty() {
        // Empty list as input -> Can create an Epsilon rule or something equivalent.
        // Restriction: Equivalent input -> equivalent output
        Assertions.assertEquals(
                ConcatRule.create(List.of()),
                ConcatRule.create(List.of()));

        Assertions.assertEquals(
                Parsewisp.parser(Gr.grammar("S", EpsilonTerm.getDefault())).parse(""),
                Parsewisp.parser(Gr.grammar("S", ConcatRule.create(List.of()))).parse(""));
    }

    @Test
    void createOnlyEps() {
        // Only epsilons in concatenation -> Parse must give equivalent output to a single epsilon.
        // I.e. `cat(eps, eps) == eps` is not necessarily true, but they must give the same parse result.

        Assertions.assertEquals(
                ConcatRule.create(List.of(EpsilonTerm.getDefault(), EpsilonTerm.getDefault())),
                ConcatRule.create(List.of(EpsilonTerm.getDefault(), EpsilonTerm.getDefault())));

        // create(...) is allowed to remove any epsilon terms.
        var r = ((ConcatRule) ConcatRule.create(List.of(EpsilonTerm.getDefault(), string_a, EpsilonTerm.getDefault(), string_a, EpsilonTerm.getDefault())));
        Assertions.assertTrue(r.getRules().size() >= 2); // At least the two string terms.
        Assertions.assertTrue(r.getRules().size() <= 5); // At max all five rules.

        Assertions.assertEquals(
                Parsewisp.parser(Gr.grammar("S", EpsilonTerm.getDefault())).parse(""),
                Parsewisp.parser(Gr.grammar("S", ConcatRule.create(List.of(EpsilonTerm.getDefault(), EpsilonTerm.getDefault()))))
                        .parse(""));
    }

    @Test
    void createOnlyOneElement() {
        // Only one element in list -> Parser must give equivalent output to the single element
        Assertions.assertEquals(
                ConcatRule.create(List.of(string_a)),
                ConcatRule.create(List.of(string_a)));
        Assertions.assertEquals(
                Parsewisp.parser(Gr.grammar("S", string_a)).parse(""),
                Parsewisp.parser(Gr.grammar("S", ConcatRule.create(List.of(string_a)))).parse(""));
    }

    @Test
    void createWithNested() {
        // Nested concatenation -> Parser must give equivalent output to a flat concatenation
        var flat = ConcatRule.create(List.of(string_a, string_a, string_a, string_a));
        var nest1 = ConcatRule.create(List.of(ConcatRule.create(List.of(string_a, string_a)), string_a, string_a));
        var nest2 = ConcatRule.create(List.of(string_a, ConcatRule.create(List.of(string_a, string_a)), string_a));
        var nest3 = ConcatRule.create(List.of(string_a, string_a, ConcatRule.create(List.of(string_a, string_a))));
        Assertions.assertEquals(
                Parsewisp.parser(Gr.grammar("S", flat)).parse("aaaa"),
                Parsewisp.parser(Gr.grammar("S", nest1)).parse("aaaa"));
        Assertions.assertEquals(
                Parsewisp.parser(Gr.grammar("S", flat)).parse("aaaa"),
                Parsewisp.parser(Gr.grammar("S", nest2)).parse("aaaa"));
        Assertions.assertEquals(
                Parsewisp.parser(Gr.grammar("S", flat)).parse("aaaa"),
                Parsewisp.parser(Gr.grammar("S", nest3)).parse("aaaa"));
    }

    @Test
    void createDirect() {
        Assertions.assertEquals(
                ConcatRule.createDirect(List.of(string_a, string_a, string_a)),
                ConcatRule.createDirect(List.of(string_a, string_a, string_a)));
        Assertions.assertEquals(
                ConcatRule.create(List.of(string_a, string_a, string_a)),
                ConcatRule.createDirect(List.of(string_a, string_a, string_a)));

        Assertions.assertEquals(
                ConcatRule.createDirect(List.of()),
                ConcatRule.createDirect(List.of()));

        Assertions.assertEquals(
                ConcatRule.createDirect(List.of(EpsilonTerm.getDefault(), EpsilonTerm.getDefault())),
                ConcatRule.createDirect(List.of(EpsilonTerm.getDefault(), EpsilonTerm.getDefault())));
        Assertions.assertNotEquals(
                ConcatRule.createDirect(List.of(EpsilonTerm.getDefault())),
                ConcatRule.createDirect(List.of(EpsilonTerm.getDefault(), EpsilonTerm.getDefault())));
        Assertions.assertEquals(
                2,
                ConcatRule.createDirect(List.of(EpsilonTerm.getDefault(), EpsilonTerm.getDefault())).getRules().size());

        var flat = ConcatRule.createDirect(List.of(string_a, string_a, string_a, string_a));
        var nest1 = ConcatRule.createDirect(List.of(ConcatRule.createDirect(List.of(string_a, string_a)), string_a, string_a));
        var nest2 = ConcatRule.createDirect(List.of(string_a, ConcatRule.createDirect(List.of(string_a, string_a)), string_a));
        var nest3 = ConcatRule.createDirect(List.of(string_a, string_a, ConcatRule.createDirect(List.of(string_a, string_a))));
        Assertions.assertNotEquals(flat, nest1);
        Assertions.assertNotEquals(flat, nest2);
        Assertions.assertNotEquals(flat, nest3);
    }

    /**
     * The {@link ConcatRule#create(List)} is allowed to flatten concatenations.
     * But it must not flatten if the sub-concatenation has been changed with {@link ConcatRule#withHideTag(boolean)} or {@link ConcatRule#withReduction(ReductionType)}.
     */
    @Test
    void createDoesNotMergeConcatIfRedOrHidden() {
        var c1 = ConcatRule.create(List.of(string_a, string_a))
                .withHideTag(true); // different hide-tag
        var c2 = ConcatRule.create(List.of(string_a, string_a))
                .withReduction(ReductionType.taggedTree(Sym.sym("A"))); // reduction has changed
        var c3 = ConcatRule.create(List.of(string_a, string_a))
                .withReduction(ReductionType.taggedTree(Sym.sym("B"))).withHideTag(true); // different reduction and hide-tag

        var cc = ConcatRule.create(List.of(c1, c2, c3));
        Assertions.assertEquals(
                ConcatRule.createDirect(List.of(c1, c2, c3)),
                cc);

        var p = Parsewisp.parser(Gr.grammar("S", cc));
        Assertions.assertEquals(
                PT.create("S",
                        /*hidden,*/
                        PT.create("A", "a", "a"),
                        PT.create("B"/*, hidden*/)
                ),
                p.parse("aaaaaa")
        );
    }

    // Rule#parse() is usually called in Concatenations. Since nested concatenations are usually flattened,
    // this is only possible though using alternations or nonterms.
    @Test
    void parse() {
        var p = Parsewisp.parser(
                Gr.grammarS("S",
                        "S", ConcatRule.create(List.of(NonTerminal.create(Sym.sym("A")), string_a)), // Rule#fullParse for this concat.
                        "A", ConcatRule.create(List.of(string_a, string_a)))); // << Rule#parse for this one.
        Assertions.assertEquals(
                PT.create("S", PT.create("A", "a", "a"), "a"),
                p.parse("aaa"));
    }

    @Test
    void parseError() {
        var p = Parsewisp.parser(
                Gr.grammarS("S",
                        "S", ConcatRule.create(List.of(NonTerminal.create(Sym.sym("A")), string_a)), // Rule#fullParse for this concat.
                        "A", ConcatRule.create(List.of(string_a, string_a)))); // << Rule#parse for this one.
        Assertions.assertTrue(p.parse("").isFailure());
        Assertions.assertTrue(p.parse("baaa").isFailure());
    }

    @Test
    void fullParseSingle() {
        // Nested concatenation -> Parser must give equivalent output to a flat concatenation
        var flat = ConcatRule.createDirect(List.of(string_a, string_a, string_a, string_a));
        var nest1 = ConcatRule.createDirect(List.of(ConcatRule.createDirect(List.of(string_a, string_a)), string_a, string_a));
        var nest2 = ConcatRule.createDirect(List.of(string_a, ConcatRule.createDirect(List.of(string_a, string_a)), string_a));
        var nest3 = ConcatRule.createDirect(List.of(string_a, string_a, ConcatRule.createDirect(List.of(string_a, string_a))));
        Assertions.assertEquals(
                PT.create("S", "a", "a", "a", "a"),
                Parsewisp.parser(Gr.grammar("S", flat)).parse("aaaa"));
        Assertions.assertEquals(
                Parsewisp.parser(Gr.grammar("S", flat)).parse("aaaa"),
                Parsewisp.parser(Gr.grammar("S", nest1)).parse("aaaa"));
        Assertions.assertEquals(
                Parsewisp.parser(Gr.grammar("S", flat)).parse("aaaa"),
                Parsewisp.parser(Gr.grammar("S", nest2)).parse("aaaa"));
        Assertions.assertEquals(
                Parsewisp.parser(Gr.grammar("S", flat)).parse("aaaa"),
                Parsewisp.parser(Gr.grammar("S", nest3)).parse("aaaa"));
    }

    @Test
    void fullParseError() {
        var p = Parsewisp.parser(Gr.grammar("S", string_a)); // Will use parse(...) for "a" and fullParse(...) for "b"
        Assertions.assertTrue(p.parse("").isFailure());
    }

    @Test
    void getRules() {
        var flat = ConcatRule.createDirect(List.of(string_a, string_a, string_a, string_a));
        var nest1 = ConcatRule.createDirect(List.of(ConcatRule.createDirect(List.of(string_a, string_a)), string_a, string_a));
        var nest2 = ConcatRule.createDirect(List.of(string_a, ConcatRule.createDirect(List.of(string_a, string_a)), string_a));
        var nest3 = ConcatRule.createDirect(List.of(string_a, string_a, ConcatRule.createDirect(List.of(string_a, string_a))));

        Assertions.assertEquals(List.of(string_a, string_a, string_a, string_a), flat.getRules());
        Assertions.assertEquals(List.of(ConcatRule.createDirect(List.of(string_a, string_a)), string_a, string_a), nest1.getRules());
        Assertions.assertEquals(List.of(string_a, ConcatRule.createDirect(List.of(string_a, string_a)), string_a), nest2.getRules());
        Assertions.assertEquals(List.of(string_a, string_a, ConcatRule.createDirect(List.of(string_a, string_a))), nest3.getRules());
    }

    @Test
    void withRules() {
        var i = List.of(string_a, string_a, string_a, string_a);
        var r = (ConcatRule) ConcatRule
                .create(i)
                .withHideTag(true)
                .withReduction(ReductionType.taggedTree(Sym.sym("A")));

        // As expected.
        Assertions.assertEquals(i, r.getRules());

        // Create a new rule, check whether hide and reduction are still unchanged.
        var ni = List.of(string_a, string_a);
        var nr = r.withRules(ni);
        Assertions.assertEquals(ni, nr.getRules());
        Assertions.assertEquals(r.isHiddenInResult(), nr.isHiddenInResult());
        Assertions.assertEquals(r.getReduction(), nr.getReduction());

        // Original left untouched.
        Assertions.assertEquals(i, r.getRules());
    }

    @Test
    void withHideTag() {
        var simpleRuleForTests = ConcatRule.createDirect(List.of(string_a, string_a));

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
        Assertions.assertEquals(PT.create("S", "a", "a"), pDefault.parse("aa"));

        // With explicit show, also not hidden
        var pShow = Parsewisp.parser(
                Gr.grammar("S", simpleRuleForTests.withHideTag(false)));
        Assertions.assertEquals(PT.create("S", "a", "a"), pShow.parse("aa"));

        // With explicit hide
        var pHide = Parsewisp.parser(
                Gr.grammar("S", simpleRuleForTests.withHideTag(true)));
        Assertions.assertEquals(PT.create("S"), pHide.parse("aa"));
    }

    @Test
    void withReduction() {
        var simpleRuleForTests = ConcatRule.createDirect(List.of(string_a, string_a));

        // Default.
        Assertions.assertEquals(
                simpleRuleForTests,
                simpleRuleForTests.withReduction(ReductionType.initial()));

        // Default: Don't merge up
        var pDefault = Parsewisp.parser(
                Gr.grammarS("S",
                        "S", NonTerminal.create(Sym.sym("A")),
                        "A", simpleRuleForTests));
        Assertions.assertEquals(PT.create("S", PT.create("A", "a", "a")), pDefault.parse("aa"));

        // Merge upwards
        var pMerge = Parsewisp.parser(
                Gr.grammarS("S",
                        "S", NonTerminal.create(Sym.sym("A")),
                        "A", simpleRuleForTests.withReduction(ReductionType.merging())));
        Assertions.assertEquals(PT.create("S", "a", "a"), pMerge.parse("aa"));

        // Add to parse tree with potentially different name (Top-level)
        var pMerge2 = Parsewisp.parser(
                Gr.grammarS("S",
                        "S", simpleRuleForTests.withReduction(ReductionType.taggedTree(Sym.sym("B")))));
        Assertions.assertEquals(PT.create("B", "a", "a"), pMerge2.parse("aa"));

        // Add to parse tree with potentially different name (Nested)
        var pMerge3 = Parsewisp.parser(
                Gr.grammarS("S",
                        "S", NonTerminal.create(Sym.sym("A")),
                        "A", simpleRuleForTests.withReduction(ReductionType.taggedTree(Sym.sym("B")))));
        Assertions.assertEquals(PT.create("S", PT.create("B", "a", "a")), pMerge3.parse("aa"));

        // Add parse tree from a rule which doesn't appear in the grammar.
        var pMerge4 = Parsewisp.parser(
                Gr.grammarS("S",
                        "S", NonTerminal.create(Sym.sym("A")),
                        "A", ConcatRule.create(List.of(
                                simpleRuleForTests.withReduction(ReductionType.taggedTree(Sym.sym("B"))),
                                StringTerm.create("b", false)
                        ))));
        Assertions.assertEquals(
                PT.create("S", PT.create("A", PT.create("B", "a", "a"), "b")),
                pMerge4.parse("aab"));
    }
}