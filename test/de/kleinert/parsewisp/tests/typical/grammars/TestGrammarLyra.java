package de.kleinert.parsewisp.tests.typical.grammars;

import de.kleinert.parsewisp.Parsewisp;
import de.kleinert.parsewisp.parser.Parser;
import de.kleinert.parsewisp.parser_options.ParserCreationOptions;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Test(s) for the Lyra grammar.
 * <p>
 * Grammar and tests from <a href="https://esolangs.org/wiki/Brainfuck#Examples">esolangs.org/wiki/Brainfuck</a>.
 */
class TestGrammarLyra {
    private @NotNull Parser parser() {
        try {
            return Parsewisp.parser(
                    Files.readString(Path.of("testres/grammars/lyra.g")),
                            ParserCreationOptions.newWithStandardWhitespace()
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void helloWorld() {
        var text = "(println! \"Hello World!\")";
        //System.out.println(parser().parse(text));
    }
}