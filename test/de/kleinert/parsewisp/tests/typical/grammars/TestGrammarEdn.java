package de.kleinert.parsewisp.tests.typical.grammars;

import de.kleinert.parsewisp.Parsewisp;
import de.kleinert.parsewisp.parser.Parser;
import de.kleinert.parsewisp.testutil.PT;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

class TestGrammarEdn {
    private @NotNull Parser parser() {
        try {
            return Parsewisp.parser(
                    Files.readString(Path.of("testres/grammars/edn.g"))
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private @NotNull List<String> symbolList() {
        return List.of("a", "+", "*", "<a>", "a:", "+:", "*#", "<a>#");
    }

    private @NotNull List<String> symbolListWithNS() {
        return symbolList().stream().map(it -> it + '/' + "sym").toList();
    }

    private @NotNull List<String> keywordList() {
        return symbolList().stream().map(it -> ':' + it).toList();
    }

    private @NotNull List<String> keywordListWithNS() {
        return symbolListWithNS().stream().map(it -> ':' + it).toList();
    }

    @Test
    void verifyWithFile() throws IOException {
        var parser = parser();
        var text = Files.readString(Path.of("testres/other/edntest.edn"));
        Assertions.assertTrue(parser.parse(text).isSuccess());
    }

    @Test
    void verifyLineComment() {
        var parser = parser();
        var text = "1 ;abf\n 2";
        Assertions.assertEquals(
                PT.create("Edn",
                        PT.create("expression",
                                PT.create("Number", PT.create("Int", "1"))),
                        PT.create("expression",
                                PT.create("Number", PT.create("Int", "2")))),
                parser.parse(text)
        );
    }

    @Test
    void verifyLineComment2() {
        var parser = parser();
        var text = "3 #_1 4 ; abf\n";
        Assertions.assertEquals(
                PT.create("Edn",
                        PT.create("expression",
                                PT.create("Number", PT.create("Int", "3"))),
                        PT.create("expression",
                                PT.create("Number", PT.create("Int", "4")))),
                parser.parse(text)
        );
    }

    @Test
    void verifyLineComment3() {
        var parser = parser();
        var text = "1.3#_2 #_4 ; #_abf\n5";
        Assertions.assertEquals(
                PT.create("Edn",
                        PT.create("expression",
                                PT.create("Number", PT.create("Float", "1.3"))),
                        PT.create("expression",
                                PT.create("Number", PT.create("Int", "5")))),
                parser.parse(text)
        );
    }

    @Test
    void verifyLineCommentFollowedByEof() {
        var parser = parser();
        var text = "1.3#_2 #_4 ; #_abf";
        Assertions.assertEquals(
                PT.create("Edn",
                        PT.create("expression",
                                PT.create("Number", PT.create("Float", "1.3")))),
                parser.parse(text)
        );
    }

    @Test
    void verifySymbol() {
        var parser = parser();
        for (String s : symbolList()) {
            Assertions.assertEquals(
                    PT.create("Edn",
                            PT.create("expression",
                                    PT.create("Symbol", s))),
                    parser.parse(s)
            );
        }
    }

    @Test
    void verifySymbolWithNS() {
        var parser = parser();
        for (String s : symbolListWithNS()) {
            var parts = s.split("/");
            Assertions.assertEquals(
                    PT.create("Edn",
                            PT.create("expression",
                                    PT.create("Symbol", parts[0], parts[1]))),
                    parser.parse(s)
            );
        }
    }

    @Test
    void verifyKeyword() {
        var parser = parser();
        for (String s : keywordList()) {
            Assertions.assertEquals(
                    PT.create("Edn",
                            PT.create("expression",
                                    PT.create("Keyword", PT.create("Symbol", s.substring(1))))),
                    parser.parse(s)
            );
        }
    }

    @Test
    void verifyKeywordWithNS() {
        var parser = parser();
        for (String s : keywordListWithNS()) {
            var parts = s.substring(1).split("/");
            Assertions.assertEquals(
                    PT.create("Edn",
                            PT.create("expression",
                                    PT.create("Keyword", PT.create("Symbol", parts[0], parts[1])))),
                    parser.parse(s)
            );
        }
    }

    @Test
    void verifyList() {
        var parser = parser();

        Assertions.assertEquals(
                PT.create("Edn", PT.create("expression", PT.create("List"))),
                parser.parse("()"));
        Assertions.assertEquals(
                PT.create("Edn",
                        PT.create("expression",
                                PT.create("List",
                                        PT.create("expression",
                                        PT.create("Number", PT.create("Int", "12")))))),
                parser.parse("(12)"));

        var tree_1_2 =
                PT.create("Edn",
                        PT.create("expression",
                                PT.create("List",
                                        PT.create("expression",
                                                PT.create("Number", PT.create("Int", "1"))),
                                        PT.create("expression",
                                                PT.create("Number", PT.create("Int", "2"))))));
        Assertions.assertEquals(tree_1_2, parser.parse("(1 2)"));
        Assertions.assertEquals(tree_1_2, parser.parse("( 1 2 )"));

        Assertions.assertEquals(
                PT.create("Edn",
                        PT.create("expression",
                                PT.create("List",
                                        PT.create("expression",
                                        PT.create("List",
                                        PT.create("expression",
                                                PT.create("Number", PT.create("Int", "1"))))),
                                        PT.create("expression",
                                                PT.create("List"))))),
                parser.parse("( (1) () )"));
    }

    @Test
    void verifyVector() {
        var parser = parser();

        Assertions.assertEquals(
                PT.create("Edn", PT.create("expression", PT.create("Vector"))),
                parser.parse("[]"));
        Assertions.assertEquals(
                PT.create("Edn",
                        PT.create("expression",
                                PT.create("Vector",
                                        PT.create("expression",
                                                PT.create("Number", PT.create("Int", "12")))))),
                parser.parse("[12]"));

        var tree_1_2 =
                PT.create("Edn",
                        PT.create("expression",
                                PT.create("Vector",
                                        PT.create("expression",
                                                PT.create("Number", PT.create("Int", "1"))),
                                        PT.create("expression",
                                                PT.create("Number", PT.create("Int", "2"))))));
        Assertions.assertEquals(tree_1_2, parser.parse("[1 2]"));
        Assertions.assertEquals(tree_1_2, parser.parse("[ 1 2 ]"));

        Assertions.assertEquals(
                PT.create("Edn",
                        PT.create("expression",
                                PT.create("Vector",
                                        PT.create("expression",
                                                PT.create("Vector",
                                                        PT.create("expression",
                                                                PT.create("Number", PT.create("Int", "1"))))),
                                        PT.create("expression",
                                                PT.create("Vector"))))),
                parser.parse("[ [1] [] ]"));
    }

    @Test
    void verifyMap() {
        var parser = parser();
        Assertions.assertTrue(parser.parse("{}").isSuccess());
        Assertions.assertTrue(parser.parse("{12}").isFailure());
        Assertions.assertTrue(parser.parse("{1 2}").isSuccess());
        Assertions.assertTrue(parser.parse("{ (1) []}").isSuccess());
    }

    @Test
    void verifyTypeDispatch() {
        var parser = parser();
        Assertions.assertTrue(parser.parse("#m/n {}").isSuccess());
        Assertions.assertTrue(parser.parse("#m/n {:name \"abc\"}").isSuccess());
    }

    @Test
    void verifyInts() {
        var parser = parser();
        Assertions.assertTrue(parser.parse("0").isSuccess());
        Assertions.assertTrue(parser.parse("+0").isSuccess());
        Assertions.assertTrue(parser.parse("-0").isSuccess());
        Assertions.assertTrue(parser.parse("1").isSuccess());
        Assertions.assertTrue(parser.parse("+1").isSuccess());
        Assertions.assertTrue(parser.parse("-1").isSuccess());
    }

    @Test
    void verifyFloat() {
        var parser = parser();
        Assertions.assertTrue(parser.parse("1.0").isSuccess());
        Assertions.assertTrue(parser.parse("+1.0").isSuccess());
        Assertions.assertTrue(parser.parse("-1.0").isSuccess());
    }

    @Test
    void verifyFloatExpFormat() {
        var parser = parser();
        Assertions.assertTrue(parser.parse("1e+1").isSuccess());
        Assertions.assertTrue(parser.parse("1e-1").isSuccess());
        Assertions.assertTrue(parser.parse("1E+1").isSuccess());
        Assertions.assertTrue(parser.parse("1E-1").isSuccess());
        Assertions.assertTrue(parser.parse("1.0e+1").isSuccess());
        Assertions.assertTrue(parser.parse("1.0e-1").isSuccess());
        Assertions.assertTrue(parser.parse("1.0E+1").isSuccess());
        Assertions.assertTrue(parser.parse("1.0E-1").isSuccess());

        Assertions.assertTrue(parser.parse("+1e+1").isSuccess());
        Assertions.assertTrue(parser.parse("+1e-1").isSuccess());
        Assertions.assertTrue(parser.parse("+1E+1").isSuccess());
        Assertions.assertTrue(parser.parse("+1E-1").isSuccess());
        Assertions.assertTrue(parser.parse("+1.0e+1").isSuccess());
        Assertions.assertTrue(parser.parse("+1.0e-1").isSuccess());
        Assertions.assertTrue(parser.parse("+1.0E+1").isSuccess());
        Assertions.assertTrue(parser.parse("+1.0E-1").isSuccess());

        Assertions.assertTrue(parser.parse("-1e+1").isSuccess());
        Assertions.assertTrue(parser.parse("-1e-1").isSuccess());
        Assertions.assertTrue(parser.parse("-1E+1").isSuccess());
        Assertions.assertTrue(parser.parse("-1E-1").isSuccess());
        Assertions.assertTrue(parser.parse("-1.0e+1").isSuccess());
        Assertions.assertTrue(parser.parse("-1.0e-1").isSuccess());
        Assertions.assertTrue(parser.parse("-1.0E+1").isSuccess());
        Assertions.assertTrue(parser.parse("-1.0E-1").isSuccess());
    }
}
