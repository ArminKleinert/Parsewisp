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
public final class Reduction {
    /**
     * The default output format for results that are to be merged into a parent tree. It has the following properties:
     * <ul>
     *     <li>The NULL_TAG (see {@link ParseTree#NULL_TAG}) as its tag.</li>
     *     <li>The type is {@link RedType#MERGE}.</li>
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
    public static @NotNull Reduction merging() {
        return new Reduction(ParseTree.NULL_TAG.content(), RedType.MERGE);
    }

    /**
     * Default output format for new instances of {@link Rule}. It has the following properties:
     * <ul>
     *     <li>The NULL_TAG (see {@link ParseTree#NULL_TAG}) as its tag.</li>
     *     <li>The type is {@link RedType#INITIAL}.</li>
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
    public static @NotNull Reduction initial() {
        return new Reduction(ParseTree.NULL_TAG.content(), RedType.INITIAL);
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
    public static @NotNull Reduction taggedTree(final @NotNull Sym key) {
        return new Reduction(key, RedType.TAGGED_PARSE_TREE);
    }

    /**
     * The available types. They mark whether the parse operation is an intermediate operation or for the final output.
     *
     * @since 0.9.7
     */
    public enum RedType {
        /**
         * The default type. This should not be used directly.
         *
         * @since 0.9.7
         */
        INITIAL,
        /**
         * Has the same properties as {@link RedType#INITIAL}, but will not be replaced with {@link RedType#TAGGED_PARSE_TREE} when finalizing the grammar.
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
    private final @NotNull Reduction.RedType type;

    private Reduction(final @NotNull Sym key, final @NotNull Reduction.RedType type) {
        this.key = key;
        this.type = type;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        final @NotNull Reduction that = (Reduction) o;
        return Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, type);
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
     * The {@link RedType} type of this reduction.
     *
     * @return The {@link RedType} type of this reduction.
     * @since 0.9.7
     */
    public @NotNull Reduction.RedType getType() {
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
     * Replaces all rules that have the reduction type {@link RedType#INITIAL} (the default when created) with rules with the reduction type {@link RedType#TAGGED_PARSE_TREE}.
     *
     * @param productions The productions as a mapping of symbols to rules.
     * @since 0.9.7
     */
    public static void applyStandardReductionToProductions(
            final @NotNull LinkedHashMap<@NotNull Sym, @NotNull Rule> productions) {
        for (final @NotNull var prod : productions.entrySet()) {
            final @NotNull var key = prod.getKey();
            @NotNull var value = prod.getValue();
            if (value.getReduction().getType() == RedType.INITIAL) {
                value = value.withReduction(Reduction.taggedTree(key));
            }
            prod.setValue(value);
        }
    }
}
