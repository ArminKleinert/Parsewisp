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
import java.util.regex.Pattern;

class ExclusionRuleTest {
    private final Rule word = RegexTerm.create(Pattern.compile("[a-zA-Z][a-zA-Z0-1_]*"));
    private final Rule keywords = AlternationRule.create(List.of(
            StringTerm.create("int", false),
            StringTerm.create("float", false)));
    private final Rule wordButNotKeyword = ExclusionRule.create(word, keywords);

    @Test
    void create() {
        Assertions.assertEquals(
                ExclusionRule.create(word, keywords),
                ExclusionRule.create(word, keywords));

        // The order matters
        Assertions.assertNotEquals(
                ExclusionRule.create(keywords, word),
                ExclusionRule.create(word, keywords));
    }

    @Test
    void createExcludedInclusion() {
        // create(...) is allowed to create an Epsilon if the inclusion and exclusion sides are the same.
        var exSame = ExclusionRule.create(word, word);
        Assertions.assertTrue(exSame instanceof ExclusionRule || exSame instanceof EpsilonTerm);
    }

    @Test
    void parse() {
        var p = Parsewisp.parser(Gr.grammar(
                "S", ConcatRule.create(List.of(
                        wordButNotKeyword,
                        StringTerm.create(";", false) // Required so the exclusion calls "parse" instead of "fullParse".
                ))));
        Assertions.assertEquals(
                PT.create("S", "anyword", ";"),
                p.parse("anyword;"));
    }

    @Test
    void parseError() {
        var p = Parsewisp.parser(Gr.grammar(
                "S", ConcatRule.create(List.of(
                        wordButNotKeyword,
                        StringTerm.create(";", false) // Required so the exclusion calls "parse" instead of "fullParse".
                ))));
        Assertions.assertTrue(p.parse("int;").isFailure()); // Matched by exclusion
        Assertions.assertTrue(p.parse("...;").isFailure()); // Invalid for inclusion
    }

    @Test
    void fullParseSingle() {
        var p = Parsewisp.parser(Gr.grammar("S", wordButNotKeyword));
        Assertions.assertEquals(
                PT.create("S", "anyword"),
                p.parse("anyword"));
    }

    @Test
    void fullParseConcat() {
        var p = Parsewisp.parser(Gr.grammar(
                "S", ConcatRule.create(List.of(
                        StringTerm.create(";", false),
                        wordButNotKeyword))));
        Assertions.assertEquals(
                PT.create("S", ";", "anyword"),
                p.parse(";anyword"));
    }

    @Test
    void fullParseError() {
        var p = Parsewisp.parser(Gr.grammar("S", wordButNotKeyword));
        Assertions.assertTrue(p.parse("int").isFailure()); // Matched by exclusion
        Assertions.assertTrue(p.parse("...").isFailure()); // Invalid for inclusion
    }

    @Test
    void getExpected() {
        var r = (ExclusionRule) ExclusionRule.create(word, keywords);
        Assertions.assertSame(word, r.getExpected());
    }

    @Test
    void getExcluded() {
        var r = (ExclusionRule) ExclusionRule.create(word, keywords);
        Assertions.assertSame(keywords, r.getExcluded());
    }

    @Test
    void getRules() {
        var r = (ExclusionRule) ExclusionRule.create(word, keywords);
        Assertions.assertEquals(List.of(word, keywords), r.getRules());
    }

    @Test
    void withRulesRequiresExactlyTwo() {
        var r = (ExclusionRule) ExclusionRule.create(word, keywords);
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> r.withRules(List.of()));
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> r.withRules(List.of(word)));
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> r.withRules(List.of(
                        word, keywords,
                        RegexTerm.create(Pattern.compile("[a-zA-Z]")))));
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> r.withRules(List.of(
                        word, keywords,
                        RegexTerm.create(Pattern.compile("[a-zA-Z]")),
                        RegexTerm.create(Pattern.compile("[0-9]")))));
    }

    @Test
    void withRules() {
        var i = List.of(word, keywords);
        var r = (ExclusionRule) ExclusionRule.create(word, keywords);

        // Expected
        Assertions.assertEquals(i, r.getRules());

        var ni = List.of(word, StringTerm.createDirect("c", false));
        var nr = r.withRules(ni);

        // The new rule.
        Assertions.assertEquals(ni, nr.getRules());

        // Original left untouched.
        Assertions.assertEquals(i, r.getRules());
    }

    @Test
    void withRulesPreservesHideAndRed() {
        var i = List.of(word, keywords);
        var r = (ExclusionRule) ExclusionRule
                .create(word, keywords)
                .withHideTag(true)
                .withReduction(ReductionType.taggedTree(Sym.sym("A")));

        // Expected
        Assertions.assertEquals(i, r.getRules());

        // Create a new rule, check whether hide and reduction are still unchanged.
        var ni = List.of(word, StringTerm.createDirect("c", false));
        var nr = r.withRules(ni);
        Assertions.assertEquals(ni, nr.getRules());
        Assertions.assertEquals(r.isHiddenInResult(), nr.isHiddenInResult());
        Assertions.assertEquals(r.getReduction(), nr.getReduction());

        // Original left untouched.
        Assertions.assertEquals(i, r.getRules());
    }

    @Test
    void withHideTag() {
        var simpleRuleForTests = (ExclusionRule) ExclusionRule.create(word, keywords);

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
    void withHideTagParse() {
    }

    @Test
    void withReduction() {
        var simpleRuleForTests = (ExclusionRule) ExclusionRule.create(word, keywords);

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
    }

    @Test
    void withReductionMergeUp() {
        var simpleRuleForTests = (ExclusionRule) ExclusionRule.create(word, keywords);

        // Merge upwards
        var pMerge = Parsewisp.parser(
                Gr.grammarS("S",
                        "S", NonTerminal.create(Sym.sym("A")),
                        "A", simpleRuleForTests.withReduction(ReductionType.merging())));
        Assertions.assertEquals(PT.create("S", "a"), pMerge.parse("a"));
    }

    @Test
    void withReductionTaggedTree() {
        var simpleRuleForTests = (ExclusionRule) ExclusionRule.create(word, keywords);

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
    }

    @Test
    void withReductionNotDirectRhs() {
        var simpleRuleForTests = (ExclusionRule) ExclusionRule.create(word, keywords);

        // Add parse tree from a rule which doesn't appear in the grammar.
        var pMerge4 = Parsewisp.parser(
                Gr.grammarS("S",
                        "S", NonTerminal.create(Sym.sym("A")),
                        "A", ConcatRule.create(List.of(
                                simpleRuleForTests.withReduction(ReductionType.taggedTree(Sym.sym("B"))),
                                StringTerm.create(";", false)
                        ))));
        Assertions.assertEquals(
                PT.create("S", PT.create("A", PT.create("B", "a"), ";")),
                pMerge4.parse("a;"));
    }
}