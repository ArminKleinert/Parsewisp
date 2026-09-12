package de.kleinert.parsewisp.tests.typical.grammars;

import de.kleinert.parsewisp.Parsewisp;
import de.kleinert.parsewisp.Sym;
import de.kleinert.parsewisp.parser.Parser;
import de.kleinert.parsewisp.parser_options.ParserCreationOptions;
import de.kleinert.parsewisp.result.ParseResult;
import de.kleinert.parsewisp.util.Transform;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Test(s) for the Poetic (esolang) grammar.
 * <p>
 * Example from <a href="https://esolangs.org/wiki/Poetic_(esolang)#Cat_program">esolangs.org/wiki/Poetic_(esolang)</a>.
 * Grammar self-written based on
 */
class TestGrammarPoetic {
    private @NotNull Parser parser() {
        try {
            return Parsewisp.parser(
                    Files.readString(Path.of("testres/grammars/poetic.g")),
                    ParserCreationOptions.getDefault()
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String transform(ParseResult pt) {
        Map<Sym, Function<List<Object>, Object>> m = new HashMap<>();
        m.put(Sym.sym("S"), (l) -> l);
        m.put(Sym.sym("i1"), (l) -> '['); // 1 corresponds to BF's '[' instruction
        m.put(Sym.sym("i2"), (l) -> ']'); // 2 corresponds to BF's ']' instruction
        m.put(Sym.sym("i3"), (l) -> '+'); // 3 corresponds to BF's '+' instruction
        m.put(Sym.sym("i4"), (l) -> '-'); // 4 corresponds to BF's '-' instruction
        m.put(Sym.sym("i5"), (l) -> '>'); // 5 corresponds to BF's '>' instruction
        m.put(Sym.sym("i6"), (l) -> '<'); // 6 corresponds to BF's '<' instruction
        m.put(Sym.sym("i7"), (l) -> '.'); // 7 corresponds to BF's '.' instruction
        m.put(Sym.sym("i8"), (l) -> ','); // 8 corresponds to BF's ',' instruction

        // 9 fills the current cell with a random number in 0..255
        m.put(Sym.sym("i9"), (l) -> "[-]" + "+".repeat(new Random().nextInt(0, 255)));

        // 10 terminates the program under certain conditions. I will ignore this.
        m.put(Sym.sym("i10"), (l) -> '_');

        // Anything over 10 writes the number into the current cell. This is implemented as setting the cell to 0 and then mindlessly incrementing.
        m.put(Sym.sym("imore"), (l) -> "[-]" + "+".repeat(l.size()));

        return ((List<?>) Transform.transform(pt, m))
                .stream()
                .map(String::valueOf)
                .collect(Collectors.joining(""));
    }

    void bfToP() {
        String s = ",+[-.,+]";
        Map<Character, Integer> transformer = Map.of(
                '[', 1,
                ']', 2,
                '+', 3,
                '-', 4,
                '>', 5,
                '<', 6,
                '.', 7,
                ',', 8
        );
        //System.out.println(s.chars().map(c -> transformer.getOrDefault((char) c, 0)).mapToObj((i) -> "a".repeat(i) + " ").collect(Collectors.joining("")));
    }

    void bfToN() {
        String s = ",+[-.,+]";
        Map<Character, Integer> transformer = Map.of(
                '[', 1,
                ']', 2,
                '+', 3,
                '-', 4,
                '>', 5,
                '<', 6,
                '.', 7,
                ',', 8
        );
        //System.out.println(s.chars().mapToObj(c -> transformer.getOrDefault((char) c, 0).toString()).collect(Collectors.joining("\n")));
    }

    @Test
    void cat() {
        var p = parser();
        var code = " ALMIGHTY\n" + // 8
                " GOD\n" + // 3
                " I\n" + // 1
                " HAVE\n" + // 4
                " ＳⲒＮＮⴹᎠ\n" + // Filler
                " AGAINST\n" + // 7
                " ΥΟꓴ,\n" + // Filler
                " ΑＧΑⲒＮＳꓔ\n" + // Filler
                " Мꓬ\n" + // Filler
                " NEIGHBOR\n" + // 8
                " Ⅰ\n" + // Filler
                " SINＮⴹᎠ\n" + // 3 + 3 Filler
                " \uD800\uDE87ꓳꓣＧⅠＶⴹ\n" + // Filler
                " ME"; // 2
//        System.out.println(p.parse(code));
//        System.out.println(transform(p.parse(code)));
    }

    @Test
    void asciiLoop() {
        var p = parser();
        var code = " I love is a great mystery\n" +
                "but i couldn't really explain it";
//        System.out.println(p.parse(code));
//        System.out.println(transform(p.parse(code)));
    }
}