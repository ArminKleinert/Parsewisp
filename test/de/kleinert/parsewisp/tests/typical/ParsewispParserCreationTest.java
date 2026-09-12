package de.kleinert.parsewisp.tests.typical;

import de.kleinert.parsewisp.Parsewisp;
import de.kleinert.parsewisp.Sym;
import de.kleinert.parsewisp.error.IllegalGrammarException;
import de.kleinert.parsewisp.error.ParserCreationFailure;
import de.kleinert.parsewisp.parser_options.ParserCreationOptions;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

class ParsewispParserCreationTest {

    @Test
    void parserFrom() {
    }

    @Test
    void parserFromString() {
        {
            final @NotNull var p = Parsewisp.parser("S = '1'");
            final @NotNull var p2 = Parsewisp.parser("S = '1'");
            Assertions.assertEquals(p, p2);
        }
    }

    @Test
    void parserFromFile() {
        try {

            final @NotNull String text = "aaaaabbbaaaabb";
            final @NotNull var grammarFile = new File("testres/grammars/as_and_bs.g");
            final @NotNull var p = Parsewisp.parser(Files.readString(grammarFile.toPath()));
            final @NotNull var grammarText = Files.readString(grammarFile.toPath());

            Assertions.assertEquals(
                    Parsewisp.parser(grammarText).parse(text),
                    p.parse(text)
            );
            Assertions.assertEquals(
                    Parsewisp.parser(grammarText),
                    p
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            final @NotNull var grammarFile = new File("testres/grammars/as_and_bs.g");
            final @NotNull var p = Parsewisp.parser(Files.readString(grammarFile.toPath()));
            Assertions.assertEquals(p, Parsewisp.parser(Files.readString(grammarFile.toPath())));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            final @NotNull var grammarFile = new File("testres/grammars/c99.g");
            final @NotNull var pFromString = Parsewisp.parser(
                    Files.readString(grammarFile.toPath()),
                    ParserCreationOptions.newWithStandardWhitespace());
            final @NotNull var pFromFile = Parsewisp.parser(
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
                () -> Parsewisp.parser(grammar));
    }

    @Test
    void failBecauseNoTerminals() {
        // Error: Illegal grammar
        final @NotNull var grammar = "S := S";
        Assertions.assertThrows(
                IllegalGrammarException.class,
                () -> Parsewisp.parser(grammar));
    }

    @Test
    void parserCreationFail() {
        {
            // Error: Starting symbol not in grammar
            final @NotNull var grammar = "S = 'abc'";
            final @NotNull var options = ParserCreationOptions.create(
                    null, Sym.sym("C"),
                    null,
                    true,
                    null);
            Assertions.assertThrows(
                    ParserCreationFailure.class,
                    () -> Parsewisp.parser(grammar, options));
        }
    }

    @Test
    void withRuleDefinitionOps() {
        var dOpts = ParserCreationOptions.getDefault().withRuleDefinitionOps(List.of("::=", ":=", "=", ":", "→", "->", "-->"));

        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> Parsewisp.parser("S = \"a\"", dOpts.withRuleDefinitionOps(List.of())));

        // Using an operator that can't be used leads to an error.
        Assertions.assertThrows(
                ParserCreationFailure.class,
                () -> Parsewisp.parser("S = \"a\"", dOpts.withRuleDefinitionOps(List.of("→"))));

        // Using an operator that is okay is valid.
        Assertions.assertDoesNotThrow(() -> Parsewisp.parser("S = \"a\"", dOpts.withRuleDefinitionOps(List.of("="))));

        // Define a bunch of equal parsers and extract the grammars.
        var g1 = Parsewisp.parser("S ::= \"a\"", dOpts).grammar();
        var g2 = Parsewisp.parser("S := \"a\"", dOpts).grammar();
        var g3 = Parsewisp.parser("S = \"a\"", dOpts).grammar();
        var g4 = Parsewisp.parser("S : \"a\"", dOpts).grammar();
        var g5 = Parsewisp.parser("S → \"a\"", dOpts).grammar();
        var g6 = Parsewisp.parser("S -> \"a\"", dOpts).grammar();
        var g7 = Parsewisp.parser("S --> \"a\"", dOpts).grammar();

        // Check equivalences of the grammars.
        Assertions.assertEquals(g1, g2);
        Assertions.assertEquals(g1, g3);
        Assertions.assertEquals(g1, g4);
        Assertions.assertEquals(g1, g5);
        Assertions.assertEquals(g1, g6);
        Assertions.assertEquals(g1, g7);
    }
}