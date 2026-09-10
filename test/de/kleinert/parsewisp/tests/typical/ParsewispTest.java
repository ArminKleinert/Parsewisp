package de.kleinert.parsewisp.tests.typical;

import de.kleinert.parsewisp.Parsewisp;
import de.kleinert.parsewisp.Sym;
import de.kleinert.parsewisp.error.ParserCreationFailure;
import de.kleinert.parsewisp.parser_options.ParserCreationOptions;
import de.kleinert.parsewisp.parser_options.ParsingOptions;
import de.kleinert.parsewisp.parser_options.Unhide;
import de.kleinert.parsewisp.result.Node;
import de.kleinert.parsewisp.testutil.PT;
import de.kleinert.parsewisp.result.ParseTree;

import java.util.HashSet;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class ParsewispTest {
    @Test
    void simplifiedParseTreeCreation() {
        var pt1 = PT.create("S", "a", "a");
        var pt2 = PT.create(Sym.sym("S"), List.of(Node.of("a"), Node.of("a")));
        Assertions.assertEquals(pt2, pt1);
    }

    @Test
    void singleOrDoubleQuotationEquivalenceForStrings() {
        var pSingleQuoted = """
                S = 'a' 'b"c\\''
                """;
        var pDoubleQuoted = """
                S = "a" "b\\"c'"
                """;

        // Valid parse
        Assertions.assertEquals(
                Parsewisp.parser(pSingleQuoted).parse("ab\"c'"),
                Parsewisp.parser(pDoubleQuoted).parse("ab\"c'"));

        // Invalid parse
        Assertions.assertEquals(
                Parsewisp.parser(pSingleQuoted).parse(""),
                Parsewisp.parser(pDoubleQuoted).parse(""));

        // The parsers are the same on the inside.
        Assertions.assertEquals(
                Parsewisp.parser(pSingleQuoted),
                Parsewisp.parser(pDoubleQuoted));
    }

    @Test
    void singleOrDoubleQuotationEquivalenceForRegexes() {
        var pSingleQuoted = """
                S = #'a' #'b"c\\''
                """;
        var pDoubleQuoted = """
                S = #"a" #"b\\"c'"
                """;

        // Valid parse
        Assertions.assertEquals(
                Parsewisp.parser(pSingleQuoted).parse("ab\"c'"),
                Parsewisp.parser(pDoubleQuoted).parse("ab\"c'"));

        // Invalid parse
        Assertions.assertEquals(
                Parsewisp.parser(pSingleQuoted).parse(""),
                Parsewisp.parser(pDoubleQuoted).parse(""));

        // The parsers are the same on the inside.
        Assertions.assertEquals(
                Parsewisp.parser(pSingleQuoted),
                Parsewisp.parser(pDoubleQuoted));
    }

    @Test
    void testParserCreationNewWithStandardWhitespace() {
        var p = Parsewisp.parser(
                "S = ('a' | 'b')*",
                ParserCreationOptions.newWithStandardWhitespace()
        );
        var tree = PT.create("S", "a", "b", "a", "b", "a");
        Assertions.assertEquals(tree, p.parse("a b      a\tb\na"));
    }

    @Test
    void testUnhideOptionsNone() {
        var p = Parsewisp.parser("S = 'a' <B> C <D> 'a'\nB = 'b'+\n<C> = 'c'\n<D> = 'd'");
        var opts = ParsingOptions.getDefault().withUnhide(Unhide.UnhideOptions.NONE);
        var tree = PT.create("S", "a", "c", "a");
        Assertions.assertEquals(tree, p.parse("abcda", opts));
    }

    @Test
    void testUnhideOptionsTags() {
        var p = Parsewisp.parser("S = 'a' <B> C <D> 'a'\nB = 'b'+\n<C> = 'c'\n<D> = 'd'");
        var opts = ParsingOptions.getDefault().withUnhide(Unhide.UnhideOptions.TAGS);
        var tree = PT.create("S", "a", PT.create("C", "c"), "a");
        Assertions.assertEquals(tree, p.parse("abcda", opts));
    }

    @Test
    void testUnhideOptionsContent() {
        var p = Parsewisp.parser("S = 'a' <B> C <D> 'a'\nB = 'b'+\n<C> = 'c'\n<D> = 'd'");
        var opts = ParsingOptions.getDefault().withUnhide(Unhide.UnhideOptions.CONTENT);
        var tree = PT.create("S", "a", PT.create("B", "b"), "c", "d", "a");
        Assertions.assertEquals(tree, p.parse("abcda", opts));
    }

    @Test
    void testUnhideOptionsAll() {
        var p = Parsewisp.parser("S = 'a' <B> C <D> 'a'\nB = 'b'+\n<C> = 'c'\n<D> = 'd'");
        var opts = ParsingOptions.getDefault().withUnhide(Unhide.UnhideOptions.ALL);
        var tree = PT.create("S", "a", PT.create("B", "b"), PT.create("C", "c"), PT.create("D", "d"), "a");
        Assertions.assertEquals(tree, p.parse("abcda", opts));
    }

    @Test
    void testUnhideOptionsInOneCase() {
        var p = Parsewisp.parser("S = 'a' <B> C <D> 'a'\nB = 'b'+\n<C> = 'c'\n<D> = 'd'");

        Assertions.assertEquals(
                PT.create("S", "a", "c", "a"),
                p.parse("abcda", ParsingOptions.getDefault().withUnhide(Unhide.UnhideOptions.NONE)));

        Assertions.assertEquals(
                PT.create("S", "a", PT.create("C", "c"), "a"),
                p.parse("abcda", ParsingOptions.getDefault().withUnhide(Unhide.UnhideOptions.TAGS)));

        Assertions.assertEquals(
                PT.create("S", "a", PT.create("B", "b"), "c", "d", "a"),
                p.parse("abcda", ParsingOptions.getDefault().withUnhide(Unhide.UnhideOptions.CONTENT)));

        Assertions.assertEquals(
                PT.create("S",
                        "a",
                        PT.create("B", "b"),
                        PT.create("C", "c"),
                        PT.create("D", "d"),
                        "a"),
                p.parse("abcda", ParsingOptions.getDefault().withUnhide(Unhide.UnhideOptions.ALL)));
    }

    @Test
    void testPartialParseOptionIgnoredOnSingleParse() {
        {
            var p = Parsewisp.parser("S = 'a'+");
            var opts = ParsingOptions.getDefault().withPartial(true);
            Assertions.assertEquals(PT.create("S", "a", "a"), p.parse("aa", opts));
        }
        {
            var p = Parsewisp.parser("S = 'a'");
            var opts = ParsingOptions.getDefault().withPartial(true);
            Assertions.assertTrue(p.parse("aa", opts).isFailure());
        }
    }

    @Test
    void testPartialParseOptionIfNotInGrammar() {
        {
            var p = Parsewisp.parser("S = 'a'");
            var opts = ParsingOptions.getDefault().withPartial(true);
            Assertions.assertEquals(List.of(PT.create("S", "a")), p.parses("aa", opts));
        }
    }

    @Test
    void parserCreationWithExplicitStartProduction() {
        {
            final var opts = ParserCreationOptions.getDefault().withStartProduction(Sym.sym("B"));
            final @NotNull var p = Parsewisp.parser("A = 'a'\nB = 'b'", opts);

            Assertions.assertEquals(p.startProduction(), opts.startProduction());

            Assertions.assertTrue(p.parse("a", ParsingOptions.getDefault()).isFailure());
            Assertions.assertEquals(PT.create("B", "b"), p.parse("b", ParsingOptions.getDefault()));
        }
        {
            // The production is not in the grammar => Fail
            final var opts = ParserCreationOptions.getDefault().withStartProduction(Sym.sym("B"));
            Assertions.assertThrows(ParserCreationFailure.class, () -> Parsewisp.parser("A = 'a'", opts));
        }
    }

    @Test
    void parseWithExplicitStartProduction() {
        {
            final @NotNull var p = Parsewisp.parser("A = 'a'\nB = 'b'");

            final var opts = ParsingOptions.getDefault().withStart(Sym.sym("B"));

            Assertions.assertTrue(p.parse("b", ParsingOptions.getDefault()).isFailure());
            Assertions.assertEquals(PT.create("B", "b"), p.parse("b", opts));
        }
        {
            // The production is not in the grammar => Fail
            final var opts = ParsingOptions.getDefault().withStart(Sym.sym("B"));
            final @NotNull var p = Parsewisp.parser("A = 'a'");
            Assertions.assertThrows(ParserCreationFailure.class, () -> p.parse("a", opts));
        }
    }

    @Test
    void parse() {
        final @NotNull var p = Parsewisp.parser("S = 'A' | 'B' | S S");
        {
            final @NotNull var res = p.parse("A", ParsingOptions.getDefault());
            Assertions.assertEquals(PT.create("S", "A"), res);
        }
        {
            final @NotNull var res = p.parse("B", ParsingOptions.getDefault());
            Assertions.assertEquals(PT.create("S", "B"), res);
        }
        {
            final @NotNull var res = p.parse("AB", ParsingOptions.getDefault());
            Assertions.assertEquals(
                    PT.create("S", PT.create("S", "A"), PT.create("S", "B")),
                    res);
        }
    }

    @Test
    void parseCat() {
        {
            final @NotNull var p = Parsewisp.parser("S = 'A' 'B'");
            final @NotNull var res = p.parse("AB", ParsingOptions.getDefault());
            Assertions.assertEquals(PT.create("S", "A", "B"), res);
        }
        {
            final @NotNull var p = Parsewisp.parser("S = 'A' 'B' S | ε");
            Assertions.assertEquals(PT.create("S"), p.parse("", ParsingOptions.getDefault()));
            Assertions.assertEquals(PT.create("S", "A", "B", PT.create("S")), p.parse("AB", ParsingOptions.getDefault()));
        }
        {
            final @NotNull var p = Parsewisp.parser("S = 'a' 'a' 'a'");
            Assertions.assertTrue(p.parse("").isFailure());
            Assertions.assertTrue(p.parse("a").isFailure());
            Assertions.assertTrue(p.parse("aa").isFailure());
            Assertions.assertEquals(PT.create("S", "a", "a", "a"), p.parse("aaa"));
            Assertions.assertTrue(p.parse("aaaa").isFailure());
        }
    }

    @Test
    void parsePlus() {
        {
            final @NotNull var p = Parsewisp.parser("S = 'a'+");
            Assertions.assertTrue(p.parse("", ParsingOptions.getDefault()).isFailure());
            Assertions.assertEquals(PT.create("S", "a"), p.parse("a", ParsingOptions.getDefault()));
            Assertions.assertEquals(PT.create("S", "a", "a"), p.parse("aa", ParsingOptions.getDefault()));
            Assertions.assertEquals(PT.create("S", "a", "a", "a"), p.parse("aaa", ParsingOptions.getDefault()));
        }
        {
            final @NotNull var p = Parsewisp.parser("S = ('a' | 'b')+");
            Assertions.assertTrue(p.parse("", ParsingOptions.getDefault()).isFailure());
            Assertions.assertEquals(PT.create("S", "b"), p.parse("b", ParsingOptions.getDefault()));
            Assertions.assertEquals(PT.create("S", "a", "b", "a"), p.parse("aba", ParsingOptions.getDefault()));
        }
    }

    @Test
    void parseStar() {
        {
            final @NotNull var p = Parsewisp.parser("S = 'a'*");
            Assertions.assertEquals(PT.create("S"), p.parse("", ParsingOptions.getDefault()));
            Assertions.assertEquals(PT.create("S", "a"), p.parse("a", ParsingOptions.getDefault()));
            Assertions.assertEquals(PT.create("S", "a", "a"), p.parse("aa", ParsingOptions.getDefault()));
            Assertions.assertEquals(PT.create("S", "a", "a", "a"), p.parse("aaa", ParsingOptions.getDefault()));
        }
        {
            final @NotNull var p = Parsewisp.parser("S = ('a' | 'b')*");
            Assertions.assertEquals(PT.create("S"), p.parse("", ParsingOptions.getDefault()));
            Assertions.assertEquals(PT.create("S", "b"), p.parse("b", ParsingOptions.getDefault()));
            Assertions.assertEquals(PT.create("S", "a", "b", "a"), p.parse("aba", ParsingOptions.getDefault()));
        }
    }

    @Test
    void parseSimpleComplex() {
        {
            final @NotNull var p = Parsewisp.parser("S = ε | S");
            var forest = p.parses("").castToParsesSuccess();
            Assertions.assertEquals(
                    List.of(
                            PT.create("S"), PT.create("S", PT.create("S")),
                            PT.create("S", PT.create("S", PT.create("S"))),
                            PT.create("S", PT.create("S", PT.create("S", PT.create("S"))))
                    ),
                    forest.stream().limit(4).toList());
        }
    }

    @Test
    void parseSimpleString() {
        {
            final @NotNull var p = Parsewisp.parser("S = 'AB'");
            final @NotNull var res = p.parse("AB", ParsingOptions.getDefault());
            Assertions.assertEquals(PT.create("S", "AB"), res);
        }
        {
            final @NotNull var p = Parsewisp.parser("S = ''");
            final @NotNull var res = p.parse("", ParsingOptions.getDefault());
            Assertions.assertEquals(PT.create("S"), res);
        }
    }

    @Test
    void parsePartial() {
        {
            final @NotNull var p = Parsewisp.parser("S = ''");
            final @NotNull var res = p.parse("", ParsingOptions.getDefault());
            Assertions.assertEquals(PT.create("S"), res);
        }
        {
            final @NotNull var p = Parsewisp.parser("S = 'AB'");
            final @NotNull var res = p.parse("AB", ParsingOptions.getDefault());
            Assertions.assertEquals(PT.create("S", "AB"), res);
        }
    }

    @Test
    void parsesWithChoice() {
        {
            final @NotNull var p = Parsewisp.parser("S = 'A' | 'B' | S S");
            final @NotNull var res = p.parses("ABA", ParsingOptions.getDefault());
            final var possibleResults = new HashSet<>(sabssPossibleResults());

            // Using Sets because the order of results is implementation-dependent when using alternation rules.
            Assertions.assertEquals(possibleResults, new HashSet<>(res));
        }
    }

    @Test
    void parsesWithChoiceEps() {
        {
            final @NotNull var p = Parsewisp.parser("S = ε | A | B | C\nA = C \nB = C \nC = ε");
            final @NotNull var possibleTrees = Set.of(
                    PT.create("S"),
                    PT.create("S", PT.create("C")),
                    PT.create("S", PT.create("A", PT.create("C"))),
                    PT.create("S", PT.create("B", PT.create("C")))
            );
            Assertions.assertEquals(possibleTrees, new HashSet<>(p.parses("", ParsingOptions.getDefault())));
        }
        {
            final @NotNull var grammar = """
                    S  = (r1 | r2 | r3)* | ε
                    r1 = 'a'
                    r2 = 'a'
                    r3 = 'a'
                    """;
            final @NotNull var text = "aa";
            final @NotNull var p = Parsewisp.parser(grammar);
            final @NotNull var ps = new HashSet<>(p.parses(text, ParsingOptions.getDefault()));
            final @NotNull var possibleParses = new HashSet<>(r1r2r3Results());
            Assertions.assertEquals(possibleParses, ps);
        }
    }

    @Test
    void parsesWithOrderedChoice() {
        {
            final @NotNull var p = Parsewisp.parser("S = 'A' / 'B' / S S");
            final @NotNull var res = p.parses("ABA", ParsingOptions.getDefault());
            final @NotNull var possibleResults = sabssPossibleResults();

            Assertions.assertEquals(possibleResults, res);
        }

        {
            final @NotNull var p = Parsewisp.parser("""
                    S = A / B / C / D / E
                    A = ε
                    B = ε
                    C = ε
                    D = ε
                    E = ε
                    """);
            var expect = List.of(
                    PT.create("S", PT.create("A")),
                    PT.create("S", PT.create("B")),
                    PT.create("S", PT.create("C")),
                    PT.create("S", PT.create("D")),
                    PT.create("S", PT.create("E"))
            );
            Assertions.assertEquals(expect, p.parses("", ParsingOptions.getDefault()));
        }
        {
            final @NotNull var p = Parsewisp.parser("S = 'a' / ε / 'a'");
            final @NotNull var possibleTrees = List.of(PT.create("S", "a"));
            Assertions.assertEquals(possibleTrees, p.parses("a", ParsingOptions.getDefault()));
        }
        {
            final @NotNull var p = Parsewisp.parser("S = ε / 'a' / 'a' / ε");
            final @NotNull var possibleTrees = List.of(PT.create("S", "a"));
            Assertions.assertEquals(possibleTrees, p.parses("a", ParsingOptions.getDefault()));
        }
        {
            final @NotNull var grammar = """
                    S = (r1 / r2 / r3)*
                    r1 = 'a'
                    r2 = 'a'
                    r3 = 'a'
                    """;
            final @NotNull var text = "a";
            final @NotNull var p = Parsewisp.parser(grammar);
            final @NotNull var ps = p.parses(text, ParsingOptions.getDefault());
            final @NotNull var possibleParses = List.of(
                    PT.create("S", PT.create("r1", "a")),
                    PT.create("S", PT.create("r2", "a")),
                    PT.create("S", PT.create("r3", "a"))
            );
            Assertions.assertEquals(possibleParses, ps);
        }
        {
            final @NotNull var grammar = """
                    S = (r1 / r2)*
                    r1 = 'a'
                    r2 = 'a'
                    """;
            final @NotNull var text = "aa";
            final @NotNull var p = Parsewisp.parser(grammar);
            final @NotNull var ps = p.parses(text, ParsingOptions.getDefault());
            final @NotNull var possibleParses = List.of(
                    PT.create("S", PT.create("r1", "a"), PT.create("r1", "a")),
                    PT.create("S", PT.create("r2", "a"), PT.create("r1", "a")),
                    PT.create("S", PT.create("r1", "a"), PT.create("r2", "a")),
                    PT.create("S", PT.create("r2", "a"), PT.create("r2", "a"))
            );
            Assertions.assertEquals(possibleParses, ps);
        }
    }

    private @NotNull @Unmodifiable List<ParseTree> r1r2r3Results() {
        return List.of(
                PT.create("S", PT.create("r1", "a"), PT.create("r1", "a")),
                PT.create("S", PT.create("r2", "a"), PT.create("r1", "a")),
                PT.create("S", PT.create("r1", "a"), PT.create("r2", "a")),
                PT.create("S", PT.create("r2", "a"), PT.create("r2", "a")),
                PT.create("S", PT.create("r2", "a"), PT.create("r3", "a")),
                PT.create("S", PT.create("r1", "a"), PT.create("r3", "a")),
                PT.create("S", PT.create("r3", "a"), PT.create("r3", "a")),
                PT.create("S", PT.create("r3", "a"), PT.create("r2", "a")),
                PT.create("S", PT.create("r3", "a"), PT.create("r1", "a"))
        );
    }

    private @NotNull @Unmodifiable List<ParseTree> sabssPossibleResults() {
        return List.of(
                PT.create(
                        "S",
                        PT.create("S", "A"),
                        PT.create("S", PT.create("S", "B"), PT.create("S", "A"))
                ),
                PT.create(
                        "S",
                        PT.create("S", PT.create("S", "A"), PT.create("S", "B")),
                        PT.create("S", "A")
                )
        );
    }

    @Test
    void parsesPartial() {
        {
            final @NotNull var grammar = """
                    S  = (r1 / r2 / r3)*
                    r1 = 'a'
                    r2 = 'a'
                    r3 = 'a'
                    """;
            final @NotNull var text = "aa";
            final @NotNull var p = Parsewisp.parser(grammar);
            final @NotNull var ps = p.parses(text, new ParsingOptions(null, true, Unhide.UnhideOptions.NONE, false, false, false));
            final @NotNull var possibleParses = partialParsesOrderedR123();
            Assertions.assertEquals(possibleParses, ps);
        }
        {
            final @NotNull var grammar = """
                    S  = (r1 | r2 | r3)*
                    r1 = 'a'
                    r2 = 'a'
                    r3 = 'a'
                    """;
            final @NotNull var text = "aa";
            final @NotNull var p = Parsewisp.parser(grammar);
            final @NotNull var ps = new HashSet<>(p.parses(text, new ParsingOptions(null, true, Unhide.UnhideOptions.NONE, false, false, false)));
            final @NotNull var possibleParses = new HashSet<>(partialParsesOrderedR123());
            Assertions.assertEquals(possibleParses, ps);
        }
    }

    private @NotNull @Unmodifiable List<ParseTree> partialParsesOrderedR123() {
        return List.of(
                PT.create("S"),
                PT.create("S", PT.create("r1", "a")),
                PT.create("S", PT.create("r1", "a"), PT.create("r1", "a")),
                PT.create("S", PT.create("r2", "a")),
                PT.create("S", PT.create("r2", "a"), PT.create("r1", "a")),
                PT.create("S", PT.create("r1", "a"), PT.create("r2", "a")),
                PT.create("S", PT.create("r3", "a")),
                PT.create("S", PT.create("r2", "a"), PT.create("r2", "a")),
                PT.create("S", PT.create("r2", "a"), PT.create("r3", "a")),
                PT.create("S", PT.create("r1", "a"), PT.create("r3", "a")),
                PT.create("S", PT.create("r3", "a"), PT.create("r3", "a")),
                PT.create("S", PT.create("r3", "a"), PT.create("r2", "a")),
                PT.create("S", PT.create("r3", "a"), PT.create("r1", "a"))
        );
    }

    @Test
    void parserWithStart() {
        final @NotNull var p = Parsewisp.parser("S1 = 'A'\nS2 = 'B'");
        Assertions.assertEquals(PT.create("S1", "A"), p.parse("A"));
        Assertions.assertTrue(p.parse("B").isFailure());

        var parserWithOtherStart = Parsewisp.parser(
                "S1 = 'A'\nS2 = 'B'",
                ParserCreationOptions.getDefault().withStartProduction(Sym.sym("S2")));
        Assertions.assertTrue(parserWithOtherStart.parse("A").isFailure());
        Assertions.assertEquals(PT.create("S2", "B"), parserWithOtherStart.parse("B"));
    }

    @Test
    void parseWithStart() {
        final @NotNull var p = Parsewisp.parser("S1 = 'A'\nS2 = 'B'");
        Assertions.assertEquals(PT.create("S1", "A"), p.parse("A"));
        Assertions.assertTrue(p.parse("B").isFailure());

        var opts = ParsingOptions.getDefault().withStart(Sym.sym("S2"));
        Assertions.assertTrue(p.parse("A", opts).isFailure());
        Assertions.assertEquals(PT.create("S2", "B"), p.parse("B", opts));
    }
}