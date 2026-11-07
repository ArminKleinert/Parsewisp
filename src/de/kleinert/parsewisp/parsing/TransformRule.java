package de.kleinert.parsewisp.parsing;

import de.kleinert.parsewisp.collections.FlatResultSeq;
import de.kleinert.parsewisp.reduction.ReductionType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Stream;

/**
 * Represents a "once or more" parse. That is the {@code p+} operator (where p is an instance of {@link Rule}).
 *
 * @since 0.9.7
 */
public final class TransformRule extends RuleWithChild {
    private final @NotNull BiFunction<List<Object>, Integer, Object> transformer;

    private TransformRule(final boolean hide,
                          final @NotNull ReductionType red,
                          final @NotNull Rule rule,
                          final @NotNull BiFunction<List<Object>, Integer, Object> transformer) {
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
    public static @NotNull Rule create(final @NotNull Rule rule, @NotNull BiFunction<List<Object>, Integer, Object> transformer) {
        return new TransformRule(defaultHidden, defaultReductionType, rule, transformer);
    }

    @Override
    public void parse(final int index, final @NotNull Gll runner) {
        final @NotNull Rule rule = getRule();
        runner.pushListener(
                makeKey(index, rule),
                result -> {
                    var wrapped = result.getResult();

                    if (wrapped instanceof FlatResultSeq)
                        wrapped = ((FlatResultSeq) wrapped).stream();
                    else
                        wrapped = Stream.ofNullable(wrapped);

                    //noinspection unchecked
                    runner.pushSuccessMessage(
                            makeKey(index, this),
                            transformer.apply((List<Object>) ((Stream<?>) wrapped).toList(), result.index()),
                            result.index()
                    );
                }
        );
    }

    @Override
    public void fullParse(final int index, final @NotNull Gll runner) {
        final @NotNull Rule rule = getRule();
        runner.pushFullListener(
                makeKey(index, rule),
                result -> {
                    var wrapped = result.getResult();

                    if (wrapped instanceof FlatResultSeq)
                        wrapped = ((FlatResultSeq) wrapped).stream();
                    else
                        wrapped = Stream.ofNullable(wrapped);

                    //noinspection unchecked
                    runner.pushSuccessMessage(
                            makeKey(index, this),
                            transformer.apply((List<Object>) ((Stream<?>) wrapped).toList(), result.index()),
                            result.index()
                    );
                }
        );
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
}
