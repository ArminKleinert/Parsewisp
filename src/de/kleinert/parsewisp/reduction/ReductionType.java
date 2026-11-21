package de.kleinert.parsewisp.reduction;

import de.kleinert.parsewisp.Sym;
import de.kleinert.parsewisp.parsing.Rule;
import de.kleinert.parsewisp.result.ParseTree;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Objects;

/**
 * A class for output formats. This entire class might be removed in the future and replaced by an alternative {@link ParseTree} variant specifically for intermediate operations.
 * <p>
 * Unlike the output formats of Instaparse (hiccup and enlive), this class has no relevance for the way parse trees can be handled.
 *
 * <pre>
 * {@code
 *     var r = StringTerm.createDirect("a", false);
 *
 *     var g = new LinkedHashMap<Sym, Rule>(2);
 *     g.put(Sym.sym("S"), NonTerminal.create(Sym.sym("A")));
 *
 *     /// Default: Don't merge up
 *     /// This gives the normal output: `[:S, [:A, "a"]]`
 *     g.put(Sym.sym("A"), r.withReduction(ReductionType.initial()));
 *     var pDefault = Parsewisp.parser(new Grammar(Sym.sym("S"), g));
 *     System.out.println(pDefault.parse("a")); //>> [:S, [:A, "a"]]
 *
 *     /// Merge upwards
 *     /// This hides the `:A` tag in the tree: `[:S, "a"]`
 *     g.put(Sym.sym("A"), r.withReduction(ReductionType.mergeReduction()));
 *     var pMerge = Parsewisp.parser(new Grammar(Sym.sym("S"), g));
 *     System.out.println(pMerge.parse("a")); //>> [:S, "a"]
 *
 *     /// Add to parse tree with potentially different name (Top-level)
 *     /// This can be used to replace the tag with any symbol.
 *     /// In this example, we replace the `:A` tag with `:B`: `[:S, [:B, "a"]]`
 *     /// It could be used to emulate some playing around with precedence
 *     ///  rules like some other parser libraries provide.
 *     g.put(Sym.sym("A"), r.withReduction(ReductionType.taggedTreeReduction(Sym.sym("B"))));
 *     var pRename = Parsewisp.parser(new Grammar(Sym.sym("S"), g));
 *     System.out.println(pRename.parse("a")); //>> [:S, [:B, "a"]]
 * }
 * </pre>
 *
 * @since 0.9.7
 */
public final class ReductionType {
    private static ReductionType mergeReduction = null;
    private static ReductionType initialReduction = null;

    /**
     * The default output format for results that are to be merged into a parent tree. It has the following properties:
     * <ul>
     *     <li>The NULL_TAG (see {@link ParseTree#NULL_TAG}) as its tag.</li>
     *     <li>The type is {@link Type#MERGE}.</li>
     *     <li>{@link ReductionType#isHiddenOrRaw} returns true.</li>
     * </ul>
     *
     * <pre>
     * {@code
     *     var g = new LinkedHashMap<Sym, Rule>(2);
     *     g.put(Sym.sym("S"), NonTerminal.create(Sym.sym("A")));
     *     g.put(Sym.sym("A"), StringTerm.createDirect("a", false).withReduction(ReductionType.merging()));
     *     var pMerge = Parsewisp.parser(new Grammar(Sym.sym("S"), g));
     *     System.out.println(pMerge.parse("a")); //>> [:S, "a"]
     * }
     * </pre>
     *
     * @return Default output format for intermediate operations.
     * @since 0.9.7
     */
    public static @NotNull ReductionType merging() {
        if (mergeReduction == null)
            mergeReduction =
                    new ReductionType(ParseTree.NULL_TAG.content(), Type.MERGE, true);
        return mergeReduction;
    }

