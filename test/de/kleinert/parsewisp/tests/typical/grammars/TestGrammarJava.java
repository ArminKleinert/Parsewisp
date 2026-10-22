package de.kleinert.parsewisp.tests.typical.grammars;

import de.kleinert.parsewisp.Parsewisp;
import de.kleinert.parsewisp.parser.Parser;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

/**
 * Test(s) for the Java grammar.
 * <p>
 * Grammar from <a href="https://docs.oracle.com/javase/specs/jls/se21/html/jls-19.html">the Java 21 language specification, section 19</a>.
 */
class TestGrammarJava {
    private @NotNull Parser parser() {
        try {
            return Parsewisp.parser(
                    Files.readString(Path.of("testres/grammars/java21.grammar"))
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void helloWorld() {
        var text = "public class Main { public static void main(String[] args) { System.out.println(\"Hello World!\"); } }";
        Assertions.assertTrue(parser().parse(text).isSuccess());
    }

    @Test
    void basicTestClass() {
        var text = """
                package de.kleinert.parsewisp.parsing;
                
                import de.kleinert.parsewisp.reduction.ReductionType;
                import org.jetbrains.annotations.NotNull;
                
                // Comment
                public abstract sealed class Terminal
                        extends SimpleRule
                        permits EOFTerm, EpsilonTerm, RegexTerm, StringTerm, ValueRangeTerm {
                    /**
                     * Constructor.
                     *
                     * @param hide The hide option.
                     * @param red  The reduction type for this rule.
                     * @since 0.9.7
                     */
                    protected Terminal create(final boolean hide, final @NotNull ReductionType red) {
                    }
                }
                """;
        var res = parser().parse(text);
        Assertions.assertTrue(res.isSuccess());
    }

    @Test
    void biggerTestClass() {
        var text = """
                package de.kleinert.parsewisp.parsing;
                
                import static de.kleinert.parsewisp.trampoline.TrampolineListenerNode.TrampolineListenerKey;
                
                import de.kleinert.parsewisp.collections.FlatResultSeq;
                import de.kleinert.parsewisp.reduction.ReductionType;
                import org.jetbrains.annotations.NotNull;
                
                /**
                 * "Zero or more" repetition. Represents a production which repeatedly ties to match an input. E.g. {@code P*} matches zero or more.
                 * <p>
                 * Notation: {@code {rule}} or {@code rule*}
                 *
                 * @since 0.9.7
                 */
                public final class ZeroOrMoreRule extends RuleWithChild {
                    private ZeroOrMoreRule(final boolean hide,
                                           final @NotNull ReductionType red,
                                           final @NotNull Rule rule) {
                        super(hide, red, rule);
                    }
                
                    /**
                     * Create a new instance. Depending on the implementation, allows for buffering or create a different type of rule.
                     *
                     * @param rule The {@link Rule} to match repeatedly.
                     * @return A rule.
                     * @since 0.9.7
                     */
                    public static @NotNull Rule create(final @NotNull Rule rule) {
                        if (rule instanceof EpsilonTerm)
                            return rule;
                        return new ZeroOrMoreRule(defaultHidden, defaultReductionType, rule);
                    }
                
                    @Override
                    public void parse(final int index, final @NotNull Gll runner) {
                        final @NotNull Rule rule = getRule();
                        final @NotNull TrampolineListenerKey nodeKeyForStar =
                                new TrampolineListenerKey(index, this);
                        runner.pushListener(
                                new TrampolineListenerKey(index, rule),
                                OnceOrMoreRule.plusListener(FlatResultSeq.make(), rule, index, nodeKeyForStar, runner)
                        );
                        runner.pushSuccessMessageWithoutValue(nodeKeyForStar, index);
                    }
                
                    @Override
                    public void fullParse(final int index, final @NotNull Gll runner) {
                        final @NotNull Rule rule = getRule();
                        final @NotNull TrampolineListenerKey nodeKeyForStar =
                                new TrampolineListenerKey(index, this);
                        if (index == runner.tramp().getText().length()) {
                            runner.pushSuccessMessageWithoutValue(nodeKeyForStar, index);
                            return;
                        }
                        runner.pushListener(
                                new TrampolineListenerKey(index, rule),
                                OnceOrMoreRule.plusFullListener(FlatResultSeq.make(), rule, index, nodeKeyForStar, runner));
                    }
                
                    @Override
                    public @NotNull ZeroOrMoreRule withRule(final @NotNull Rule rule) {
                        return new ZeroOrMoreRule(hide, red, rule);
                    }
                
                    @Override
                    public @NotNull ZeroOrMoreRule withHideTag(boolean hide) {
                        return isHiddenInResult() == hide ? this : new ZeroOrMoreRule(hide, red, rule);
                    }
                
                    @Override
                    public @NotNull ZeroOrMoreRule withReduction(@NotNull ReductionType red) {
                        return getReduction() == red ? this : new ZeroOrMoreRule(hide, red, rule);
                    }
                }
                """;
        var res = parser().parse(text);
        Assertions.assertTrue(res.isSuccess());
    }

    @Test
    void testWhichIsAcceptablyBig() {
        var text = """
                package de.kleinert.parsewisp.tests.typical.rules_available;
                class RulesAvailableGeneralizedTests {
                """;
        text += "private static void doNothing(List opts, boolean run) {}".repeat(500);
        text += "}";
        var res = parser().parse(text);
        Assertions.assertTrue(res.isSuccess());
    }

//    @Test
//    void testWhichIsWayTooBig() {
//        var text = """
//                package de.kleinert.parsewisp.tests.typical.rules_available;
//                class RulesAvailableGeneralizedTests {
//                """;
//        text += "private static void doNothing(List opts, boolean run) {}".repeat(5000);
//        text += "}";
//        var res = parser().parse(text);
//        Assertions.assertTrue(res.isSuccess());
//    }

    @Test
    void testParseParsewispSources() throws IOException {
        BiPredicate<Path, BasicFileAttributes> matcher = (p, bfa) -> bfa.isRegularFile();
        final var files = Stream.concat(
                        Files.find(Paths.get("src"), 999, matcher),
                        Files.find(Paths.get("test"), 999, matcher))
                .filter(p -> p.toString().endsWith(".java"))
                .toList();
        var p = parser();

        for (Path file : files) {
            var s = Files.readString(file);
            var res = p.parse(s);
            Assertions.assertTrue(
                    res.isSuccess(),
                    () -> "Failed testcase.\n" + res.castToParseFailure());
        }
    }
}