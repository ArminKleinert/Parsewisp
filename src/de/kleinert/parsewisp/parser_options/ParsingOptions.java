package de.kleinert.parsewisp.parser_options;

import static de.kleinert.parsewisp.parser_options.Unhide.UnhideOptions;

import de.kleinert.parsewisp.*;
import de.kleinert.parsewisp.parser.Parser;
import de.kleinert.parsewisp.parsing.RegexTerm;
import de.kleinert.parsewisp.result.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Options for parsing with an already created parser.
 *
 * @param start                   Start production for the parse. Use {@code null} to use the Parser's start production.
 * @param usePartial              Whether to return partial (incomplete) parses.
 * @param unhide                  Which parts of a parse result to show when they would normally be hidden by the parser.
 * @param embedFailureInParseTree If true, a failed parse results in a {@link ParseTree}, as a success would, but a {@link ParseFailureNode} is embedded in the tree.
 * @param iterativeDeepening      Whether to iteratively deepen parsing when parsing with a regex terminal. See {@link RegexTerm#parse}.
 * @param failureIfEmpty          Important for {@link Parsewisp#parses(Parser, String, ParsingOptions)} only. If false, return an empty list if the parsing failed. If true, return the failure.
 * @see ParsingOptions#DEFAULT_START
 * @see ParsingOptions#DEFAULT_PARTIAL
 * @see ParsingOptions#DEFAULT_UNHIDE
 * @see ParsingOptions#DEFAULT_EMBED_FAILURES
 * @see ParsingOptions#DEFAULT_FAILURE_IF_EMPTY
 */
public record ParsingOptions(
        @Nullable Sym start,
        boolean usePartial,
        @NotNull UnhideOptions unhide,
        boolean embedFailureInParseTree,
        boolean iterativeDeepening,
        boolean failureIfEmpty
) {
    /**
     * Default for the start production name of a parse operation. ({@code null})
     */
    public static final @Nullable Sym DEFAULT_START = null;
    /**
     * Default for the start production name of a parse operation. ({@code false})
     */
    public static final boolean DEFAULT_PARTIAL = false;
    /**
     * By default, leave the parse trees as intended (hidden parts stay hidden). The value is {@link UnhideOptions#NONE}
     */
    public static final @NotNull UnhideOptions DEFAULT_UNHIDE = UnhideOptions.NONE;
    /**
     * By default, do not include failure nodes in parse trees. ({@code false})
     */
    public static final boolean DEFAULT_EMBED_FAILURES = false;
    /**
     * By default, do not iteratively deepen search when parsing with a regex. ({@code false})<br/>
     * The reason is that it is much slower to do.
     */
    public static final boolean DEFAULT_ITERATIVE_DEEPENING = false;
    /**
     * {@code false} by default.
     */
    public static final boolean DEFAULT_FAILURE_IF_EMPTY = false;

    /**
     * Calls {@link ParsingOptions#ParsingOptions(Sym, boolean, UnhideOptions, boolean, boolean, boolean)} with the static defaults.
     *
     * @return An instance of this class, using all the static DEFAULT_* values.
     * @see ParsingOptions#DEFAULT_START
     * @see ParsingOptions#DEFAULT_PARTIAL
     * @see ParsingOptions#DEFAULT_UNHIDE
     * @see ParsingOptions#DEFAULT_EMBED_FAILURES
     * @see ParsingOptions#DEFAULT_ITERATIVE_DEEPENING
     * @see ParsingOptions#DEFAULT_FAILURE_IF_EMPTY
     */
    public static @NotNull ParsingOptions getDefault() {
        return new ParsingOptions(DEFAULT_START, DEFAULT_PARTIAL, DEFAULT_UNHIDE, DEFAULT_EMBED_FAILURES, DEFAULT_ITERATIVE_DEEPENING, DEFAULT_FAILURE_IF_EMPTY);
    }

    /**
     * When a parser is created, it carries its own start production. With this option, the start production can be forcefully changed for a parse. If the parser's start should be used, use {@code null} as the parameter.
     * <p>
     * Example:
     * <pre>
     * {@code
     *      // The grammar has these two, unrelated productions. The first production is A. So that is the production the parser uses.
     *      //    A := 'a'
     *      //    B := 'b'
     *      var p = Alpha.parser("A := 'a'\nB := 'b'");
     *
     *      println(p.parse("a")); // Success: [:A, a]
     *      println(p.parse("b")); // Failure: 'b' could not be parsed from production A.
     *
     *      // With the start production explicitly changed, it works:
     *      println(p.parse("b", Alpha.ParsingOptions.getDefault().withStart(Keyword.intern("B")))); // [:B, b]
     * }
     * </pre>
     * This can be useful if the grammar has multiple unrelated parts.
     *
     * @param start The new start production.
     * @return A new instance.
     * @see ParsingOptions#start()
     * @see ParsingOptions#DEFAULT_START
     * @see ParsingOptions#getDefault()
     */
    public @NotNull ParsingOptions withStart(final @Nullable Sym start) {
        if (Objects.equals(this.start, start)) return this;
        return new ParsingOptions(start, usePartial, unhide, embedFailureInParseTree, iterativeDeepening, failureIfEmpty);
    }

    /**
     * When generating a parse tree, the parser tries to parse the full string first. With this option, partial parses can be made available.
     * <br>
     * The option only applies when requesting the full parse forest (e.g. {@link Parsewisp#parses(Parser, String, ParsingOptions)}), but not when only a single parse is requested (e.g. {@link Parsewisp#parse(Parser, String, ParsingOptions)}).
     * <pre>
     * {@code
     *      var p = Alpha.parser("S := 'a'+");
     *      println(p.parses("aa")); // [[:S, a, a]]
     *
     *      var opts = Alpha.ParsingOptions.getDefault().withPartialSetTo(true);
     *      println(p.parses("aa", opts)); // [[:S, a], [:S, a, a]]
     *
     *      // The option has no effect on single parse:
     *      println(p.parse("aa", opts)); // [:S, a, a]
     * }
     * </pre>
     *
     * @param usePartial The argument as a boolean.
     * @return A new instance.
     * @see #usePartial()
     */
    public @NotNull ParsingOptions withPartial(final boolean usePartial) {
        if (Objects.equals(this.usePartial, usePartial)) return this;
        return new ParsingOptions(start, usePartial, unhide, embedFailureInParseTree, iterativeDeepening, failureIfEmpty);
    }

    /**
     * Create a new instance with {@link #unhide()} set to the parameter.
     * <p>
     * Consider the following grammar:
     * <pre>
     * {@code
     *   S   = 'a' <B> C <D> 'e' (* Expects the string "abcde", but will hide the substrings parsed by productions B and D *)
     *   B   = 'b'
     *   <C> = 'c' (* Expects the string "c", but will "flatten" itself into the output. *)
     *   <D> = 'd' (* Same as C *)
     * }
     * </pre>
     * Explanation: Production S expects the string {@code "abcde"}, but the outputs of productions B and D will be hidden. The result of production C will be shown in the output without the associated tag "C".
     * <br>
     * Now the code:
     * <pre>
     * {@code
     *   var p = Alpha.parser("S = 'a' <B> C <D> 'a'\nB = 'b'+\n<C> = 'c'\n<D> = 'd'");
     *
     *   // No options.
     *   println("Default => " + p.parse("abcda"));       // [:S, a, c, a]
     *
     *   var opts = Alpha.ParsingOptions.getDefault();
     *   println("Default => " + p.parse("abcda"));       // [:S, a, c, a]
     *
     *   opts = opts.withUnhide(Alpha.UnhideOptions.none);
     *   println("none    => " + p.parse("abcda", opts)); // [:S, a, c, a]
     *
     *   opts = opts.withUnhide(Alpha.UnhideOptions.tags);
     *   println("tags    => " + p.parse("abcda", opts)); // [:S, a, [:C, c], a]
     *
     *   opts = opts.withUnhide(Alpha.UnhideOptions.content);
     *   println("content => " + p.parse("abcda", opts)); // [:S, a, [:B, b], c, d, a]
     *
     *   opts = opts.withUnhide(Alpha.UnhideOptions.all);
     *   println("all     => " + p.parse("abcda", opts)); // [:S, a, [:B, b], [:C, c], [:D, d], a]
     * }
     * </pre>
     *
     * @param unhide The new option.
     * @return A new instance.
     * @see #unhide()
     */
    public @NotNull ParsingOptions withUnhide(final @NotNull UnhideOptions unhide) {
        if (Objects.equals(this.unhide, unhide)) return this;
        return new ParsingOptions(start, usePartial, unhide, embedFailureInParseTree, iterativeDeepening, failureIfEmpty);
    }

    /**
     * Creates a new instance with the {@link #embedFailureInParseTree()} option set to the parameter.
     *
     * <pre>
     * {@code
     *      var p = Alpha.parser("S := #'a'+");
     *      var text = "ab";
     *
     *      // A normal parse results in a failure.
     *      println(p.parse("ab").castToParseFailure().contentsToString());
     *      // => [1, [ParseFailureReason[rule=#"a", reasonString=null, untilEndOfInput=false, tag=regex]], 1, 2, ab]
     *
     *      // With the total option, a parsetree is returned, potentially providing more information about the failure.
     *      var opts = Alpha.ParsingOptions.getDefault().withTotal(true);
     *      println(p.parse("ab", opts));
     *      // => [:S, "a", [:failure, could not parse "b" at 1..2]]
     * }
     * </pre>
     *
     * @param embedFailureInParseTree The new (or old) setting.
     * @return A new instance.
     * @see #embedFailureInParseTree()
     */
    public @NotNull ParsingOptions withEmbedFailureInParseTree(final boolean embedFailureInParseTree) {
        if (Objects.equals(this.embedFailureInParseTree, embedFailureInParseTree)) return this;
        return new ParsingOptions(start, usePartial, unhide, embedFailureInParseTree, iterativeDeepening, failureIfEmpty);
    }

    /**
     * Creates a new instance with the {@link #iterativeDeepening()} option set to the parameter.
     *
     * @param iterativeDeepening The new (or old) setting.
     * @return A new instance.
     * @see #iterativeDeepening()
     */
    public @NotNull ParsingOptions withIterativeDeepening(final boolean iterativeDeepening) {
        if (Objects.equals(this.iterativeDeepening, iterativeDeepening)) return this;
        return new ParsingOptions(start, usePartial, unhide, embedFailureInParseTree, iterativeDeepening, failureIfEmpty);
    }

    /**
     * Creates a new instance with the {@link #failureIfEmpty()} option set to the parameter.
     *
     * @param failureIfEmpty The new (or old) setting.
     * @return A new instance.
     * @see #failureIfEmpty()
     */
    public @NotNull ParsingOptions withFailureIfEmpty(final boolean failureIfEmpty) {
        if (Objects.equals(this.failureIfEmpty, failureIfEmpty)) return this;
        return new ParsingOptions(start, usePartial, unhide, embedFailureInParseTree, iterativeDeepening, failureIfEmpty);
    }
}
