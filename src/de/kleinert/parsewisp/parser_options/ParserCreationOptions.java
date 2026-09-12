package de.kleinert.parsewisp.parser_options;

import de.kleinert.parsewisp.Parsewisp;
import de.kleinert.parsewisp.Sym;
import de.kleinert.parsewisp.parser.Parser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;

///**
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
    @Nullable Parser whitespaceParser;
    @Nullable Sym startProduction;
    @NotNull RedefinitionOption redefinitionOption;
    boolean checkCorrectness;
    @NotNull Collection<@NotNull String> ruleDefinitionOpts;

    public ParserCreationOptions(@Nullable Parser whitespaceParser,
    @Nullable Sym startProduction,
    @NotNull RedefinitionOption redefinitionOption,
    boolean checkCorrectness,
    @NotNull Collection<@NotNull String> ruleDefinitionOpts){
        this.  whitespaceParser   =     whitespaceParser     ;
        this. startProduction    =  startProduction        ;
        this.  redefinitionOption   =     redefinitionOption     ;
        this. checkCorrectness    =     checkCorrectness     ;
        this.   ruleDefinitionOpts  =       ruleDefinitionOpts   ;
    }

    public @Nullable Parser whitespaceParser(){return  whitespaceParser       ;}
    public @Nullable Sym startProduction(){return        startProduction ;}
    public @NotNull RedefinitionOption redefinitionOption(){return         redefinitionOption;}

    public boolean checkCorrectness(){return         checkCorrectness;}
    public @NotNull Collection<@NotNull String> ruleDefinitionOpts(){return   ruleDefinitionOpts      ;}

    private static final boolean defaultCheckCorrectness = true;

    /**
     * Default for {@link ParserCreationOptions#ruleDefinitionOpts()}.
     * <p>
     * Value (might not be up to date): {@code List.of(":=", "::=", "=", ":")}
     *
     * @return List of strings.
     */
    public static @NotNull @Unmodifiable List<String> defaultRuleDefinitionOps() {
        return List.of(":=", "::=", "=", ":", "←", "<-");
    }

    /**
     * Constructor.
     *
     * @param whitespaceParser   A parser which is used to ignore whitespaces between words or characters. This parser is merged into the new parser when the creation options are used. If null, no such parser is used.
     * @param startProduction    The starting production name of the parser. If null, the first defined production is used.
     * @param redefinitionOption Sets what to do when a production appears twice in the definition.
     * @param checkCorrectness   Whether to check the correctness of the grammar when creating the parser.
     * @param ruleDefinitionOpts A collection of possible "definition operators" for rules. If null, use {@link #defaultRuleDefinitionOps()}. Example: {@code List.of(":=", "::=", "=", ":")}
     * @return A new instance.
     */
    public static @NotNull ParserCreationOptions create(
            final @Nullable Parser whitespaceParser,
            final @Nullable Sym startProduction,
            final @Nullable RedefinitionOption redefinitionOption,
            final boolean checkCorrectness,
            final @Nullable Collection<String> ruleDefinitionOpts) {
        var redefinitionOption1 = redefinitionOption == null
                ? RedefinitionOption.defaultOption
                : redefinitionOption;
        var ruleDefinitionOpts1 = ruleDefinitionOpts == null
                ? defaultRuleDefinitionOps()
                : ruleDefinitionOpts;

        if (ruleDefinitionOpts1.isEmpty())
            throw new IllegalArgumentException("Empty rule definition operator list.");

        return new ParserCreationOptions(
                whitespaceParser, startProduction, redefinitionOption1,
                checkCorrectness, ruleDefinitionOpts1);
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
                checkCorrectness, ruleDefinitionOpts);
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
                checkCorrectness, ruleDefinitionOpts);
    }

