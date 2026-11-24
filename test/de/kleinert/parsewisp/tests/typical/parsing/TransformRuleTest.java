package de.kleinert.parsewisp.tests.typical.parsing;

import de.kleinert.parsewisp.Parsewisp;
import de.kleinert.parsewisp.Sym;
import de.kleinert.parsewisp.grammar.Grammar;
import de.kleinert.parsewisp.parsing.ConcatRule;
import de.kleinert.parsewisp.parsing.StringTerm;
import de.kleinert.parsewisp.parsing.TransformRule;
import de.kleinert.parsewisp.testutil.PT;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class TransformRuleTest {
    @Test
    void create() {
    }

    @Test
    void parse() {
        var g = new Grammar(Sym.sym("S"), new LinkedHashMap<>(Map.of(
                Sym.sym("S"), ConcatRule.create(List.of(
                        TransformRule.create(
                                StringTerm.create("abc", false),
                                (r, i) -> Arrays.asList(((String) r.get(0)).split(""))),
                        StringTerm.create("def", false)))
        )));
        var p = Parsewisp.parser(g);
        Assertions.assertEquals(
                PT.create("S", "a", "b", "c", "def"),
                p.parse("abcdef")
        );
    }

    @Test
    void parse1() {
        var g = new Grammar(Sym.sym("S"), new LinkedHashMap<>(Map.of(
                Sym.sym("S"), ConcatRule.create(List.of(
                        TransformRule.create(
                                StringTerm.create("abc", false),
                                (r, i) -> List.of("a", "b", "c")
                        ))))));
        var p = Parsewisp.parser(g);
        Assertions.assertEquals(
                PT.create("S", "a", "b", "c"),
                p.parse("abc")
        );
    }

    @Test
    void parseError() {        //TODO
    }

    @Test
    void fullParse() {
        var g = new Grammar(Sym.sym("S"), new LinkedHashMap<>(Map.of(
                Sym.sym("S"), ConcatRule.create(List.of(
                        StringTerm.create("abc", false),
                        TransformRule.create(
                                StringTerm.create("def", false),
                                (r, i) -> Arrays.asList(((String) r.get(0)).split("")))))
        )));
        var p = Parsewisp.parser(g);
        Assertions.assertEquals(
                PT.create("S", "abc", "d", "e", "f"),
                p.parse("abcdef")
        );
    }

    @Test
    void fullParseSingle() {        //TODO
    }

    @Test
    void fullParseConcat() {//TODO
    }

    @Test
    void fullParseError() {//TODO
    }

    @Test
    void getRule() {//TODO
    }

    @Test
    void withRule() {
    }

    @Test
    void withHideTag() {
    }

    @Test void withHideTagParse() {}

    @Test
    void withReduction() {
    }

    @Test void withReductionMergeUp() {}
    @Test void withReductionTaggedTree() {}
    @Test void withReductionNotDirectRhs() {}
}