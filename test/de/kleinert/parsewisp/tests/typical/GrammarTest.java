package de.kleinert.parsewisp.tests.typical;

import de.kleinert.parsewisp.Parsewisp;
import de.kleinert.parsewisp.Sym;
import de.kleinert.parsewisp.parser_options.ParserCreationOptions;
import de.kleinert.parsewisp.parsing.StringTerm;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

class GrammarTest {
    @Test
    void grammarCollect() {
        var g = Parsewisp.parser("S = '1' | '2' S").grammar();
        var ga = g.analyze();

        Assertions.assertTrue(ga.isValid());

        Assertions.assertEquals(Set.of(Sym.sym("S")), new HashSet<>(ga.usedNTs()));
        Assertions.assertEquals(Set.of(Sym.sym("S")), new HashSet<>(ga.definedNTs()));

        Assertions.assertEquals(
                Set.of(StringTerm.create("1", false), StringTerm.create("2", false)),
                new HashSet<>(ga.collect(it -> it instanceof StringTerm)));
    }

    @Test
    void grammarAnalysisDoesNotRateStartSymAsUnused() {
        var ga = Parsewisp.parser("""
                S = A | "a"
                A = A epsilon | epsilon
                B = "b"
                C = "c"
                """).grammar().analyze();
        Assertions.assertFalse(ga.getUnusedNTs().contains(Sym.sym("S")));

        var ga2 = Parsewisp.parser("S = 'a'").grammar().analyze();
        Assertions.assertFalse(ga2.getUnusedNTs().contains(Sym.sym("S")));
    }

    @Test
    void grammarAnalysis() {
        var ga = Parsewisp.parser("""
                S = A | "a"
                A = A epsilon | epsilon
                B = "b"
                C = "c"
                """).grammar().analyze();
        Assertions.assertEquals(
                Set.of(Sym.sym("S"), Sym.sym("A"), Sym.sym("B"), Sym.sym("C")),
                ga.definedNTs());
        Assertions.assertEquals(
                Set.of(Sym.sym("A")),
                ga.usedNTs());
        Assertions.assertEquals(
                Set.of(Sym.sym("B"), Sym.sym("C")),
                ga.getUnusedNTs());
        Assertions.assertFalse(ga.getUnusedNTs().contains(Sym.sym("S")));
        Assertions.assertTrue(ga.isValid());
    }

    @Test
    void grammarSubsetFullTest() {
        var ga = Parsewisp.parser("""
                S = A | "a"
                A = A B | epsilon
                B = C
                C = "c"
                """).grammar().analyze();
        Assertions.assertEquals(
                ga.grammar(),
                ga.subGrammar(Sym.sym("S")));
    }

    @Test
    void grammarSubsetPartialTest() {
        var ga = Parsewisp.parser("""
                S = A | "a"
                A = A | C
                B = C
                C = "c"
                """).grammar().analyze();
        Assertions.assertEquals(
                Set.of(Sym.sym("S"), Sym.sym("A"), Sym.sym("C")),
                ga.subGrammar(Sym.sym("S")).keySet()
        );
        Assertions.assertEquals(
                Set.of(Sym.sym("A"), Sym.sym("C")),
                ga.subGrammar(Sym.sym("A")).keySet()
        );
    }

    @Test
    void grammarSubsetPartialTest1() {
        var p1 = Parsewisp.parser("""
                S = A | "a"
                A = A | C
                C = "c"
                """);
        var p2 = Parsewisp.parser("""
                S = A | "a"
                A = A | C
                B = C
                C = "c"
                """);
        Assertions.assertEquals(
                p1.grammar(),
                p2.grammar().analyze().subGrammar(Sym.sym("S"))
        );
    }

    @Test
    void grammarIsProductiveTest() {
        var noCheckOpts = ParserCreationOptions.getDefault().withCorrectnessCheck(false);
        Assertions.assertFalse(Parsewisp.parser("S = S", noCheckOpts).grammar().analyze().isProductive(Sym.sym("S")));
        Assertions.assertTrue(Parsewisp.parser("S = S | epsilon", noCheckOpts).grammar().analyze().isProductive(Sym.sym("S")));

        Assertions.assertTrue(Parsewisp.parser("S = A | epsilon; A = A", noCheckOpts).grammar().analyze().isProductive(Sym.sym("S")));
        Assertions.assertFalse(Parsewisp.parser("S = A | epsilon; A = A", noCheckOpts).grammar().analyze().isProductive(Sym.sym("A")));
        Assertions.assertTrue(Parsewisp.parser("S = A | epsilon; A = S", noCheckOpts).grammar().analyze().isProductive(Sym.sym("S")));

        Assertions.assertTrue(Parsewisp.parser("S = A B | epsilon; A = 'a'; B = 'b'", noCheckOpts).grammar().analyze().isProductive(Sym.sym("S")));
        Assertions.assertTrue(Parsewisp.parser("S = A B | epsilon; A = 'a'; B = S", noCheckOpts).grammar().analyze().isProductive(Sym.sym("S")));
        Assertions.assertFalse(Parsewisp.parser("S = A B; A = 'a'; B = S", noCheckOpts).grammar().analyze().isProductive(Sym.sym("S")));
        Assertions.assertFalse(Parsewisp.parser("S = A B; A = 'a'; B = C; C = B", noCheckOpts).grammar().analyze().isProductive(Sym.sym("S")));
    }
}
