package de.kleinert.parsewisp.viz;

import de.kleinert.parsewisp.grammar.Grammar;
import de.kleinert.parsewisp.grammar.GrammarPrinter;
import de.kleinert.parsewisp.parsing.Rule;
import de.kleinert.parsewisp.result.*;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Provides a utility for creating pictures of parse trees. If the trees become too large, the generation might fail.
 * <pre>
 * {@code
 *   var text = "...";
 *   var p = Parsewisp.parser("...");
 *   var rule = Parsewisp.parse(p, text).castToParseSuccess();
 *   println(Viztool.dumpParseTree("vizoutput", rule));
 * }
 * </pre>
 * The tool <b>dot</b> must be installed.
 *
 * @since 0.9.7
 */
public final class Viztool {
    private Viztool() {
    }

    private static int dumpParseTreeHelp(final @NotNull PrintStream printer,
                                         final @NotNull List<Node> parseRes,
                                         final @NotNull AtomicInteger count) {
        final int currentId = count.getAndIncrement();
        final @NotNull String label = getLabel(parseRes.get(0));

        printer.print(currentId + "[shape=box, label=\"" + label + "\"];");
        parseRes.stream().skip(1)
                .mapToInt(child -> dumpParseTreeHelp(
                        printer,
                        child instanceof Node.NodeParseTree ? ((Node.NodeParseTree) child).content() : List.of(child),
                        count))
                .forEach(childId -> printer.append(String.valueOf(currentId))
                        .append(" -> ")
                        .append(String.valueOf(childId))
                        .append(";"));
        return currentId;
    }

    private static @NotNull String getLabel(final @NotNull Node node) {
        final @NotNull String label;
        if (node instanceof Node.NodeString) {
            label = ((Node.NodeString) node).content();
        } else if (node instanceof Node.NodeTreeTag) {
            label = ((Node.NodeTreeTag) node).content().name();
        } else if (node instanceof Node.NodeFail) {
            throw new IllegalStateException("Cannot create parse-tree visualization for " + node + " (TODO).");
        } else if (node instanceof Node.NodeParseTree) {
            throw new IllegalStateException("This case should be handled in dumpParseTreeHelp.");
        } else {
            throw new IllegalStateException(node.getClass().getName());
        }
        return label.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    /**
     * Creates a picture.
     * Example:
     * <pre>
     * {@code
     *   var text = "abc";
     *   var p = Parsewisp.parser("S : A 'bc'\nA : 'a'");
     *   var rule = Parsewisp.parse(p, text).castToParseSuccess(); // Parse tree [:S, [:A, 'a'], 'bc']
     *   println(Viztool.dumpParseTree("vizoutput", rule));
     * }
     * </pre>
     *
     * @param dotFileNamePrefix Filename without file format.
     * @param parseRes          The parse tree.
     * @return The return code. That is 0 on success or another number on failure.
     * @throws IOException          If the file can't be created or written to.
     * @throws InterruptedException If the operation is interrupted somehow.
     * @since 0.9.7
     */
    public static int dumpParseTree(
            final @NotNull String dotFileNamePrefix,
            final @NotNull ParseTree parseRes) throws IOException, InterruptedException {
        final @NotNull var dotFileName = dotFileNamePrefix + ".dot";
        final @NotNull var pngFileName = dotFileNamePrefix + ".png";
        final @NotNull var args = new String[]{"dot", "-Tpng", dotFileName, "-o", pngFileName};

        try (final @NotNull var printer = new PrintStream(dotFileName)) {
            printer.print("digraph G {");
            dumpParseTreeHelp(printer, parseRes, new AtomicInteger());
            printer.print("}");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        return Runtime.getRuntime().exec(args).waitFor();
    }
}
