package alphaparse.tests.typical;

import alphaparse.Alpha;
import alphaparse.Sym;
import alphaparse.error.IllegalGrammarException;
import alphaparse.error.ParserCreationFailure;
import alphaparse.parser_options.GlobalCaseInsensitivity;
import alphaparse.parser_options.ParserCreationOptions;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

class AlphaParserCreationTest {

    @Test
    void parserFrom() {
    }

    @Test
    void parserFromString() {
        {
            final @NotNull var p = Alpha.parser("S = '1'");
            final @NotNull var p2 = Alpha.parser("S = '1'");
            Assertions.assertEquals(p, p2);
        }
    }

    @Test
    void parserFromFile() {
        try {

            final @NotNull String text = "aaaaabbbaaaabb";
            final @NotNull var grammarFile = new File("testres/grammars/as_and_bs.g");
            final @NotNull var p = Alpha.parser(Files.readString(grammarFile.toPath()));
            final @NotNull var grammarText = Files.readString(grammarFile.toPath());

            Assertions.assertEquals(
                    Alpha.parser(grammarText).parse(text),
                    p.parse(text)
            );
            Assertions.assertEquals(
                    Alpha.parser(grammarText),
                    p
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            final @NotNull var grammarFile = new File("testres/grammars/as_and_bs.g");
            final @NotNull var p = Alpha.parser(Files.readString(grammarFile.toPath()));
            Assertions.assertEquals(p, Alpha.parser(Files.readString(grammarFile.toPath())));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            final @NotNull var grammarFile = new File("testres/grammars/c99.g");
            final @NotNull var pFromString = Alpha.parser(
                    Files.readString(grammarFile.toPath()),
                    ParserCreationOptions.newWithStandardWhitespace());
            final @NotNull var pFromFile = Alpha.parser(
                    Files.readString(grammarFile.toPath()),
                    ParserCreationOptions.newWithStandardWhitespace());
            final @NotNull var text = "void a(){}";
            Assertions.assertEquals(pFromString.parses(text), pFromFile.parses(text));
            Assertions.assertEquals(pFromString, pFromFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void parserFromStringWithOptions() {
    }
/*
@Nullable Parser whitespaceParser,
@Nullable Keyword startProduction,
@NotNull Cfg.GlobalCaseInsensitivity stringCaseInsensitive,
@NotNull ReductionType.ReductionTypesAvailable outputFormat
*/

    @Test
    void parserFromFileWithOptions() {
    }

    @Test
    void parserFromGrammarWithOptions() {
    }

    @Test
    void failBecauseOfUndefinedNT() {
        // Error: Illegal grammar
        final @NotNull var grammar = "S := A";
        Assertions.assertThrows(
                IllegalGrammarException.class,
                () -> Alpha.parser(grammar));
    }

    @Test
    void failBecauseNoTerminals() {
        // Error: Illegal grammar
        final @NotNull var grammar = "S := S";
        Assertions.assertThrows(
                IllegalGrammarException.class,
                () -> Alpha.parser(grammar));
    }

    @Test
    void parserCreationFail() {
        {
            // Error: Starting symbol not in grammar
            final @NotNull var grammar = "S = 'abc'";
            final @NotNull var options = ParserCreationOptions.create(
                    null, Sym.sym("C"),
                    GlobalCaseInsensitivity.DEFAULT,
                    null,
                    null,
                    true,
                    null,
                    null);
            Assertions.assertThrows(
                    ParserCreationFailure.class,
                    () -> Alpha.parser(grammar, options));
        }
    }

    @Test
    void withRuleDefinitionOps() {
        var dOpts = ParserCreationOptions.getDefault().withRuleDefinitionOps(List.of("::=", ":=", "=", ":", "→", "->", "-->"));

        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> Alpha.parser("S = \"a\"", dOpts.withRuleDefinitionOps(List.of())));

        // Using an operator that can't be used leads to an error.
        Assertions.assertThrows(
                ParserCreationFailure.class,
                () -> Alpha.parser("S = \"a\"", dOpts.withRuleDefinitionOps(List.of("→"))));

        // Using an operator that is okay is valid.
        Assertions.assertDoesNotThrow(() -> Alpha.parser("S = \"a\"", dOpts.withRuleDefinitionOps(List.of("="))));

        // Define a bunch of equal parsers and extract the grammars.
        var g1 = Alpha.parser("S ::= \"a\"", dOpts).grammar();
        var g2 = Alpha.parser("S := \"a\"", dOpts).grammar();
        var g3 = Alpha.parser("S = \"a\"", dOpts).grammar();
        var g4 = Alpha.parser("S : \"a\"", dOpts).grammar();
        var g5 = Alpha.parser("S → \"a\"", dOpts).grammar();
        var g6 = Alpha.parser("S -> \"a\"", dOpts).grammar();
        var g7 = Alpha.parser("S --> \"a\"", dOpts).grammar();

        // Check equivalences of the grammars.
        Assertions.assertEquals(g1, g2);
        Assertions.assertEquals(g1, g3);
        Assertions.assertEquals(g1, g4);
        Assertions.assertEquals(g1, g5);
        Assertions.assertEquals(g1, g6);
        Assertions.assertEquals(g1, g7);
    }

    @Test
    void withEpsilonNames() {
        var dOpts = ParserCreationOptions.getDefault().withEpsilonNames(List.of("Epsilon", "epsilon", "EPSILON", "eps", "ε"));

        // Using an epsilon that can't be used leads to an error.
        Assertions.assertThrows(
                IllegalGrammarException.class,
                () -> Alpha.parser("S = Epsilon", dOpts.withEpsilonNames(List.of("ε"))));

        // Using an epsilon that is okay is valid.
        Assertions.assertDoesNotThrow(() -> Alpha.parser("S = ε", dOpts.withEpsilonNames(List.of("ε"))));

        // Define a bunch of equal parsers and extract the grammars.
        var g1 = Alpha.parser("S = ε", dOpts).grammar();
        var g2 = Alpha.parser("S = eps", dOpts).grammar();
        var g3 = Alpha.parser("S = EPSILON", dOpts).grammar();
        var g4 = Alpha.parser("S = epsilon", dOpts).grammar();
        var g5 = Alpha.parser("S = Epsilon", dOpts).grammar();

        // Check equivalences of the grammars.
        Assertions.assertEquals(g1, g2);
        Assertions.assertEquals(g1, g3);
        Assertions.assertEquals(g1, g4);
        Assertions.assertEquals(g1, g5);
    }
}