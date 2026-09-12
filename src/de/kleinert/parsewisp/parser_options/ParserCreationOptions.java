package de.kleinert.parsewisp.parser_options;

import de.kleinert.parsewisp.Parsewisp;
import de.kleinert.parsewisp.Sym;
import de.kleinert.parsewisp.parser.Parser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;

/// **
// * This class provides options for creating {@link Parser} instances.
// *
// * @param whitespaceParser   A parser which is used to ignore whitespaces between words or characters. This parser is merged into the new parser when the creation options are used.
// * @param startProduction    The starting production name of the parser.
// * @param redefinitionOption Sets what to do when a production appears twice in the definition.
// * @param usableRules        A Set of rules that can be used when building the parser. See {@link RulesAvailable}.
// * @param checkCorrectness   Whether to check the correctness of the grammar when creating the parser.
// * @param ruleDefinitionOpts A collection of possible "definition operators" for rules.
// * @param epsilonNames       A collection of possible epsilon names. If null, use {@link #defaultEpsilonNames()}.
// */
@Unmodifiable
public class ParserCreationOptions {
    private final @Nullable Parser whitespaceParser;
    private final @Nullable Sym startProduction;
    private final @NotNull RedefinitionOption redefinitionOption;
    private final boolean checkCorrectness;

    public ParserCreationOptions(@Nullable Parser whitespaceParser,
                                 @Nullable Sym startProduction,
                                 @NotNull RedefinitionOption redefinitionOption,
                                 boolean checkCorrectness) {
        this.whitespaceParser = whitespaceParser;
        this.startProduction = startProduction;
        this.redefinitionOption = redefinitionOption;
        this.checkCorrectness = checkCorrectness;
    }

    public @Nullable Parser whitespaceParser() {
        return whitespaceParser;
    }

    public @Nullable Sym startProduction() {
        return startProduction;
    }

    public @NotNull RedefinitionOption redefinitionOption() {
        return redefinitionOption;
    }

    public boolean checkCorrectness() {
        return checkCorrectness;
    }

    private static final boolean defaultCheckCorrectness = true;

    /**
     * Constructor.
     *
     * @param whitespaceParser   A parser which is used to ignore whitespaces between words or characters. This parser is merged into the new parser when the creation options are used. If null, no such parser is used.
     * @param startProduction    The starting production name of the parser. If null, the first defined production is used.
     * @param redefinitionOption Sets what to do when a production appears twice in the definition.
     * @param checkCorrectness   Whether to check the correctness of the grammar when creating the parser.
     * @return A new instance.
     */
    public static @NotNull ParserCreationOptions create(
            final @Nullable Parser whitespaceParser,
            final @Nullable Sym startProduction,
            final @Nullable RedefinitionOption redefinitionOption,
            final boolean checkCorrectness) {
        var redefinitionOption1 = redefinitionOption == null
                ? RedefinitionOption.defaultOption
                : redefinitionOption;

        return new ParserCreationOptions(
                whitespaceParser, startProduction, redefinitionOption1,
                checkCorrectness);
    }

    /**
     * Creates a new instance with the whitespace-ignoring parser set.
     *
     * @param whitespaceParser The parser (or null).
     * @return A new instance.
     */
    public @NotNull ParserCreationOptions withWhitespaceParser(
            final @Nullable Parser whitespaceParser) {
        if (Objects.equals(this.whitespaceParser(), whitespaceParser))
            return this;
        return ParserCreationOptions.create(
                whitespaceParser, startProduction,
                redefinitionOption,
                checkCorrectness);
    }

    /**
     * Creates a new instance with the start production set.
     *
     * @param startProduction The start production's name.
     * @return A new instance.
     */
    public @NotNull ParserCreationOptions withStartProduction(
            final @Nullable Sym startProduction) {
        if (Objects.equals(this.startProduction(), startProduction))
            return this;
        return ParserCreationOptions.create(
                whitespaceParser, startProduction,
                redefinitionOption,
                checkCorrectness);
    }

    /**
     * Sets what to do when a production appears twice in the definition.
     *
     * @param redefinitionOption Sets what to do when a production appears twice in the definition.
     * @return A new instance.
     */
    public @NotNull ParserCreationOptions withRedefinitionOption(
            final RedefinitionOption redefinitionOption) {
        return ParserCreationOptions.create(
                whitespaceParser, startProduction,
                redefinitionOption,
                checkCorrectness);
    }

    /**
     * Creates a new instance with {@link ParserCreationOptions#checkCorrectness()} set to the parameter.
     *
     * @param checkCorrectness The new setting for {@link ParserCreationOptions#checkCorrectness()}.
     * @return A new instance.
     */
    public @NotNull ParserCreationOptions withCorrectnessCheck(
            final boolean checkCorrectness) {
        return ParserCreationOptions.create(
                whitespaceParser, startProduction,
                redefinitionOption,
                checkCorrectness);
    }

    /**
     * Creates a new instance using the most common whitespace parser.
     * <pre>
     * {@code
     *
     *   // With whitespace parser:
     *   var p = Parsewisp.parser("S = ('a' | 'b')*");
     *   println(p.parse("a b      a\tb\na")); // Error
     *
     *   // With whitespace parser:
     *   var p = Parsewisp.parser("S = ('a' | 'b')*", Parsewisp.ParserCreationOptions.newWithStandardWhitespace());
     *   println(p.parse("a b      a\tb\na")); // [:S, a, b, a, b, a]
     * }
     * </pre>
     *
     * @return A new instance.
     */
    public static @NotNull ParserCreationOptions newWithStandardWhitespace() {
        return ParserCreationOptions
                .getDefault()
                .withWhitespaceParser(Parsewisp.getPredefinedWhitespaceParser("standard"));
    }

    /**
     * The default settings. Equivalent to {@link #ParserCreationOptions(Parser, Sym, RedefinitionOption, boolean)} with using {@code null} or whichever defaults this class provides.
     *
     * <p>
     * Characteristics:
     * <ul>
     *     <li>Redefinition option: {@link RedefinitionOption#defaultOption}</li>
     * </ul>
     *
     * @return default settings.
     */
    public static @NotNull ParserCreationOptions getDefault() {
        return ParserCreationOptions.create(
                null, null, null, defaultCheckCorrectness);
    }
}
