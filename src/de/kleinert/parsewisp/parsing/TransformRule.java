package de.kleinert.parsewisp.parsing;

import de.kleinert.parsewisp.collections.FlatResultSeq;
import de.kleinert.parsewisp.functions.Listener;
import de.kleinert.parsewisp.reduction.ReductionType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Stream;

/**
 * This class provides limited utility for interacting with the host environment and can modify the parse tree
 * <pre>
 * {@code
 *         var g = new Grammar(Sym.sym("S"), new LinkedHashMap<>(Map.of(
 *                 Sym.sym("S"), ConcatRule.create(List.of(
 *                         TransformRule.create(
 *                                 StringTerm.create("abc", false),
 *                                 (r, i) -> List.of("a", "b", "c")
 *                         ))))));
 *         var p = Parsewisp.parser(g);
 *         System.out.println(p.parse("abc")); // [:S, "a", "b", "c"]
 * }
 * </pre>
 *
 * @since 0.9.7
 */
public final class TransformRule extends RuleWithChild {
    private final @NotNull BiFunction<List<Object>, Integer, List<Object>> transformer;

    private TransformRule(final boolean hide,
                          final @NotNull ReductionType red,
                          final @NotNull Rule rule,
                          final @NotNull BiFunction<List<Object>, Integer, List<Object>> transformer) {
        super(hide, red, rule);
        this.transformer = transformer;
    }

    /**
     * Create a new instance. Depending on the implementation, allows for buffering or create a different type of rule.
     *
     * @param rule        The {@link Rule} to match repeatedly.
     * @param transformer The transformer function.
     * @return A rule.
     * @since 0.9.7
     */
    public static @NotNull Rule create(final @NotNull Rule rule, @NotNull BiFunction<List<Object>, Integer, List<Object>> transformer) {
        return new TransformRule(defaultHidden, defaultReductionType, rule, transformer);
    }

    private static @NotNull FlatResultSeq frs(final List<Object> init) {
        var frs = FlatResultSeq.make();
        for (Object o : init) {
            frs = frs.appendOrConcat(o);
        }
        return frs;
    }

    @Override
    public void parse(final int index, final @NotNull Gll runner) {
        runner.pushListener(makeKey(index, getRule()), commonListener(runner, index));
    }

    @Override
    public void fullParse(final int index, final @NotNull Gll runner) {
        runner.pushFullListener(makeKey(index, getRule()), commonListener(runner, index));
    }

    private Listener commonListener(Gll runner, int index) {
        return result -> {
            var wrapped = result.getResult();
            wrapped = (wrapped instanceof FlatResultSeq)
                    ? ((FlatResultSeq) wrapped).stream()
                    : Stream.ofNullable(wrapped);

            //noinspection unchecked
            runner.pushSuccessMessage(
                    makeKey(index, this),
                    frs(transformer.apply((List<Object>) ((Stream<?>) wrapped).toList(), result.index())),
                    result.index()
            );
        };
    }

    @Override
    public @NotNull TransformRule withRule(final @NotNull Rule rule) {
        return new TransformRule(hide, red, rule, transformer);
    }

    @Override
    public @NotNull TransformRule withHideTag(boolean hide) {
        return isHiddenInResult() == hide ? this : new TransformRule(hide, red, rule, transformer);
    }

    @Override
    public @NotNull TransformRule withReduction(@NotNull ReductionType red) {
        return getReduction() == red ? this : new TransformRule(hide, red, rule, transformer);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        TransformRule that = (TransformRule) o;
        return transformer.equals(that.transformer);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + transformer.hashCode();
        return result;
    }
}
