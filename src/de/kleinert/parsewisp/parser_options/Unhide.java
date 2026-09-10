package de.kleinert.parsewisp.parser_options;

import de.kleinert.parsewisp.Sym;
import de.kleinert.parsewisp.grammar.Grammar;
import de.kleinert.parsewisp.parsing.Rule;
import de.kleinert.parsewisp.reduction.ReductionType;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;

/**
 * Options for unhiding parts of the output from a parse. A thorough description can be found in the description of the {@link ParsingOptions} class.
 * <p>
 * As an example, take the grammar
 * <pre>
 * {@code
 *      S   = 'a' <B> C <D> 'a'
 *      B   = 'b'+
 *      <C> = 'c'
 *      <D> = 'd'
 * }
 * </pre>
 * Now, parsing the text {@code "abcda"}, the expected tree would be {@code [:S, "a", "c", "a"]}. Where the {@code B} subtree is completely hidden, {@code C} is flattened (merged into {@code S}) and {@code D} is also completely hidden.
 * <pre>
 * {@code
 *      var p = Parsewisp.parser("S = 'a' <B> C <D> 'a'\nB = 'b'+\n<C> = 'c'\n<D> = 'd'");
 *
 *      Parsewisp.parse(p, "abcda", ParsingOptions.getDefault().withUnhide(UnhideOptions.NONE));    // [:S, "a", "c", "a"]
 *      Parsewisp.parse(p, "abcda", ParsingOptions.getDefault().withUnhide(UnhideOptions.TAGS));    // [:S, "a", [:C, "c"], "a"]
 *      Parsewisp.parse(p, "abcda", ParsingOptions.getDefault().withUnhide(UnhideOptions.CONTENT)); // [:S, "a", [:B, "b"], "c", "d", "a"]
 *      Parsewisp.parse(p, "abcda", ParsingOptions.getDefault().withUnhide(UnhideOptions.ALL));     // [:S, "a", [:B, "b"], [:C, "c"], [:D, "d"], "a"]
 * }
 * </pre>
 *
 * @see ParsingOptions#unhide()
 */
public class Unhide {
    private Unhide() {
    }

    /**
     * Applies {@link Rule#unhideContent} to all entries in the grammar.
     *
     * @param grammar The grammar.
     * @return The new grammar.
     * @see ParsingOptions#unhide()
     */
    public static @NotNull Grammar unhideContent(final @NotNull Grammar grammar) {
        final @NotNull LinkedHashMap<Sym, Rule> res = new LinkedHashMap<>();
        for (final @NotNull var symRuleEntry : grammar.entrySet()) {
            res.put(symRuleEntry.getKey(),
                    symRuleEntry.getValue().unhideContent());
        }
        return new Grammar(grammar.getStartSym(), res);
    }

    /**
     * Applies the reduction-type to all entries in the grammar.
     *
     * @param grammar The grammar.
     * @return The new grammar.
     */
    public static @NotNull Grammar unhideTags(final @NotNull Grammar grammar) {
        final @NotNull LinkedHashMap<Sym, Rule> res = new LinkedHashMap<>();
        for (final @NotNull var symRuleEntry : grammar.entrySet()) {
            final @NotNull var key = symRuleEntry.getKey();
            final @NotNull var reduction = ReductionType.nonTerminalReduction(key);
            final @NotNull var pUnhide = symRuleEntry.getValue().withReduction(reduction);
            res.put(key, pUnhide);
        }
        return new Grammar(grammar.getStartSym(), res);
    }

    /**
     * Applies the reduction-type to all entries in the grammar
     * and applies {@link Rule#unhideContent()}.
     *
     * @param grammar The grammar.
     * @return The new grammar.
     */
    public static @NotNull Grammar unhideAll(final @NotNull Grammar grammar) {
        final @NotNull LinkedHashMap<Sym, Rule> res = new LinkedHashMap<>();
        for (final @NotNull var symRuleEntry : grammar.entrySet()) {
            final @NotNull var key = symRuleEntry.getKey();
            final @NotNull var reduction = ReductionType.nonTerminalReduction(key);
            final @NotNull var p = symRuleEntry.getValue().unhideContent().withReduction(reduction);
            res.put(key, p);
        }
        return new Grammar(grammar.getStartSym(), res);
    }

    /**
     * See {@link Unhide} for documentation and an example.
     */
    public enum UnhideOptions {
        /**
         * Do nothing.
         *
         * @see ParsingOptions#unhide()
         */
        NONE,
        /**
         * Unhide tags, do not show hidden contents.
         *
         * @see Unhide#unhideTags(Grammar)
         * @see ParsingOptions#unhide()
         */
        TAGS,
        /**
         * Unhide contents, but keep tags hidden.
         *
         * @see Unhide#unhideContent(Grammar)
         * @see ParsingOptions#unhide()
         */
        CONTENT,
        /**
         * Show both contents and tags.
         *
         * @see Unhide#unhideAll(Grammar)
         * @see ParsingOptions#unhide()
         */
        ALL
    }
}
