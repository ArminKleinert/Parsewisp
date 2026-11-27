package de.kleinert.parsewisp.tests.typical.parsing;

import de.kleinert.parsewisp.Parsewisp;
import de.kleinert.parsewisp.Sym;
import de.kleinert.parsewisp.parsing.ConcatRule;
import de.kleinert.parsewisp.parsing.NonTerminal;
import de.kleinert.parsewisp.parsing.StringTerm;
import de.kleinert.parsewisp.reduction.Reduction;
import de.kleinert.parsewisp.testutil.Gr;
import de.kleinert.parsewisp.testutil.PT;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class NonTerminalTest {
    private NonTerminal nt(String name) {
        return NonTerminal.create(Sym.sym(name));
    }

    private NonTerminal simpleRuleForTests() {
        return NonTerminal.create(Sym.sym("A"));
    }

    @Test
    void create() {
        Assertions.assertEquals(
                NonTerminal.create(Sym.sym("S")), NonTerminal.create(Sym.sym("S")));
        Assertions.assertNotEquals(
                NonTerminal.create(Sym.sym("S1")), NonTerminal.create(Sym.sym("S2")));
    }

    @Test
    void getKeyword() {
        Assertions.assertEquals(
                Sym.sym("S"),
                NonTerminal.create(Sym.sym("S")).getKeyword());
    }

    @Test
    void parse() {
        var p = Parsewisp.parser(Gr.grammar(
                "S", ConcatRule.create(List.of(nt("A"), nt("B"))), // Calls parse(...) for A, but fullParse(...) for B.
                "A", StringTerm.create("a", false),
                "B", StringTerm.create("b", false)));
        Assertions.assertEquals(
                PT.create("S", PT.create("A", "a"), PT.create("B", "b")),
                p.parse("ab"));
    }

    @Test
    void parseError() {
        // A NonTerminal can't really fail. Only its related rules can.
        var p = Parsewisp.parser(Gr.grammar(
                "S", ConcatRule.create(List.of(nt("A"), nt("B"))), // Calls parse(...) for A, but fullParse(...) for B.
                "A", StringTerm.create("a", false),
                "B", StringTerm.create("b", false)));
        Assertions.assertTrue(p.parse("cb").isFailure());
        Assertions.assertTrue(p.parse("b").isFailure());
    }

    @Test
    void fullParseSingle() {
        var p = Parsewisp.parser(Gr.grammar(
                "S", nt("A"),
                "A", StringTerm.create("ab", false)));
        Assertions.assertEquals(
                PT.create("S", PT.create("A", "ab")),
                p.parse("ab"));
    }

    @Test
    void fullParseConcat() {
        var p = Parsewisp.parser(Gr.grammar(
                "S", ConcatRule.create(List.of(nt("A"), nt("B"))), // Calls parse(...) for A, but fullParse(...) for B.
                "A", StringTerm.create("a", false),
                "B", StringTerm.create("b", false)));
        Assertions.assertEquals(
                PT.create("S", PT.create("A", "a"), PT.create("B", "b")),
                p.parse("ab"));
    }

    @Test
    void fullParseError() {
        // A NonTerminal can't really fail. Only its related rules can.
        var p = Parsewisp.parser(Gr.grammar(
                "S", ConcatRule.create(List.of(nt("A"), nt("B"))), // Calls parse(...) for A, but fullParse(...) for B.
                "A", StringTerm.create("a", false),
                "B", StringTerm.create("a", false)));
        Assertions.assertTrue(p.parse("cb").isFailure());
        Assertions.assertTrue(p.parse("b").isFailure());
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
        var pDefault = Parsewisp.parser(Gr.grammar(
                "S", nt("A"),
                "A", StringTerm.create("a", false)));
        Assertions.assertEquals(
                PT.create("S", PT.create("A", "a")),
                pDefault.parse("a"));

        // With explicit show, also not hidden
        var pShow = Parsewisp.parser(Gr.grammar(
                "S", nt("A").withHideTag(false),
                "A", StringTerm.create("a", false)));
        Assertions.assertEquals(
                PT.create("S", PT.create("A", "a")),
                pShow.parse("a"));

        // With explicit hide
        var pHide = Parsewisp.parser(Gr.grammar(
                "S", nt("A").withHideTag(true),
                "A", StringTerm.create("a", false)));
        Assertions.assertEquals(PT.create("S"), pHide.parse("a"));
    }

    @Test
    void withReduction() {
        // Default.
        Assertions.assertEquals(
                simpleRuleForTests(),
                simpleRuleForTests().withReduction(Reduction.initial()));

        // Default: Don't merge up
        var pDefault = Parsewisp.parser(Gr.grammar(
                "S", nt("A"),
                "A", nt("B"),
                "B", StringTerm.create("a", false)));
        Assertions.assertEquals(
                PT.create("S", PT.create("A", PT.create("B", "a"))),
                pDefault.parse("a"));
    }

    @Test
    void withReductionMergeUp() {
        var pDefault = Parsewisp.parser(Gr.grammar(
                "S", nt("A"),
                "A", nt("B"),
                "B", StringTerm.create("a", false)));
        Assertions.assertEquals(
                PT.create("S", PT.create("A", PT.create("B", "a"))),
                pDefault.parse("a"));
    }

    @Test
    void withReductionTaggedTree() {
        var pDefault = Parsewisp.parser(Gr.grammar(
                "S", nt("A"),
                "A", nt("B").withReduction(Reduction.taggedTree(Sym.sym("C"))),
                "B", StringTerm.create("a", false)));
        Assertions.assertEquals(
                PT.create("S", PT.create("C", PT.create("B", "a"))),
                pDefault.parse("a"));
    }

    @Test
    void withReductionNotDirectRhs() {
        var pDefault = Parsewisp.parser(Gr.grammar(
                "S", nt("A"),
                "A", ConcatRule.create(List.of(
                        nt("B").withReduction(Reduction.taggedTree(Sym.sym("C"))),
                        StringTerm.create("b", false)
                )),
                "B", StringTerm.create("a", false)));
        Assertions.assertEquals(
                PT.create("S", PT.create("A", PT.create("C", PT.create("B", "a")), "b")),
                pDefault.parse("ab"));
    }
}