//    /**
//     * Creates a new instance with string case-insensitivity set to the parameter.
//     *
//     * @param stringCaseInsensitive The setting for the case-insensitivity.
//     * @return A new instance.
//     */
//    public @NotNull ParserCreationOptions withStringCaseInsensitive(
//            final @Nullable GlobalCaseInsensitivity stringCaseInsensitive) {
//        if (Objects.equals(this.stringCaseInsensitive(), stringCaseInsensitive))
//            return this;
//        return ParserCreationOptions.create(
//                whitespaceParser, startProduction,
//                redefinitionOption, usableRules,
//                checkCorrectness, ruleDefinitionOpts);
//    }
//
//    /**
//     * Creates a new instance with string case-insensitivity set to the parameter. The parameter here is a boolean.
//     * {@code true} becomes {@link GlobalCaseInsensitivity#TRUE}. {@link GlobalCaseInsensitivity#FALSE}
//     *
//     * @param stringCaseInsensitive The setting for the case-insensitivity.
//     * @return A new instance.
//     */
//    public @NotNull ParserCreationOptions withStringCaseInsensitive(
//            final boolean stringCaseInsensitive) {
//        return withStringCaseInsensitive(stringCaseInsensitive
//                ? GlobalCaseInsensitivity.TRUE
//                : GlobalCaseInsensitivity.FALSE);
//    }

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
                checkCorrectness, ruleDefinitionOpts);
    }

//    /**
//     * Creates a new instance with {@link ParserCreationOptions#usableRules()} set to the parameter.
//     *
//     * @param usableRules The new setting for {@link ParserCreationOptions#usableRules()}.
//     * @return A new instance.
//     */
//    public @NotNull ParserCreationOptions withRulesAvailable(
//            final @Nullable Set<RulesAvailable> usableRules) {
//        return ParserCreationOptions.create(
//                whitespaceParser, startProduction,
//                redefinitionOption,
//                checkCorrectness, ruleDefinitionOpts);
//    }

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
                checkCorrectness, ruleDefinitionOpts);
    }

    /**
     * Creates a new instance with {@link ParserCreationOptions#ruleDefinitionOpts()} set to the parameter.
     *
     * @param ruleDefinitionOps The new setting for {@link ParserCreationOptions#ruleDefinitionOpts()}.
     * @return A new instance.
     */
    public @NotNull ParserCreationOptions withRuleDefinitionOps(
            final @Nullable Collection<String> ruleDefinitionOps) {
        return ParserCreationOptions.create(
                whitespaceParser, startProduction,
                redefinitionOption,
                checkCorrectness, ruleDefinitionOps);
    }

