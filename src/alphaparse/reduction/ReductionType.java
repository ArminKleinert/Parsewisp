package alphaparse.reduction;

import alphaparse.Sym;
import alphaparse.parsing.Rule;
import alphaparse.result.ParseTree;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Objects;

/**
 * A class for output formats. This entire class might be removed in the future and replaced by an alternative {@link ParseTree} variant specifically for intermediate operations.
 * <p>
 * Unlike Instaparse's output formats, this class has no relevance for the final output.
 * <p>
 * None of this class's operations should be used by users of the library!
 */
public final class ReductionType {
    private static ReductionType standardIntermediateReduction = null;
    private static ReductionType standardInitialReduction = null;

    /**
     * The default output format for intermediate operations. It has the following properties:
     * <ul>
     *     <li>The illegal NULL_TAG (see {@link ParseTree#NULL_TAG}) as its tag to mark it as an illegal final output.</li>
     *     <li>The type is {@link ReductionTypesAvailable#INTERMEDIATE}.</li>
     *     <li>{@link ReductionType#isHiddenOrRaw} returns true.</li>
     * </ul>
     *
     * @return Default output format for intermediate operations.
     */
    public static @NotNull ReductionType standardIntermediateReduction() {
        if (standardIntermediateReduction == null)
            standardIntermediateReduction =
                    new ReductionType(ParseTree.NULL_TAG.content(), ReductionTypesAvailable.INTERMEDIATE, true);
        return standardIntermediateReduction;
    }

    /**
     * Default output format for new instances of {@link Rule}. It has the following properties:
     * <ul>
     *     <li>The illegal NULL_TAG (see {@link ParseTree#NULL_TAG}) as its tag to mark it as an illegal final output.</li>
     *     <li>The type is {@link ReductionTypesAvailable#INITIAL}.</li>
     *     <li>{@link ReductionType#isHiddenOrRaw} returns true.</li>
     * </ul>
     *
     * @return Default output format for new instances of {@link Rule}.
     */
    public static @NotNull ReductionType standardInitialReduction() {
        if (standardInitialReduction == null)
            standardInitialReduction =
                    new ReductionType(ParseTree.NULL_TAG.content(), ReductionTypesAvailable.INITIAL, true);
        return standardInitialReduction;
    }

    /**
     * The available types. They mark whether the parse operation is an intermediate operation or for the final output.
     */
    public enum ReductionTypesAvailable {
        /**
         * The default type. This should not be used directly.
         */
        INITIAL,
        /**
         * Has the same properties as {@link ReductionTypesAvailable#INITIAL}, but will not be replaced with the default type when finalizing the grammar. This should not be used directly.
         */
        INTERMEDIATE,
        /**
         * Default type. Represent parse trees as lists. Might be removed in the future.
         */
        TAGGED_PARSE_TREE
    }

    private final @NotNull Sym key;
    private final @NotNull ReductionTypesAvailable type;
    private final boolean hiddenOrRaw;

    private ReductionType(final @NotNull Sym key, final @NotNull ReductionTypesAvailable type, final boolean hiddenOrRaw) {
        this.key = key;
        this.type = type;
        this.hiddenOrRaw = hiddenOrRaw;
    }

    /**
     * Default output format for final operations whose result is not hidden in the parse tree.
     *
     * @param key The production's name.
     * @return A new output descriptor.
     */
    public static @NotNull ReductionType defaultNonRawReduction(final @NotNull Sym key) {
        return new ReductionType(key, ReductionTypesAvailable.TAGGED_PARSE_TREE, false);
    }

    /**
     * Default output format for final operations whose key is legal for output.
     *
     * @param key The production's name.
     * @return A new output descriptor.
     */
    public static @NotNull ReductionType nonTerminalReduction(final @NotNull Sym key) {
        return defaultNonRawReduction(key);
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        final @NotNull ReductionType that = (ReductionType) o;
        return Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, type, hiddenOrRaw);
    }

    /**
     * The production key this output format is for.
     *
     * @return The production key this output format is for.
     */
    public @NotNull Sym getKey() {
        return key;
    }

    /**
     * The {@link ReductionTypesAvailable} type of this reduction.
     *
     * @return The {@link ReductionTypesAvailable} type of this reduction.
     */
    public @NotNull ReductionTypesAvailable getReductionType() {
        return type;
    }

    @Override
    public @NotNull String toString() {
        return "{" +
                "key=" + key +
                ", type=" + type +
                '}';
    }

    /**
     * If true, the intermediate result will be hidden in the final output tree.
     *
     * @return If true, the intermediate result will be hidden in the final output tree.
     */
    public boolean isHiddenOrRaw() {
        return hiddenOrRaw;
    }

    /**
     * Replaces all rules that have the reduction type {@link alphaparse.reduction.ReductionType.ReductionTypesAvailable#INITIAL} (the default when created) with rules with the reduction type {@link alphaparse.reduction.ReductionType.ReductionTypesAvailable#TAGGED_PARSE_TREE}.
     *
     * @param productions The productions as a mapping of symbols to rules.
     */
    public static void applyStandardReductionToProductions(
            final @NotNull LinkedHashMap<@NotNull Sym, @NotNull Rule> productions) {
        for (final @NotNull var prod : productions.entrySet()) {
            final @NotNull var key = prod.getKey();
            @NotNull var value = prod.getValue();
            if (value.getReduction().getReductionType() == ReductionTypesAvailable.INITIAL) {
                value = value.withReduction(ReductionType.defaultNonRawReduction(key));
            }
            prod.setValue(value);
        }
    }
}
