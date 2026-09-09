package de.kleinert.parsewisp.tests.typical.grammars;

import de.kleinert.parsewisp.Parsewisp;
import de.kleinert.parsewisp.parser.Parser;
import de.kleinert.parsewisp.parser_options.ParserCreationOptions;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Test(s) for the otc (Obfuscated Tiny C) grammar.
 * <p>
 * Grammar and tests from <a href="https://esolangs.org/wiki/Obfuscated_Tiny_C">esolangs.org/wiki/Obfuscated_Tiny_C</a>.
 */
class TestGrammarOTC {
    private @NotNull Parser parser() {
        try {
            return Parsewisp.parser(
                    Files.readString(Path.of("testres/grammars/otc.g")),
                    ParserCreationOptions.newWithStandardWhitespace()
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void verifySimpleProgram() {
        var text = "int i; int main (int argc, char** argv) { i = 11; while (--i) { printf(\"%d\", i); } }";
        Assertions.assertTrue(parser().parse(text).isSuccess());
    }
}