//    /**
//     * Creates a new instance with {@link ParserCreationOptions#epsilonNames()} set to the parameter.
//     *
//     * @param epsilonNames The new setting for {@link ParserCreationOptions#epsilonNames()}.
//     * @return A new instance.
//     */
//    public @NotNull ParserCreationOptions withEpsilonNames(
//            final @Nullable Collection<String> epsilonNames) {
//        return ParserCreationOptions.create(
//                whitespaceParser, startProduction,
//                redefinitionOption, usableRules,
//                checkCorrectness, ruleDefinitionOpts, epsilonNames);
//    }

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
     * The default settings. Equivalent to {@link #ParserCreationOptions(Parser, Sym, RedefinitionOption, boolean, Collection)} with using {@code null} or whichever defaults this class provides.
     *
     * <p>
     * Characteristics:
     * <ul>
     *     <li>Rule definition operators: See {@link #defaultRuleDefinitionOps}</li>
     *     <li>Redefinition option: {@link RedefinitionOption#defaultOption}</li>
     * </ul>
     *
     * @return default settings.
     */
    public static @NotNull ParserCreationOptions getDefault() {
        return ParserCreationOptions.create(
                null, null,
                null,
                defaultCheckCorrectness, null);
    }

//    /**
//     * ABNF settings: Strings are case-insensitive, redefinition of productions with {@code =/} creates a choice rule.
//     * <p>
//     * Characteristics:
//     * <ul>
//     *     <li>Rule definition operators: {@code "=", "=/"}</li>
//     *     <li>Case insensitivity: {@code true}</li>
//     *     <li>Redefinition option: {@link RedefinitionOption#CHOICE}</li>
//     *     <li>Epsilon equivalents: {@code "ε"}</li>
//     * </ul>
//     * <p>
//     * Available rules:
//     * <ul>
//     *     <li>{@link RulesAvailable#ABNF_CORE}</li>
//     *     <li>{@link RulesAvailable#ABNF_IDENTIFIERS}</li>
//     *     <li>{@link RulesAvailable#OPTIONAL}</li>
//     *     <li>{@link RulesAvailable#ORDERED_CHOICE}</li>
//     *     <li>{@link RulesAvailable#REGEX}</li>
//     *     <li>{@link RulesAvailable#STRING_CASE_SENSITIVITY_PREFIX}</li>
//     *     <li>{@link RulesAvailable#VALUE_RANGE}</li>
//     *     <li>{@link RulesAvailable#VARIABLE_REPEAT}</li>
//     * </ul>
//     *
//     * @return Options for ABNF parsers.
//     */
//    public static @NotNull ParserCreationOptions abnf() {
//        var rules = EnumSet.of(
//                RulesAvailable.ABNF_CORE,
//                RulesAvailable.ABNF_IDENTIFIERS,
//                RulesAvailable.OPTIONAL,
//                RulesAvailable.ORDERED_CHOICE,
//                RulesAvailable.REGEX,
//                RulesAvailable.STRING_CASE_SENSITIVITY_PREFIX,
//                RulesAvailable.VALUE_RANGE,
//                RulesAvailable.VARIABLE_REPEAT);
//        return ParserCreationOptions.create(
//                null, null, GlobalCaseInsensitivity.TRUE,
//                RedefinitionOption.CHOICE, rules, defaultCheckCorrectness,
//                List.of("=/", "="),
//                List.of("ε")
//        );
//    }

//    /**
//     * EBNF settings.
//     * <p>
//     * Characteristics:
//     * <ul>
//     *     <li>Rule definition operators: {@code "=", "::=", "=", ":"}</li>
//     *     <li>Case insensitivity: {@code false}</li>
//     *     <li>Redefinition option: {@link RedefinitionOption#defaultOption}</li>
//     *     <li>Epsilon equivalents: {@code "ε"}</li>
//     * </ul>
//     * <p>
//     * Available rules:
//     * <ul>
//     *     <li>{@link RulesAvailable#ALTERNATION}</li>
//     *     <li>{@link RulesAvailable#EXCLUSION}</li>
//     *     <li>{@link RulesAvailable#LOOKAHEAD}</li>
//     *     <li>{@link RulesAvailable#NEGATIVE_LOOKAHEAD}</li>
//     *     <li>{@link RulesAvailable#OPTIONAL_QUERY},
//     *     <li>{@link RulesAvailable#OPTIONAL_REPETITION_STAR}</li>
//     *     <li>{@link RulesAvailable#OPTIONAL_REPETITION}</li>
//     *     <li>{@link RulesAvailable#OPTIONAL}</li>
//     *     <li>{@link RulesAvailable#PLUS}</li>
//     *     <li>{@link RulesAvailable#REGEX}</li>
//     *     <li>{@link RulesAvailable#SINGLY_QUOTED}</li>
//     * </ul>
//     *
//     * @return Options for EBNF parsers.
//     */
//    public static @NotNull ParserCreationOptions ebnf() {
//        var rules = EnumSet.of(
//                RulesAvailable.ALTERNATION,
//                RulesAvailable.EXCLUSION,
//                RulesAvailable.LOOKAHEAD,
//                RulesAvailable.NEGATIVE_LOOKAHEAD,
//                RulesAvailable.OPTIONAL,
//                RulesAvailable.OPTIONAL_QUERY,
//                RulesAvailable.OPTIONAL_REPETITION,
//                RulesAvailable.OPTIONAL_REPETITION_STAR,
//                RulesAvailable.PLUS,
//                RulesAvailable.REGEX,
//                RulesAvailable.SINGLY_QUOTED);
//        return ParserCreationOptions.create(
//                null, null, GlobalCaseInsensitivity.FALSE,
//                RedefinitionOption.defaultOption, rules, defaultCheckCorrectness,
//                List.of("="),
//                List.of("ε")
//        );
//    }

//    /**
//     * PEG settings.
//     * <p>
//     * Characteristics:
//     * <ul>
//     *     <li>Rule definition operators: {@code "←", "<-"}</li>
//     *     <li>Case insensitivity: {@code false}</li>
//     *     <li>Redefinition option: {@link RedefinitionOption#defaultOption}</li>
//     *     <li>Epsilon equivalents: {@code "ε"}</li>
//     * </ul>
//     * <p>
//     * Available rules:
//     * <ul>
//     *     <li>{@link RulesAvailable#LOOKAHEAD}</li>
//     *     <li>{@link RulesAvailable#NEGATIVE_LOOKAHEAD}</li>
//     *     <li>{@link RulesAvailable#OPTIONAL_QUERY},
//     *     <li>{@link RulesAvailable#OPTIONAL_REPETITION_STAR}</li>
//     *     <li>{@link RulesAvailable#ORDERED_CHOICE}</li>
//     *     <li>{@link RulesAvailable#PLUS}</li>
//     *     <li>{@link RulesAvailable#REGEX}</li>
//     *     <li>{@link RulesAvailable#SINGLY_QUOTED}</li>
//     * </ul>
//     *
//     * @return Options for PEG parsers.
//     */
//    public static @NotNull ParserCreationOptions peg() {
//        var rules = EnumSet.of(
//                RulesAvailable.LOOKAHEAD,
//                RulesAvailable.NEGATIVE_LOOKAHEAD,
//                RulesAvailable.OPTIONAL_QUERY,
//                RulesAvailable.OPTIONAL_REPETITION_STAR,
//                RulesAvailable.ORDERED_CHOICE,
//                RulesAvailable.PLUS,
//                RulesAvailable.REGEX,
//                RulesAvailable.SINGLY_QUOTED);
//        return ParserCreationOptions.create(
//                null, null, GlobalCaseInsensitivity.FALSE,
//                RedefinitionOption.defaultOption, rules, defaultCheckCorrectness,
//                List.of("←", "<-"),
//                List.of("ε")
//        );
//    }

//    /**
//     * EBNF settings without addons.
//     * <p>
//     * Characteristics:
//     * <ul>
//     *     <li>Rule definition operators: {@code "="}</li>
//     *     <li>Case insensitivity: {@code false}</li>
//     *     <li>Redefinition option: {@link RedefinitionOption#defaultOption}</li>
//     *     <li>Epsilon equivalents: {@code "ε"}</li>
//     * </ul>
//     * <p>
//     * Available rules:
//     * <ul>
//     *     <li>{@link RulesAvailable#ALTERNATION}</li>
//     *     <li>{@link RulesAvailable#EXCLUSION}</li>
//     *     <li>{@link RulesAvailable#OPTIONAL_REPETITION}</li>
//     *     <li>{@link RulesAvailable#OPTIONAL}</li>
//     *     <li>{@link RulesAvailable#REGEX}</li>
//     *     <li>{@link RulesAvailable#SINGLY_QUOTED}</li>
//     * </ul>
//     *
//     * @return Options for EBNF parsers.
//     */
//    public static @NotNull ParserCreationOptions pureEbnf() {
//        var rules = EnumSet.of(
//                RulesAvailable.ALTERNATION,
//                RulesAvailable.EXCLUSION,
//                RulesAvailable.OPTIONAL,
//                RulesAvailable.OPTIONAL_REPETITION,
//                RulesAvailable.REGEX,
//                RulesAvailable.SINGLY_QUOTED);
//        return ParserCreationOptions.create(
//                null, null, GlobalCaseInsensitivity.FALSE,
//                RedefinitionOption.defaultOption, rules, defaultCheckCorrectness,
//                List.of("="),
//                List.of("ε")
//        );
//    }
}
