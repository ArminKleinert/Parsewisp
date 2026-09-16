package de.kleinert.parsewisp.tests.typical;

import de.kleinert.parsewisp.Parsewisp;
import de.kleinert.parsewisp.Sym;
import de.kleinert.parsewisp.grammar.Grammar;
import de.kleinert.parsewisp.grammar.GrammarBuilder;
import de.kleinert.parsewisp.parser_options.ParserCreationOptions;
import de.kleinert.parsewisp.parsing.*;
import de.kleinert.parsewisp.testutil.PT;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

class GrammarBuilderTest {
    @Test
    void equivalentToStringGrammar() {
        var gFromString = Parsewisp.parser(
                        """
                                S = NUMBER NUMBER*
                                NUMBER = '0' | '1' | '2' | '3' | '4' | '5' | '6' | '7' | '8' | '9'
                                """)
                .grammar();
        var gFromGB = new GrammarBuilder() {
            @Override
            protected void make() {
                addProduction("S", cat(nt("NUMBER"), repMin(nt(Sym.sym("NUMBER")), 0)));
                addProduction("NUMBER", alternationGuaranteeDistinctAndNotEmpty(Stream.of("0", "1", "2", "3", "4", "5", "6", "7", "8", "9").map(this::stringCS).toList()));
            }
        }.build();

        Assertions.assertEquals(gFromString, gFromGB);

        var pFromString = Parsewisp.parser(gFromString, ParserCreationOptions.getDefault().withStartProduction(Sym.sym("S")));
        var pFromGB = Parsewisp.parser(gFromGB, ParserCreationOptions.getDefault().withStartProduction(Sym.sym("S")));
        var text = "0123456789";
        Assertions.assertEquals(pFromString.parse(text), pFromGB.parse(text));
    }

    @Test
    void equivalentToMoreExplicitGrammar() {
        var pGrammarList = new LinkedHashMap<Sym, Rule>();
        pGrammarList.put(Sym.sym("S"),
                ConcatRule.create(List.of(NonTerminal.create(Sym.sym("NUMBER")), ZeroOrMoreRule.create(NonTerminal.create(Sym.sym("NUMBER"))))));
        pGrammarList.put(Sym.sym("NUMBER"), AlternationRule.create(Stream.of("0", "1", "2", "3", "4", "5", "6", "7", "8", "9").map(it -> StringTerm.create(it, false)).toList()));
        var pFromGrammar = Parsewisp.parser(
                        new Grammar(Sym.sym("S"), pGrammarList),
                        ParserCreationOptions.getDefault().withStartProduction(Sym.sym("S")))
                .grammar();

        var pFromGB = new GrammarBuilder() {
            @Override
            protected void make() {
                addProduction("S", cat(nt("NUMBER"), zeroOrMore(nt(Sym.sym("NUMBER")))));
                addProduction("NUMBER", alternationGuaranteeDistinctAndNotEmpty(Stream.of("0", "1", "2", "3", "4", "5", "6", "7", "8", "9").map(this::stringCS).toList()));
            }
        }.build();

        Assertions.assertEquals(pFromGrammar, pFromGB);
    }

    @Test
    void testGrammarBuilderFeatures() {
        // A grammar which has 9 different ways to match at least one number/underscore.
        var gFromGB = new GrammarBuilder() {
            @Override
            protected void make() {
                addProduction("S", ordAlt(List.of(
                        nt("A"), nt("B"), nt("C"), nt("D"),
                        nt("E"), nt("F"), nt("G"), nt("H"),
                        nt("I"), nt("J"), nt("K"), eof())
                ));
                addProduction("A", cat(rep(regex("[0-9_]"), 1, Integer.MAX_VALUE)));
                addProduction("B", cat(regex("[0-9_]"), repMax(string(""), 0), repMax(regex("[0-9_]"), Integer.MAX_VALUE)));
                addProduction("C", cat(repMin(regex("[0-9_]"), 1)));
                addProduction("D", cat(regex("[0-9_]"), zeroOrMore(regex("[0-9_]"))));
                addProduction("E", cat(rep(regex("[0-9_]"), 1), rep(regex("[0-9_]"), 0), zeroOrMore(regex("[0-9_]"))));
                addProduction("F", repMin(alt(numVal('0', '9'), numVal(0x5F), numVal(0x60)), 1));
                addProduction("G", onceOrMore(alternationGuaranteeDistinctAndNotEmpty(Stream.concat(
                                IntStream.range('0', '9' + 1).boxed(),
                                Stream.of((int) '_'))
                        .map(i -> String.valueOf((char) i.intValue()))
                        .map(this::of)
                        .collect(Collectors.toList()))));
                addProduction("H", onceOrMore(alternationGuaranteeDistinctAndNotEmpty(Stream.concat(
                                IntStream.range('0', '9' + 1).boxed(),
                                Stream.of((int) '_'))
                        .map(i -> String.valueOf((char) i.intValue()))
                        .map(this::of)
                        .collect(Collectors.toList()))));
                addProduction("I", cat(regex("[0-9_]"), opt(onceOrMore(regex("[0-9_]")))));
                addProduction("J", onceOrMore(oneOf("0123456789")));
                addProduction("K", onceOrMore(oneOf("0123456789abcdefABCDEF")));
            }
        }.build();
        var p = Parsewisp.parser(gFromGB, ParserCreationOptions.getDefault());

        Assertions.assertEquals(
                List.of(PT.create("S", PT.create("A", "9", "9")), PT.create("S", PT.create("B", "9", "9")),
                        PT.create("S", PT.create("C", "9", "9")), PT.create("S", PT.create("D", "9", "9")),
                        PT.create("S", PT.create("E", "9", "9")), PT.create("S", PT.create("F", "9", "9")),
                        PT.create("S", PT.create("G", "9", "9")), PT.create("S", PT.create("H", "9", "9")),
                        PT.create("S", PT.create("I", "9", "9")),
                        PT.create("S", PT.create("J", "9", "9")),
                        PT.create("S", PT.create("K", "9", "9"))),
                Parsewisp.parser(gFromGB, ParserCreationOptions.getDefault()).parses("99"));
        Assertions.assertEquals(List.of(PT.create("S")), p.parses(""));
    }
}