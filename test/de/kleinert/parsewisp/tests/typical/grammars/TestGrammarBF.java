package de.kleinert.parsewisp.tests.typical.grammars;

import de.kleinert.parsewisp.Parsewisp;
import de.kleinert.parsewisp.parser.Parser;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Test(s) for the bf (BrainFuck) grammar.
 * <p>
 * Grammar and tests from <a href="https://esolangs.org/wiki/Brainfuck#Examples">esolangs.org/wiki/Brainfuck</a>.
 */
class TestGrammarBF {
    private @NotNull Parser parser() {
        try {
            return Parsewisp.parser(
                    Files.readString(Path.of("testres/grammars/bf.g"))
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void helloWorld() {
        var text = "++++++++[>++++[>++>+++>+++>+<<<<-]>+>+>->>+[<]<-]>>.>---.+++++++..+++.>>.<-.<.+++.------.--------.>>+.>++.";
        Assertions.assertTrue(parser().parse(text).isSuccess());
    }

    @Test
    void moveValue() {
        var text = """
                Code:   Pseudo code:
                >>      Move the pointer to cell2
                [-]     Set cell2 to 0\s
                <<      Move the pointer back to cell0
                [       While cell0 is not 0
                  -       Subtract 1 from cell0
                  >>      Move the pointer to cell2
                  +       Add 1 to cell2
                  <<      Move the pointer back to cell0
                ]       End while""";
        Assertions.assertTrue(parser().parse(text).isSuccess());
    }
    @Test void cat(){
        var text = ",[.,]";
        Assertions.assertTrue(parser().parse(text).isSuccess());}

    @Test void quine() {
        var text = """
                -->+++>+>+>+>+++++>++>++>->+++>++>+>>>>>>>>>>>>>>>>->++++>>>>->+++>+++>+++>+++>+
                ++>+++>+>+>>>->->>++++>+>>>>->>++++>+>+>>->->++>++>++>++++>+>++>->++>++++>+>+>++
                >++>->->++>++>++++>+>+>>>>>->>->>++++>++>++>++++>>>>>->>>>>+++>->++++>->->->+++>
                >>+>+>+++>+>++++>>+++>->>>>>->>>++++>++>++>+>+++>->++++>>->->+++>+>+++>+>++++>>>
                +++>->++++>>->->++>++++>++>++++>>++[-[->>+[>]++[<]<]>>+[>]<--[++>++++>]+[<]<<++]
                >>>[>]++++>++++[--[+>+>++++<<[-->>--<<[->-<[--->>+<<[+>+++<[+>>++<<]]]]]]>+++[>+
                ++++++++++++++<-]>--.<<<]""";
        Assertions.assertTrue(parser().parse(text).isSuccess());}
    @Test void xkcdRandom() {
        var text = "+c+h+o+s[e-n> +b+y+ <f]a>i+r[ -d>i+c+e+ +r<o]l-l+>;\n" +
                "Guaranteed to be random.";
        Assertions.assertTrue(parser().parse(text).isSuccess());}
}