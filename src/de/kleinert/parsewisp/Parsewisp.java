package de.kleinert.parsewisp;

import de.kleinert.parsewisp.error.*;
import de.kleinert.parsewisp.grammar.*;
import de.kleinert.parsewisp.parser.Parser;
import de.kleinert.parsewisp.parser_options.*;
import de.kleinert.parsewisp.result.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.file.Files;
import java.util.Map;

/**
 * The main interface for interacting with the library.
 */
public final class Parsewisp {
    private Parsewisp() {
    }

    /**
     * Creates a parser from a grammar specification, using the default creation options.
     *
     * @param grammar The grammar as a string.
     * @return The parser.
     * @see #parser(String, ParserCreationOptions)
     * @see ParserCreationOptions#getDefault()
     */
    public static @NotNull Parser parser(final @NotNull String grammar) {
        return parser(grammar, ParserCreationOptions.getDefault());
    }

    /**
     * Creates a parser from a grammar specification. For documentation of the options, see {@link ParserCreationOptions}.
     *
     * @param grammar The grammar as a string.
     * @param options The options.
     * @return The parser.
     */
    public static @NotNull Parser parser(final @NotNull String grammar,
                                         final @NotNull ParserCreationOptions options) {
        var grammarForParsingGrammars = CfgGrammar.makeCfg(options);
        var g = Cfg.make(options).buildGrammar(grammar, grammarForParsingGrammars);
        return parser(g, options);
    }

    /**
     * Creates a parser from a grammar specification. For documentation of the options, see {@link ParserCreationOptions}.
     *
     * @param grammar The grammar as a file.
     * @param options The options
     * @return The parser.
     * @throws IOException If the file doesn't exist or can't be accessed.
     * @see #parser(String, ParserCreationOptions)
     */
    public static @NotNull Parser parser(final @NotNull File grammar,
                                         final @NotNull ParserCreationOptions options) throws IOException {
        final @NotNull String contents = Files.readString(grammar.toPath());
        return parser(contents, options);
    }

    /**
     * Creates a parser from a grammar. See {@link #parser(String, ParserCreationOptions)} for what the options do.
     *
     * @param grammar The grammar.
     * @param options The options, most importantly the start production.
     * @return The parser.
     * @throws ParserCreationFailure If the start production is invalid.
     */
    public static @NotNull Parser parser(@NotNull Grammar grammar,
                                         @Nullable ParserCreationOptions options) {
        if (options == null)
            options = ParserCreationOptions.getDefault();

        if (options.startProduction() != null && !grammar.containsKey(options.startProduction()))
            throw new ParserCreationFailure("The start production " + options.startProduction() + " is not in the grammar.");

        try {
            var builder = new GrammarBuilder(options) {
                @Override
                protected void make() {
                }
            };

            var g = builder.buildWithWhitespace(grammar, options.whitespaceParser());

            var start = options.startProduction() != null
                    ? options.startProduction()
                    : grammar.getStartSym();
            return new Parser(g, start);
        } catch (IllegalGrammarException exception) {
            throw new ParserCreationFailure(exception);
        }
    }

    /**
     * Get a whitespace parser (for use in {@link ParserCreationOptions#withWhitespaceParser(Parser)}). The parser is accessed by a keyword.
     * <p>
     * The defined names are the keywords {@code :standard} (ignores spaces, tabs and newlines) and {@code :comma} (which also ignores commas).
     *
     * @param wsParserName The key.
     * @return A parser or null.
     */
    public static @Nullable Parser getPredefinedWhitespaceParser(
            final @Nullable String wsParserName) {
        if (wsParserName == null) {
            return null;
        }
        if (predefinedWsParsers == null) {
            predefinedWsParsers = Map.of(
                    "standard", parser("whitespace = #'\\s+'", ParserCreationOptions.getDefault()),
                    "comma", parser("whitespace = #'[,\\s]+'", ParserCreationOptions.getDefault())
            );
        }
        return predefinedWsParsers.get(wsParserName);
    }

    private static @Nullable Map<String, Parser> predefinedWsParsers = null;
}
