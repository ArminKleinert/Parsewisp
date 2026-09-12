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
 * Test(s) for the C99 grammar.
 */
class TestGrammarC99Scannerless {
    private @NotNull Parser parser() {
        try {
            return Parsewisp.parser(
                    Files.readString(Path.of("testres/grammars/c99_sl.g")),
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

    @Test
    void verifySimpleProgram1() throws IOException {
        var text = "int i;";
        //System.out.println(rule().parses(text).stream().map(ParseTree::toString).collect(Collectors.joining("\n")));
        //System.out.println(rule().parses(text).size());
    }
}