    /**
     * Default output format for new instances of {@link Rule}. It has the following properties:
     * <ul>
     *     <li>The NULL_TAG (see {@link ParseTree#NULL_TAG}) as its tag.</li>
     *     <li>The type is {@link Type#INITIAL}.</li>
     *     <li>{@link ReductionType#isHiddenOrRaw} returns true.</li>
     * </ul>
     *
     * <pre>
     * {@code
     *     var g = new LinkedHashMap<Sym, Rule>(2);
     *     g.put(Sym.sym("S"), NonTerminal.create(Sym.sym("A")));
     *     g.put(Sym.sym("A"), StringTerm.createDirect("a", false).withReduction(ReductionType.initial()));
     *     var pDefault = Parsewisp.parser(new Grammar(Sym.sym("S"), g));
     *     System.out.println(pDefault.parse("a")); //>> [:S, [:A, "a"]]
     * }
     * </pre>
     *
     * @return Default output format for new instances of {@link Rule}.
     * @since 0.9.7
     */
    public static @NotNull ReductionType initial() {
        if (initialReduction == null)
            initialReduction =
                    new ReductionType(ParseTree.NULL_TAG.content(), Type.INITIAL, true);
        return initialReduction;
    }

    /**
     * Default output format for final operations whose result is not hidden in the parse tree.
     *
     * <pre>
     * {@code
     *     var g = new LinkedHashMap<Sym, Rule>(2);
     *     g.put(Sym.sym("S"), NonTerminal.create(Sym.sym("A")));
     *     g.put(Sym.sym("A"), StringTerm.createDirect("a", false)
     *             .withReduction(ReductionType.taggedTree(Sym.sym("A"))));
     *     var pTagged = Parsewisp.parser(new Grammar(Sym.sym("S"), g));
     *     System.out.println(pTagged.parse("a")); //>> [:S, [:A, "a"]]
     * }
     * </pre>
     *
     * @param key The production's name.
     * @return A new output descriptor.
     * @since 0.9.7
     */
    public static @NotNull ReductionType taggedTree(final @NotNull Sym key) {
        return new ReductionType(key, Type.TAGGED_PARSE_TREE, false);
    }

    /**
     * The available types. They mark whether the parse operation is an intermediate operation or for the final output.
     *
     * @since 0.9.7
     */
    public enum Type {
        /**
         * The default type. This should not be used directly.
         *
         * @since 0.9.7
         */
        INITIAL,
        /**
         * Has the same properties as {@link Type#INITIAL}, but will not be replaced with {@link Type#TAGGED_PARSE_TREE} when finalizing the grammar.
         *
         * @since 0.9.7
         */
        MERGE,
        /**
         * Default type. Represent parse trees as lists.
         *
         * @since 0.9.7
         */
        TAGGED_PARSE_TREE
    }

    private final @NotNull Sym key;
    private final @NotNull ReductionType.Type type;
    private final boolean hiddenOrRaw;

    private ReductionType(final @NotNull Sym key, final @NotNull ReductionType.Type type, final boolean hiddenOrRaw) {
        this.key = key;
        this.type = type;
        this.hiddenOrRaw = hiddenOrRaw;
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
     * @since 0.9.7
     */
    public @NotNull Sym getKey() {
        return key;
    }

    /**
     * The {@link Type} type of this reduction.
     *
     * @return The {@link Type} type of this reduction.
     * @since 0.9.7
     */
    public @NotNull ReductionType.Type getReductionType() {
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
     * @since 0.9.7
     */
    public boolean isHiddenOrRaw() {
        return hiddenOrRaw;
    }

    /**
     * Replaces all rules that have the reduction type {@link Type#INITIAL} (the default when created) with rules with the reduction type {@link Type#TAGGED_PARSE_TREE}.
     *
     * @param productions The productions as a mapping of symbols to rules.
     * @since 0.9.7
     */
    public static void applyStandardReductionToProductions(
            final @NotNull LinkedHashMap<@NotNull Sym, @NotNull Rule> productions) {
        for (final @NotNull var prod : productions.entrySet()) {
            final @NotNull var key = prod.getKey();
            @NotNull var value = prod.getValue();
            if (value.getReduction().getReductionType() == Type.INITIAL) {
                value = value.withReduction(ReductionType.taggedTree(key));
            }
            prod.setValue(value);
        }
    }
}
