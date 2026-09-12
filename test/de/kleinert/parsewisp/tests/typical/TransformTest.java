package de.kleinert.parsewisp.tests.typical;

import de.kleinert.parsewisp.Parsewisp;
import de.kleinert.parsewisp.Sym;
import de.kleinert.parsewisp.testutil.PT;
import de.kleinert.parsewisp.util.Transform;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

class TransformTest {
    @Test
    void testAdd1() {
        var tree = PT.create("S", "1", "2", "3");
        Function<List<Object>, Object> transformFn =
                o -> o.stream().mapToInt(it -> Integer.parseInt((String) it)).sum();
        Map<Sym, Function<List<Object>, Object>> transformMap = Map.of(Sym.sym("S"), transformFn);

        Assertions.assertEquals(
                6,
                Transform.transform(tree, transformMap));
        Assertions.assertEquals(
                Integer.valueOf(6),
                Transform.transform(tree, transformMap, (o) -> (Integer) o));
        Assertions.assertEquals(
                6,
                Transform.transform(tree, transformMap, (o) -> (Integer) o).intValue());
    }

    @Test
    void testAdd2() {
        var p = Parsewisp.parser("""
                S     = NUM (<'+'> NUM)*
                <NUM> = #'\\d+'
                """);
        var tree = p.parse("1+2+3").castToParseSuccess();
        Function<List<Object>, Object> transformFn =
                o -> o.stream().mapToInt(it -> Integer.parseInt((String) it)).sum();
        Map<Sym, Function<List<Object>, Object>> transformMap = Map.of(Sym.sym("S"), transformFn);

        Assertions.assertEquals(
                6,
                Transform.transform(tree, transformMap));
        Assertions.assertEquals(
                Integer.valueOf(6),
                Transform.transform(tree, transformMap, (o) -> (Integer) o));
        Assertions.assertEquals(
                6,
                Transform.transform(tree, transformMap, (o) -> (Integer) o).intValue());
    }

    @Test
    void testAdd3() {
        var p = Parsewisp.parser("""
                S   = NUM ('+' NUM)*
                NUM = #'\\d+'
                """);
        var tree = p.parse("1+2+3").castToParseSuccess();
        Map<Sym, Function<List<Object>, Object>> transformMap = Map.of(
                Sym.sym("S"), o -> o.stream()
                        .filter(it -> !it.equals("+"))
                        .map(it -> (String) it)
                        .mapToInt(Integer::parseInt)
                        .sum(),
                Sym.sym("NUM"), o -> o.get(0)
        );

        Assertions.assertEquals(
                6,
                Transform.transform(tree, transformMap));
        Assertions.assertEquals(
                Integer.valueOf(6),
                Transform.transform(tree, transformMap, (o) -> (Integer) o));
        Assertions.assertEquals(
                6,
                Transform.transform(tree, transformMap, (o) -> (Integer) o).intValue());
    }

    @Test
    void testAdd4() {
        var p = Parsewisp.parser("""
                S   = A
                A   = NUM ('+' NUM)*
                NUM = #'\\d+'
                """);
        var tree = p.parse("1+2+3").castToParseSuccess();
        Map<Sym, Function<List<Object>, Object>> transformMap = Map.of(
                Sym.sym("A"), o -> String.valueOf(o
                        .stream()
                        .filter(it -> !it.equals("+"))
                        .map(it -> (String) it)
                        .mapToInt(Integer::parseInt)
                        .sum()),
                Sym.sym("NUM"), o -> o.get(0)
        );

        Assertions.assertEquals(
                List.of(Sym.sym("S"), "6"),
                Transform.transform(tree, transformMap));
        Assertions.assertEquals(
                List.of(Sym.sym("S"), "6"),
                Transform.transform(tree, transformMap, (o) -> (List<?>) o));
    }
}