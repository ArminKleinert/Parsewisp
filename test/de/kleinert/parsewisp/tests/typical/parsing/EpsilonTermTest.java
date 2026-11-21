package de.kleinert.parsewisp.tests.typical.parsing;

import de.kleinert.parsewisp.Parsewisp;
import de.kleinert.parsewisp.parsing.ConcatRule;
import de.kleinert.parsewisp.parsing.EpsilonTerm;
import de.kleinert.parsewisp.parsing.StringTerm;
import de.kleinert.parsewisp.testutil.Gr;
import de.kleinert.parsewisp.testutil.PT;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class EpsilonTermTest {

    @Test
    void getDefault() {
        Assertions.assertEquals(EpsilonTerm.getDefault(), EpsilonTerm.getDefault());
    }

    @Test
    void parse() {
        var g = Gr.grammar(
                "S", ConcatRule.create(List.of(
                        EpsilonTerm.getDefault(),
                        StringTerm.create("b", false))));
        var p = Parsewisp.parser(g); // Will use parse(...) for epsilon and fullParse(...) for "b"
        Assertions.assertEquals(
                PT.create("S", "b"),
                p.parse("b")
        );
    }

    @Test
    void fullParsePart() {
        var g = Gr.grammar(
                "S", ConcatRule.create(List.of(
                        StringTerm.create("a", false),
                        EpsilonTerm.getDefault())));
        var p = Parsewisp.parser(g); // Will use parse(...) for "a" and fullParse(...) for epsilon
        Assertions.assertEquals(
                PT.create("S", "a"),
                p.parse("a")
        );

        Assertions.assertTrue(p.parse("b").isFailure());
    }

    @Test
    void fullParse() {
        var g = Gr.grammar(
                "S", ConcatRule.create(List.of(EpsilonTerm.getDefault())));
        var p = Parsewisp.parser(g);
        Assertions.assertEquals(
                PT.create("S"),
                p.parse("")
        );
    }

    @Test
    void parseError() {        //TODO
    }

    @Test
    void fullParseSingle() {        //TODO
    }


    @Test
    void fullParseConcat() {//TODO
    }

    @Test
    void fullParseError() {
        var g = Gr.grammar(
                "S", ConcatRule.create(List.of(EpsilonTerm.getDefault())));
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