package alphaparse.viz;

import alphaparse.result.Node;
import alphaparse.result.ParseTree;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Provides a utility for creating pictures of parse trees. If the trees become too large, the generation might fail.
 * <pre>
 * {@code
 *   var text = "...";
 *   var p = Alpha.parser("...");
 *   var rule = Alpha.parse(p, text).castToParseSuccess();
 *   println(Viztool.dumpParseTree("vizoutput", rule));
 * }
 * </pre>
 * The tool <b>dot</b> must be installed.
 */
public final class Viztool {
    private Viztool() {
    }

    private static int dumpParseTreeHelp(final @NotNull PrintStream printer,
                                         final @NotNull List<Node> parseRes,
                                         final @NotNull AtomicInteger count) {
        final int currentId = count.getAndIncrement();
        final @NotNull String label = getLabel(parseRes);

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

    private static @NotNull String getLabel(@NotNull List<Node> parseRes) {
        final @NotNull Node fpr = parseRes.get(0);
        final @NotNull String label;
        if (fpr instanceof Node.NodeString) {
            label = ((Node.NodeString) fpr).content();
        } else if (fpr instanceof Node.NodeTreeTag) {
            label = ((Node.NodeTreeTag) fpr).content().name();
        } else if (fpr instanceof Node.NodeFail) {
            throw new IllegalStateException("Cannot create parse-tree visualization for " + fpr + " (TODO).");
        } else if (fpr instanceof Node.NodeParseTree) {
            throw new IllegalStateException("This case should be handled in dumpParseTreeHelp.");
        } else {
            throw new IllegalStateException(fpr.getClass().getName());
        }
        return label.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    /**
     * Creates a picture.
     * Example:
     * <pre>
     * {@code
     *   var text = "abc";
     *   var p = Alpha.parser("S : A 'bc'\nA : 'a'");
     *   var rule = Alpha.parse(p, text).castToParseSuccess(); // Parse tree [:S, [:A, 'a'], 'bc']
     *   println(Viztool.dumpParseTree("vizoutput", rule));
     * }
     * </pre>
     *
     * @param dotFileNamePrefix Filename without file format.
     * @param parseRes          The parse tree.
     * @return The return code. That is 0 on success or another number on failure.
     * @throws IOException          If the file can't be created or written to.
     * @throws InterruptedException If the operation is interrupted somehow.
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
