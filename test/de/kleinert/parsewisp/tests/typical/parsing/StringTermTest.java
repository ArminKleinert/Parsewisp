package de.kleinert.parsewisp.tests.typical.parsing;

import de.kleinert.parsewisp.Parsewisp;
import de.kleinert.parsewisp.Sym;
import de.kleinert.parsewisp.parsing.ConcatRule;
import de.kleinert.parsewisp.parsing.NonTerminal;
import de.kleinert.parsewisp.parsing.StringTerm;
import de.kleinert.parsewisp.reduction.ReductionType;
import de.kleinert.parsewisp.testutil.Gr;
import de.kleinert.parsewisp.testutil.PT;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class StringTermTest {
    private final StringTerm simpleRuleForTests = StringTerm.createDirect("a", false);
    private final StringTerm simple_a = StringTerm.createDirect("a", false);
    private final StringTerm simple_b = StringTerm.createDirect("b", false);

    private StringTerm make(String s, boolean cs) {
        return StringTerm.createDirect(s, cs);
    }

    @Test
    void create() {
        Assertions.assertEquals(
                StringTerm.create("", false),
                StringTerm.create("", false));
        Assertions.assertEquals(
                StringTerm.create("", false),
                StringTerm.create("", true));
        Assertions.assertEquals(
                StringTerm.create("", true),
                StringTerm.create("", false));
        Assertions.assertEquals(
                StringTerm.create("", true),
                StringTerm.create("", true));

        Assertions.assertEquals(
                StringTerm.create("a", false),
                StringTerm.create("a", false));
        Assertions.assertNotEquals(
                StringTerm.create("a", false),
                StringTerm.create("a", true));
        Assertions.assertNotEquals(
                StringTerm.create("a", true),
                StringTerm.create("a", false));
        Assertions.assertEquals(
                StringTerm.create("a", true),
                StringTerm.create("a", true));

        Assertions.assertNotEquals(
                StringTerm.create("a", false),
                StringTerm.create("b", false));
        Assertions.assertNotEquals(
                StringTerm.create("a", false),
                StringTerm.create("b", true));
        Assertions.assertNotEquals(
                StringTerm.create("a", true),
                StringTerm.create("b", false));
        Assertions.assertNotEquals(
                StringTerm.create("a", true),
                StringTerm.create("b", true));
    }

    @Test
    void createDirect() {
        Assertions.assertEquals(
                make("", false),
                make("", false));
        Assertions.assertEquals(
                make("", false),
                make("", true));
        Assertions.assertEquals(
                make("", true),
                make("", false));
        Assertions.assertEquals(
                make("", true),
                make("", true));

        Assertions.assertEquals(
                make("a", false),
                make("a", false));
        Assertions.assertNotEquals(
                make("a", false),
                make("a", true));
        Assertions.assertNotEquals(
                make("a", true),
                make("a", false));
        Assertions.assertEquals(
                make("a", true),
                make("a", true));

        Assertions.assertNotEquals(
                make("a", false),
                make("b", false));
        Assertions.assertNotEquals(
                make("a", false),
                make("b", true));
        Assertions.assertNotEquals(
                make("a", true),
                make("b", false));
        Assertions.assertNotEquals(
                make("a", true),
                make("b", true));
    }

    @Test
    void parse() {
        // Case-sensitive.
        // Will use parse(...) for "a" and fullParse(...) for "b"
        var p = Parsewisp.parser(Gr.grammar("S", ConcatRule.create(List.of(simple_a, simple_b))));
        Assertions.assertEquals(PT.create("S", "a", "b"), p.parse("ab"));

        // Case-insensitive.
        // Will use parse(...) for "a" and fullParse(...) for "b"
        var pCI = Parsewisp.parser(Gr.grammar("S", ConcatRule.create(List.of(
                make("a", true),
                make("b", true)
        ))));
        Assertions.assertEquals(PT.create("S", "a", "b"), pCI.parse("ab"));
        Assertions.assertEquals(PT.create("S", "a", "b"), pCI.parse("Ab"));
        Assertions.assertEquals(PT.create("S", "a", "b"), pCI.parse("aB"));
        Assertions.assertEquals(PT.create("S", "a", "b"), pCI.parse("AB"));
    }

    @Test
    void parseError() {
        // Will use parse(...) for "a" and fullParse(...) for "b"
        var p = Parsewisp.parser(Gr.grammar("S", ConcatRule.create(List.of(simple_a, simple_b))));
        Assertions.assertTrue(p.parse("b").isFailure());
    }

    @Test
    void fullParseSingle() {
        // Case-sensitive.
        var p = Parsewisp.parser(Gr.grammar("S", simple_a));
        Assertions.assertEquals(PT.create("S", "a"), p.parse("a"));

        // Case-insensitive.
        var pCI = Parsewisp.parser(Gr.grammar("S", make("a", true)));
        Assertions.assertEquals(PT.create("S", "a"), pCI.parse("a"));
        Assertions.assertEquals(PT.create("S", "a"), pCI.parse("A"));
    }

    @Test
    void fullParseConcat() {
        var p = Parsewisp.parser(Gr.grammar(
                "S", ConcatRule.create(List.of(simple_a, simple_b))));
        Assertions.assertEquals(PT.create("S", "a", "b"), p.parse("ab"));
    }

    @Test
    void fullParseError() {
        var p = Parsewisp.parser(Gr.grammar("S", simple_a)); // Will use parse(...) for "a" and fullParse(...) for "b"
        Assertions.assertTrue(p.parse("").isFailure());
    }

    @Test
    void getString() {
        Assertions.assertEquals("", make("", false).getString());
        Assertions.assertEquals("", make("", true).getString());
        Assertions.assertEquals("a", make("a", false).getString());
        Assertions.assertEquals("a", make("a", true).getString());

        Assertions.assertEquals("\"", make("\"", false).getString());
        Assertions.assertEquals("\"", make("\"", true).getString());
    }

    @Test
    void isCaseInsensitive() {
        Assertions.assertFalse(make("", false).isCaseInsensitive());
        Assertions.assertTrue(make("", true).isCaseInsensitive());
        Assertions.assertFalse(make("a", false).isCaseInsensitive());
        Assertions.assertTrue(make("a", true).isCaseInsensitive());
    }

    @Test
    void withHideTag() {
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
        // Default.
        Assertions.assertEquals(
                simpleRuleForTests,
                simpleRuleForTests.withReduction(ReductionType.initial()));

        // Default: Don't merge up
        var pDefault = Parsewisp.parser(
                Gr.grammarS("S",
                        "S", NonTerminal.create(Sym.sym("A")),
                        "A", simple_a));
        Assertions.assertEquals(PT.create("S", PT.create("A", "a")), pDefault.parse("a"));

        // Merge upwards
        var pMerge = Parsewisp.parser(
                Gr.grammarS("S",
                        "S", NonTerminal.create(Sym.sym("A")),
                        "A", simple_a.withReduction(ReductionType.merging())));
        Assertions.assertEquals(PT.create("S", "a"), pMerge.parse("a"));

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

        // Add parse tree from a rule which doesn't appear in the grammar.
        var pMerge4 = Parsewisp.parser(
                Gr.grammarS("S",
                        "S", NonTerminal.create(Sym.sym("A")),
                        "A", ConcatRule.create(List.of(
                                simple_a.withReduction(ReductionType.taggedTree(Sym.sym("B"))),
                                make("b", false)
                        ))));
        Assertions.assertEquals(
                PT.create("S", PT.create("A", PT.create("B", "a"), "b")),
                pMerge4.parse("ab"));
    }
}