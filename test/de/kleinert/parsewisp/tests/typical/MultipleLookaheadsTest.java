package de.kleinert.parsewisp.tests.typical;

import de.kleinert.parsewisp.Parsewisp;
import de.kleinert.parsewisp.Sym;
import de.kleinert.parsewisp.grammar.Grammar;
import de.kleinert.parsewisp.parser_options.ParserCreationOptions;
import de.kleinert.parsewisp.parsing.*;
import de.kleinert.parsewisp.testutil.PT;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Pattern;

class MultipleLookaheadsTest {
    @Test
    void contradictoryLookaheads() {
        var p = Parsewisp.parser("S := &'a' &'b' ('a' | 'b' | 'c')+");
        Assertions.assertTrue(p.parse("a").isFailure());
        Assertions.assertTrue(p.parse("b").isFailure());
        Assertions.assertTrue(p.parse("c").isFailure());
    }

    @Test
    void lookaheads1() {
        var grammarAsMap = new LinkedHashMap<Sym, Rule>();
        grammarAsMap.put(
                Sym.sym("S"),
                ConcatRule.create(List.of(LookaheadRule.create(LookaheadRule.create(StringTerm.create("a", false))),
                        RegexTerm.create(Pattern.compile("[abc]")))));
        var p = Parsewisp.parser(new Grammar(
                        Sym.sym("S"),
                        grammarAsMap),
                ParserCreationOptions.getDefault().withStartProduction(Sym.sym("S")));
        Assertions.assertEquals(PT.create("S", "a"), p.parse("a"));
        Assertions.assertTrue(p.parse("b").isFailure());
    }

    @Test
    void doubledLookahead() {
        var p = Parsewisp.parser("S = &'a' &'a' ('a' | 'b' | 'c')+");
        Assertions.assertEquals(PT.create("S", "a"), p.parse("a"));
        Assertions.assertTrue(p.parse("b").isFailure());
    }

    @Test
    void doubledNegativeLookahead() {
        var p = Parsewisp.parser("S = !'a' !'a' ('a' | 'b' | 'c')+");
        Assertions.assertEquals(PT.create("S", "b"), p.parse("b"));
        Assertions.assertEquals(PT.create("S", "c"), p.parse("c"));
        Assertions.assertTrue(p.parse("a").isFailure());
    }

    @Test
    void doubledNegativeLookahead1() {
        var p = Parsewisp.parser("S = !'a' !'b' ('a' | 'b' | 'c')+");
        Assertions.assertEquals(PT.create("S", "c"), p.parse("c"));
        Assertions.assertTrue(p.parse("a").isFailure());
        Assertions.assertTrue(p.parse("b").isFailure());
    }
}
