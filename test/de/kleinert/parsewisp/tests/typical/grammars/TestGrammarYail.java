package de.kleinert.parsewisp.tests.typical.grammars;

import de.kleinert.parsewisp.Parsewisp;
import de.kleinert.parsewisp.parser.Parser;
import de.kleinert.parsewisp.parser_options.ParserCreationOptions;
import de.kleinert.parsewisp.parser_options.ParsingOptions;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

class TestGrammarYail {
    private @NotNull Parser parser() {
        try {
            var opts = ParserCreationOptions.getDefault();
            return Parsewisp.parser(
                    Files.readString(Path.of("testres/grammars/yail.g")),
                    opts
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void verifySimpleProgram() {
        var text = "print 1 ;";
        Assertions.assertTrue(parser().parse(text).isSuccess());
    }

    @Test
    void verifyVar() {
        var text = "var v = 0;";
        Assertions.assertTrue(parser().parse(text).isSuccess());
    }

    @Test
    void verifyAssign() {
        var text = "v = 0;";
        Assertions.assertTrue(parser().parse(text).isSuccess());
    }

    @Test
    void verifyFunctionDef() {
        var text = "//: (int32_t, int32_t) -> int64_t\nfun f(a, b) { return host(\"((int64_t)a) + b\"); }";
        //System.out.println(rule().parse(text));
        Assertions.assertTrue(parser().parse(text, ParsingOptions.getDefault()).isSuccess());
    }

    @Test
    void verifySimpleStuff() {
        var text = """
                var v = 4;
                fun f(v) { if (v > 0) return -v; else return v; }
                print f(v) <= 0;""";
        Assertions.assertTrue(parser().parse(text).isSuccess());
    }
}
