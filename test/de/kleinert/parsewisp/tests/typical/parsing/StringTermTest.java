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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class StringTermTest {
    @Test
    void create() {
    }

    @Test
    void parse() {
        var g = Gr.grammar(Map.of("S", ConcatRule.create(List.of(
                StringTerm.create("a", false),
                StringTerm.create("b", false)))));
        var p = Parsewisp.parser(g); // Will use parse(...) for "a" and fullParse(...) for "b"
        Assertions.assertEquals(PT.create("S", "a", "b"), p.parse("ab"));
    }

    @Test
    void fullParse() {
        var g = Gr.grammar(Map.of("S", ConcatRule.create(List.of(
                StringTerm.create("a", false),
                StringTerm.create("b", false)))));
        var p = Parsewisp.parser(g); // Will use parse(...) for "a" and fullParse(...) for "b"
        Assertions.assertEquals(PT.create("S", "a", "b"), p.parse("ab"));
    }

    @Test
    void getString() {
    }

    @Test
    void isCaseInsensitive() {
    }

    @Test
    void withHideTag() {
    }

    @Test
    void withReduction() {
    }

}