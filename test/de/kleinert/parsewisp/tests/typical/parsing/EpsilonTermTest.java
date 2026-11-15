package de.kleinert.parsewisp.tests.typical.parsing;

import de.kleinert.parsewisp.Parsewisp;
import de.kleinert.parsewisp.Sym;
import de.kleinert.parsewisp.grammar.Grammar;
import de.kleinert.parsewisp.parsing.ConcatRule;
import de.kleinert.parsewisp.parsing.EpsilonTerm;
import de.kleinert.parsewisp.parsing.StringTerm;
import de.kleinert.parsewisp.testutil.Gr;
import de.kleinert.parsewisp.testutil.PT;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class EpsilonTermTest {

    @Test
    void getDefault() {
        Assertions.assertEquals(EpsilonTerm.getDefault(), EpsilonTerm.getDefault());
    }

    @Test
    void parse() {
        var g = new Grammar(Sym.sym("S"), new LinkedHashMap<>(Map.of(
                Sym.sym("S"), ConcatRule.create(List.of(
                        EpsilonTerm.getDefault(),
                        StringTerm.create("b", false)))
        )));
        var p = Parsewisp.parser(g); // Will use parse(...) for epsilon and fullParse(...) for "b"
        Assertions.assertEquals(
                PT.create("S", "b"),
                p.parse("b")
        );
    }

    @Test
    void fullParsePart() {
        var g = Gr.grammar(Map.of("S", ConcatRule.create(List.of(
                StringTerm.create("a", false),
                EpsilonTerm.getDefault()))));
        var p = Parsewisp.parser(g); // Will use parse(...) for "a" and fullParse(...) for epsilon
        Assertions.assertEquals(
                PT.create("S", "a"),
                p.parse("a")
        );

        Assertions.assertTrue(p.parse("b").isFailure());
    }

    @Test
    void fullParse() {
        var g = Gr.grammar(Map.of("S", ConcatRule.create(List.of(
                EpsilonTerm.getDefault()))));
        var p = Parsewisp.parser(g);
        Assertions.assertEquals(
                PT.create("S"),
                p.parse("")
        );
    }

    @Test
    void fullParseError() {
        var g = Gr.grammar(Map.of("S", ConcatRule.create(List.of(
                EpsilonTerm.getDefault()))));
        var p = Parsewisp.parser(g);

        Assertions.assertTrue(p.parse("a").isFailure());
    }

    @Test
    void withHideTag() {
    }

    @Test
    void withReduction() {
    }
}