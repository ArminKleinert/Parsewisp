package de.kleinert.parsewisp.util;

import de.kleinert.parsewisp.Sym;
import de.kleinert.parsewisp.result.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * This class provides methods for transforming parse trees via tag-to-function tables.
 */
public final class Transform {
    private Transform() {
    }

    private static Object transform(
            final @NotNull Node node,
            final @NotNull Map<@NotNull Sym, @NotNull Function<@NotNull List<Object>, Object>> transformMap
    ) {
        if (node instanceof Node.NodeTreeTag) return ((Node.NodeTreeTag) node).content();
        if (node instanceof Node.NodeString) return ((Node.NodeString) node).content();
        if (node instanceof Node.NodeFail) return ((Node.NodeFail) node).content();
        if (node instanceof Node.NodeParseTree) return transform(((Node.NodeParseTree) node).content(), transformMap);
        throw new IllegalArgumentException("Invalid node: " + node + " of type " + node.getClass());
    }

    private static Object transform(
            final @NotNull ParseTree parseTree,
            final @NotNull Map<Sym, Function<List<Object>, Object>> transformMap
    ) {
        var tagSym = parseTree.getTag().content();
        var transformFn = transformMap.get(tagSym);
        var transformedNodes = parseTree.getContent().stream().map(node -> transform(node, transformMap)).toList();
        if (transformFn != null) {
            return transformFn.apply(transformedNodes);
        } else {
            return ParseTree.create(parseTree.getTag().content(), transformedNodes);
        }
    }

    /**
     * Transforms a {@link ParseTree} through application of a transformation-map.
     *
     * <pre>
     * {@code
     *         var p = Alpha.parser("""
     *                 S := NUM ('+' NUM)*
     *                 NUM := #'\\d+'
     *                 """);
     *         var tree = p.parse("1+2+3").castToParseSuccess();
     *
     *         Map<Sym,Function<List<Object>, Object>> transformMap = Map.of(
     *                 Sym.sym("S"), o -> o.stream()
     *                         .filter(it -> !it.equals("+")) // If the token is "+", ignore.
     *                         .map(it -> (String) it) // Treat each as String.
     *                         .mapToInt(Integer::parseInt) // Parse each String to Int.
     *                         .sum(), // Sum.
     *                 Sym.sym("NUM"), List::getFirst
     *         );
     *         Function<Object, Integer> finalizer = (o) -> (Integer)o;
     *
     *         // The tree is [:S, [:NUM, "1"], "+", [:NUM, "2"], "+", [:NUM, "3"]].
     *         // The transformation sees the tag :S and looks up the function. It starts walking the content.
     *         //   The content is [[:NUM, "1"], "+", [:NUM, "2"], "+", [:NUM, "3"]].
     *         //   First, `.filter(it -> !it.equals("+"))` removes all occurences of "+".
     *         //   The content is now [[:NUM, "1"], [:NUM, "2"], [:NUM, "3"]].
     *         //   While walking, `List::getFirst` is applied to the content of each subtree.
     *         //   The content is now ["1", "2", "3"].
     *         //   The function applies `Integer::parseInt` to each element.
     *         //   The content is now [1, 2, 3].
     *         //   The function now sums up the numbers: 1+2+3 = 6.
     *         //   Lastly, the finalizer is applied, casting the default to an Integer.
     *         Assertions.assertEquals(
     *                 Integer.valueOf(6),
     *                 Transform.transform(tree, transformMap, finalizer));
     * }
     * </pre>
     * Simplified pseudocode for the above:
     * <pre>
     * {@code
     *         Map<Sym,Function<List<Object>, Object>> transformMap = Map.of(
     *                 Sym.sym("S"), o -> o.filter(it != "+").map(it.toInt()).sum(),
     *                 Sym.sym("NUM"), List::getFirst
     *         );
     * }
     * </pre>
     * <p>
     * If a tag is not in the Map, a parse tree is constructed.
     * In this case, all nodes in the content MUST have a corresponding {@link Node} wrapper ({@link ParseTree} or {@link String})!
     * <pre>
     * {@code
     *         var p = Alpha.parser("""
     *                 S := A
     *                 A := NUM ('+' NUM)*
     *                 NUM := #'\\d+'
     *                 """);
     *         var tree = p.parse("1+2+3").castToParseSuccess();
     *
     *         Map<Sym,Function<List<Object>, Object>> transformMap = Map.of(
     *                 Sym.sym("A"), o -> String.valueOf(o.stream()
     *                         .filter(it -> !it.equals("+")) // If the token is "+", ignore.
     *                         .map(it -> (String) it) // Treat each as String.
     *                         .mapToInt(Integer::parseInt) // Parse each String to Int.
     *                         .sum()), // Sum.
     *                 Sym.sym("NUM"), List::getFirst
     *         );
     *         Function<Object, Integer> finalizer = (o) -> (Integer)o;
     *
     *         Assertions.assertEquals(
     *                 ParseTree.create("S", "6"),
     *                 Transform.transform(tree, transformMap));
     * }
     * </pre>
     *
     * @param parseResult  The parse result (expected to be a {@link ParseTree}).
     * @param transformMap Map of tags (symbols) to functions.
     * @param finalizer    Casting from raw Object to specific output type.
     * @param <T>          The output type.
     * @return Transformed content.
     * @throws IllegalArgumentException If the parseResult is not a parse tree.
     */
    public static <T> T transform(
            final @NotNull ParseResult parseResult,
            final @NotNull Map<@NotNull Sym, @NotNull Function<@NotNull List<Object>, Object>> transformMap,
            final @NotNull Function<Object, T> finalizer
    ) {
        return finalizer.apply(transform(parseResult, transformMap));
    }

    /**
     * Same as {@link #transform(ParseResult, Map, Function)} but without a finalizer. The output is a plain object.
     *
     * @param parseResult  The parse result (expected to be a {@link ParseTree}).
     * @param transformMap Map of tags (symbols) to functions.
     * @return Transformed content.
     * @throws IllegalArgumentException If the parseResult is not a parse tree.
     */
    public static Object transform(
            final @NotNull ParseResult parseResult,
            final @NotNull Map<@NotNull Sym, @NotNull Function<@NotNull List<Object>, Object>> transformMap
    ) {
        if (!(parseResult instanceof ParseTree parseTree))
            throw new IllegalArgumentException(parseResult.toString());
        final @NotNull var tagSym = parseTree.getTag().content();
        final @NotNull var transformedNodes = parseTree
                .getContent().stream()
                .map(node -> transform(node, transformMap))
                .toList();
        final var transformFn = transformMap.get(tagSym);
        final Object result;
        if (transformFn != null) {
            result = transformFn.apply(transformedNodes);
        } else {
            result = ParseTree.create(parseTree.getTag().content(), transformedNodes);
        }
        return result;
    }
}
