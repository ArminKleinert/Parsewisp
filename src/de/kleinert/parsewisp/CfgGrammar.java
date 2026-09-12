package de.kleinert.parsewisp;

import de.kleinert.parsewisp.grammar.*;
import de.kleinert.parsewisp.parsing.*;
import de.kleinert.parsewisp.parser_options.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A builder for grammars for parsing context free grammars (CFGs).
 */
public final class CfgGrammar extends GrammarBuilder {
    final @NotNull ParserCreationOptions options;

    private CfgGrammar(final @NotNull ParserCreationOptions options) {
        super(options);
        this.options = options;
    }

    private final @NotNull Rule optWhitespace =
            makeCfgOptWhitespaceRhs().enableHideTag();

    private final Set<String> epsilonNames = Set.of("Epsilon", "epsilon", "EPSILON", "eps", "ε");

    private final @NotNull NonTerminal factorNt = nt("factor");

    private final @NotNull NonTerminal ntNt = nt("nt");
    private final @NotNull NonTerminal altOrOrdNt = nt("alt-or-ord");

    private @NotNull Pattern regexDoc(final @NotNull String patternString, final @NotNull String comment) {
        return Pattern.compile(patternString + "(?x) #" + comment);
    }

    private @NotNull Rule makeCfgRulesRhs() {
        final @NotNull Rule rulesRule = concatNoEpsilonMoreThan1(
                List.of(optWhitespace,
                        onceOrMore(nt("rule")) /// {@link #makeCfgRuleRhs}
                ))
                .hideTag();
        return rulesRule;
    }

    private @NotNull Rule makeCfgCommentRhs() {
        final @NotNull Rule rulesRule =
                concatNoEpsilonMoreThan1(
                        List.of(string("(*"),
                                makeCfgInsideCommentRhs(),
                                string("*)")))
                        .hideTag();

//        if (!options.lineCommentIndicators().isEmpty()) {
//            return specialSequence("Linecomment: One of " + options.lineCommentIndicators() + " until end of line.",
//                    string -> {
//                        if (!options.lineCommentIndicators().stream().anyMatch(it -> string.startsWith(it)))
//                            return Optional.empty();
//                var index
//                    });
//        }

        return rulesRule;
    }

    private @NotNull Rule makeCfgInsideCommentRhs() {
        final @NotNull Pattern insideComment = Pattern.compile("(?s)(?:(?!\\(\\*|\\*\\)).)* (?x) # Comment text");
        final @NotNull Rule rulesRule =
                concatNoEpsilonMoreThan1(
                        List.of(regex(insideComment),
                                zeroOrMore(concatNoEpsilonMoreThan1(
                                        List.of(nt("comment"), /// {@link #makeCfgCommentRhs}
                                                regex(insideComment)))
                                )));
        return rulesRule;
    }

    private @NotNull Rule makeCfgOptWhitespaceRhs() {
        final @NotNull Pattern ws = regexDoc("[,\\s]*", "optional whitespace");
        final @NotNull Rule rulesRule =
                concatNoEpsilonMoreThan1(
                        List.of(regex(ws),
                                zeroOrMore(concatNoEpsilonMoreThan1(
                                        List.of(nt("comment"), /// {@link #makeCfgCommentRhs}
                                                regex(ws)))
                                )));
        return rulesRule;
    }

    /**
     * Recognition of {@link EpsilonTerm}.
     *
     * @return A {@link Rule}.
     */
    private @NotNull Rule makeCfgEpsilonRhs() {
        return specialSequence(
                "One of " + epsilonNames,
                text -> epsilonNames.stream().filter(text::startsWith).max(Comparator.comparingInt(String::length))
        );
    }

    private @NotNull Rule makeCfgFactorRhs() {
        final @NotNull Rule rulesRule =
                alternationGuaranteeDistinctAndNotEmpty(
                        List.of(nt("string"), /// {@link #makeCfgStringRhs}
                                nt("regexp"), /// {@link #makeCfgRegexRhs}
                                nt("opt"), /// {@link #makeCfgOptRhs}
                                nt("opt_query"), /// {@link #makeCfgOptQueryRhs}
                                nt("star"), /// {@link #makeCfgZeroOrMoreStarRhs}
                                nt("opt_rep"), /// {@link #makeCfgZeroOrMoreStdRhs}
                                nt("plus"), /// {@link #makeCfgPlusRhs}
                                nt("paren"), /// {@link #makeCfgParenRhs}
                                nt("hide"), /// {@link #makeCfgHideRhs}
                                nt("epsilon"), /// {@link #makeCfgEpsilonRhs}
                                nt("rep"), /// ABNF feature {@link #makeCfgRepRhs}
                                nt("abnf-range"), /// ABNF feature {@link #makeABNFValueRange}
                                nt("eof"), /// {@link #makeEofRhs}
                                ntNt /// {@link #makeCfgNtRhs}
                        ))
                        .hideTag();
        return rulesRule;
    }

    /**
     * Recognition of {@link OnceOrMoreRule}.
     *
     * @return A {@link Rule}.
     */
    private @NotNull Rule makeCfgPlusRhs() {
        final @NotNull Rule rulesRule =
                concatNoEpsilonMoreThan1(
                        List.of(factorNt, /// {@link #makeCfgFactorRhs}
                                optWhitespace,
                                string("+").enableHideTag()));
        return rulesRule;
    }

    /**
     * Recognition of dividers. For example, that is "=" in "S = ...".
     *
     * @return A {@link Rule}.
     */
    private @NotNull Rule makeCfgRuleSeparatorRhs() {
        return altList(List.of(
                stringCS(":="), stringCS("::="), stringCS("=/"),
                stringCS("="), stringCS(":")));
    }

    private @NotNull Rule makeCfgParenRhs() {
        final @NotNull Rule rulesRule =
                concatNoEpsilonMoreThan1(
                        List.of(string("(").enableHideTag(),
                                optWhitespace,
                                altOrOrdNt, /// {@link #makeCfgAltOrOrdRhs}
                                optWhitespace,
                                string(")").enableHideTag()));
        return rulesRule;
    }

    private @NotNull Rule makeCfgHideRhs() {
        final @NotNull Rule rulesRule =
                concatNoEpsilonMoreThan1(
                        List.of(buffer(string("<").enableHideTag()),
                                optWhitespace,
                                altOrOrdNt, /// {@link #makeCfgAltOrOrdRhs}
                                optWhitespace,
                                buffer(string(">").enableHideTag())));
        return rulesRule;
    }


    /**
     * Recognition of {@link StringTerm}.
     *
     * @return A {@link Rule}.
     */
    private @NotNull Rule makeCfgStringRhs() {
        final @NotNull String doubleQuoteStringPrefixed =
                "(%[is])?\\\"[^\\\"\\\\]*(?:\\\\.[^\\\"\\\\]*)*\\\"";
        final @NotNull String singleQuoteStringPrefixed =
                "(%[is])?'[^'\\\\]*(?:\\\\.[^'\\\\]*)*'";

        final @NotNull Pattern doubleQuotedString = regexDoc(
                doubleQuoteStringPrefixed, "Prefixed double-quoted string");
        var doubleQuoteStringRegexRule = regex(doubleQuotedString);

        final @NotNull Pattern singleQuotedString = regexDoc(
                singleQuoteStringPrefixed, "Prefixed single-quoted string");

        return altList(List.of(
                doubleQuoteStringRegexRule,
                regex(singleQuotedString)));
    }

    /**
     * Recognition of {@link RegexTerm}.
     *
     * @return A {@link Rule}.
     */
    private @NotNull Rule makeCfgRegexRhs() {
        final @NotNull Pattern singleQuotedRegex =
                regexDoc("#'[^'\\\\]*(?:\\\\.[^'\\\\]*)*'", "Single-quoted regexp");
        final @NotNull Pattern doubleQuotedRegex =
                regexDoc("#\\\"[^\\\"\\\\]*(?:\\\\.[^\\\"\\\\]*)*\\\"", "Double-quoted regexp");
        final @NotNull Rule rulesRule =
                alternationGuaranteeDistinctAndNotEmpty(
                        List.of(regex(singleQuotedRegex),
                                regex(doubleQuotedRegex)));
        return rulesRule;
    }

    private @NotNull Rule makeCfgRulesOrParserRhs() {
        final @NotNull Rule rulesRule =
                alternationGuaranteeDistinctAndNotEmpty(
                        List.of(nt("rules"), /// {@link #makeCfgRulesRhs}
                                altOrOrdNt /// {@link #makeCfgAltOrOrdRhs}
                        ))
                        .hideTag();
        return rulesRule;
    }

    /**
     * Recognition of {@link NonTerminal}.
     *
     * @return A {@link Rule}.
     */
    private @NotNull Rule makeCfgNtRhs() {
        final Pattern regex = Pattern.compile("[^, \\r\\t\\n<>(){}\\[\\]+*?:=|'\"#&!;./%\\-0-9][^, \\r\\t\\n<>(){}\\[\\]+*?:=|'\"#&!;./%]*");
        //final Pattern regex = Pattern.compile("[a-zA-Z][a-zA-Z0-9_]*");

        return specialSequence(
                "matches " + regex + " but is not reserved for other purposes",
                text -> {
                    final @NotNull Matcher matcher = regex.matcher(text);
                    if (!matcher.lookingAt()) {
                        return Optional.empty();
                    }
                    final String matched = matcher.group();
                    if (epsilonNames.contains(matched)) {
                        return Optional.empty();
                    }
                    if (EOFTerm.text().equals(matched)) {
                        return Optional.empty();
                    }
                    return Optional.of(matched);
                });
    }

    /**
     * Recognition of {@link VariableRepetitionRule}.
     *
     * @return A {@link Rule}.
     */
    private @NotNull Rule makeCfgRepRhs() {
        final @NotNull Rule repRegexChoice =
                regex(Pattern.compile("\\d+(?:\\*\\d*)?|\\*\\d+"));
        final @NotNull Rule rulesRule =
                concatNoEpsilonMoreThan1(List.of(
                        repRegexChoice,
                        optWhitespace,
                        factorNt /// {@link #makeCfgFactorRhs}
                ));
        return rulesRule;
    }

    /**
     * Recognition of {@link LookaheadRule}.
     *
     * @return A {@link Rule}.
     */
    private @NotNull Rule makeCfgLookRhs() {
        final @NotNull Rule rulesRule =
                concatNoEpsilonMoreThan1(
                        List.of(string("&").enableHideTag(),
                                optWhitespace,
                                factorNt /// {@link #makeCfgFactorRhs}
                        ));
        return rulesRule;
    }

    /**
     * Recognition of {@link NegativeLookaheadRule}.
     *
     * @return A {@link Rule}.
     */
    private @NotNull Rule makeCfgNegRhs() {
        final @NotNull Rule rulesRule =
                concatNoEpsilonMoreThan1(
                        List.of(string("!").enableHideTag(),
                                optWhitespace,
                                factorNt /// {@link #makeCfgFactorRhs}
                        ));
        return rulesRule;
    }

    /**
     * Recognition of {@link ZeroOrMoreRule} with the pattern {@code {rule}}.
     *
     * @return A {@link Rule}.
     */
    private @NotNull Rule makeCfgZeroOrMoreStdRhs() {
        final @NotNull Rule rule =
                concatNoEpsilonMoreThan1(
                        List.of(string("{").enableHideTag(),
                                optWhitespace,
                                altOrOrdNt, /// {@link #makeCfgAltOrOrdRhs}
                                optWhitespace,
                                string("}").enableHideTag()));
        return rule;
    }

    /**
     * Recognition of {@link ZeroOrMoreRule} with the pattern {@code rule*}.
     *
     * @return A {@link Rule}.
     */
    private @NotNull Rule makeCfgZeroOrMoreStarRhs() {
        final @NotNull Rule rule =
                concatNoEpsilonMoreThan1(
                        List.of(factorNt, /// {@link #makeCfgFactorRhs}
                                optWhitespace,
                                string("*").enableHideTag()));
        return rule;
    }

    /**
     * Recognition of {@link OptionalRule} with the pattern {@code [rule]}.
     *
     * @return A {@link Rule}.
     */
    private @NotNull Rule makeCfgOptRhs() {
        final @NotNull Rule rule =
                concatNoEpsilonMoreThan1(
                        List.of(string("[").enableHideTag(),
                                optWhitespace,
                                altOrOrdNt, /// {@link #makeCfgAltOrOrdRhs}
                                optWhitespace,
                                string("]").enableHideTag()));
        return rule;
    }

    /**
     * Recognition of {@link OptionalRule} with the pattern {@code rule*}.
     *
     * @return A {@link Rule}.
     */
    private @NotNull Rule makeCfgOptQueryRhs() {
        final @NotNull Rule rule =
                concatNoEpsilonMoreThan1(
                        List.of(factorNt, /// {@link #makeCfgFactorRhs}
                                optWhitespace,
                                string("?").enableHideTag()));
        return rule;
    }

    private @NotNull Rule makeCfgAltOrOrdRhs() {
        final @NotNull Rule rulesRule = alternationGuaranteeDistinctAndNotEmpty(
                List.of(nt("alt"), /// {@link #makeCfgAltRhs}
                        nt("ord") /// {@link #makeCfgOrdRhs}
                )).hideTag();
        return rulesRule;
    }

    private @NotNull Rule makeCfgHideNtRhs() {
        final @NotNull Rule rulesRule =
                concatNoEpsilonMoreThan1(
                        List.of(buffer(string("<").enableHideTag()),
                                optWhitespace,
                                ntNt, /// {@link #makeCfgNtRhs}
                                optWhitespace,
                                buffer(string(">").enableHideTag())));
        return rulesRule;
    }

    private @NotNull Rule makeCfgRuleRhs() {
        final @NotNull Rule optWs = nt("opt-whitespace"); /// {@link #makeCfgOptWhitespaceRhs}
        final @NotNull Rule rulesRule =
                concatNoEpsilonMoreThan1(
                        List.of(alternationGuaranteeDistinctAndNotEmpty(
                                        List.of(ntNt, /// {@link #makeCfgNtRhs}
                                                nt("hide-nt") /// {@link #makeCfgHideNtRhs}
                                        )),
                                optWhitespace,
                                nt("rule-separator").enableHideTag(), /// {@link #makeCfgRuleSeparatorRhs}
                                optWhitespace,
                                altOrOrdNt, /// {@link #makeCfgAltOrOrdRhs}
                                alternationGuaranteeDistinctAndNotEmpty(
                                        List.of(optWs,
                                                concatNoEpsilonMoreThan1(
                                                        List.of(optWs,
                                                                alternationGuaranteeDistinctAndNotEmpty(
                                                                        List.of(string(";"),
                                                                                string("."))),
                                                                optWs))))
                                        .enableHideTag()));
        return rulesRule;
    }

    /**
     * Recognition of {@link OrderedChoiceRule}.
     *
     * @return A {@link Rule}.
     */
    private @NotNull Rule makeCfgOrdRhs() {
        final @NotNull Rule catNt = nt("cat"); /// {@link #makeCfgCatRhs}
        final @NotNull Rule rulesRule =
                concatNoEpsilonMoreThan1(
                        List.of(catNt,
                                zeroOrMore(
                                        concatNoEpsilonMoreThan1(
                                                List.of(optWhitespace,
                                                        string("/").enableHideTag(),
                                                        optWhitespace,
                                                        catNt)))));
        return rulesRule;
    }

    /**
     * Recognition of {@link AlternationRule}.
     *
     * @return A {@link Rule}.
     */
    private @NotNull Rule makeCfgAltRhs() {
        final @NotNull Rule catNt = nt("cat"); /// {@link #makeCfgCatRhs}
        final @NotNull Rule rulesRule =
                concatNoEpsilonMoreThan1(
                        List.of(catNt,
                                zeroOrMore(
                                        concatNoEpsilonMoreThan1(
                                                List.of(optWhitespace,
                                                        string("|").enableHideTag(),
                                                        optWhitespace,
                                                        catNt)))));
        return rulesRule;
    }

    /**
     * Recognition of {@link ConcatRule}.
     *
     * @return A {@link Rule}.
     */
    private @NotNull Rule makeCfgCatRhs() {
        final @NotNull Rule factorLookNeg = alternationGuaranteeDistinctAndNotEmpty(List.of(
                factorNt, /// {@link #makeCfgFactorRhs}
                nt("look"), /// {@link #makeCfgLookRhs}
                nt("neg"), /// {@link #makeCfgNegRhs}
                nt("exclude") /// {@link #makeCfgExclude}
        ));
        final @NotNull Rule rulesRule =
                onceOrMore(
                        concatNoEpsilonMoreThan1(
                                List.of(optWhitespace,
                                        factorLookNeg,
                                        optWhitespace)));
        return rulesRule;
    }

    /**
     * Recognition of {@link ExclusionRule}.
     *
     * @return A {@link Rule}.
     */
    private @NotNull Rule makeCfgExclude() {
        final @NotNull Rule factorLookNeg =
                factorNt; /// {@link #makeCfgFactorRhs}
        final @NotNull Rule rulesRule =
                concatNoEpsilonMoreThan1(
                        List.of(factorLookNeg, optWhitespace,
                                string("-").enableHideTag(),
                                optWhitespace,
                                alternationGuaranteeDistinctAndNotEmpty(List.of(factorLookNeg, nt("exclude")))));
        return rulesRule;
    }

    /**
     * Recognition of {@link EOFTerm}.
     *
     * @return A {@link Rule}.
     */
    private @NotNull Rule makeEofRhs() {
        return string(EOFTerm.text());
    }

    /**
     * Recognition of {@link ValueRangeTerm}.
     *
     * @return A {@link Rule}.
     */
    private @NotNull Rule makeABNFValueRange() {
        final Pattern regex = regexDoc(
                "%b[01]+(\\-[01]+)?|%d[0-9]+(\\-[0-9]+)?|%x[0-9a-fA-F]+(\\-[0-9a-fA-F]+)?",
                "ABNF Value Range"
        );
        return regex(regex);
    }

    @Override
    protected void make() {
        addProduction("rules", makeCfgRulesRhs());
        addProduction("comment", makeCfgCommentRhs());
        //addProduction("inside-comment"), g.makeCfgInsideCommentRhs());
        addProduction("opt-whitespace", makeCfgOptWhitespaceRhs());
        addProduction("rule-separator", makeCfgRuleSeparatorRhs());
        addProduction("rule", makeCfgRuleRhs());
        addProduction("nt", makeCfgNtRhs());
        addProduction("hide-nt", makeCfgHideNtRhs());
        addProduction("paren", makeCfgParenRhs());
        addProduction("hide", makeCfgHideRhs());
        addProduction("cat", makeCfgCatRhs());
        addProduction("string", makeCfgStringRhs());
        addProduction("epsilon", makeCfgEpsilonRhs());
        addProduction("factor", makeCfgFactorRhs());
        addProduction("rules-or-parser", makeCfgRulesOrParserRhs());
        addProduction("alt-or-ord", makeCfgAltOrOrdRhs());
        addProduction("alt", makeCfgAltRhs());
        addProduction("ord", makeCfgOrdRhs()); // PEG extension.
        addProduction("rep", makeCfgRepRhs()); // ABNF
        addProduction("regexp", makeCfgRegexRhs());
        addProduction("opt", makeCfgOptRhs());
        addProduction("opt_query", makeCfgOptQueryRhs());
        addProduction("star", makeCfgZeroOrMoreStarRhs());
        addProduction("opt_rep", makeCfgZeroOrMoreStdRhs());
        addProduction("plus", makeCfgPlusRhs());
        addProduction("look", makeCfgLookRhs());
        addProduction("neg", makeCfgNegRhs());
        addProduction("abnf-range", makeABNFValueRange()); // ABNF
        addProduction("exclude", makeCfgExclude());
        addProduction("eof", makeEofRhs());
    }

    /**
     * Returns the grammar, which is constructed based on the options provided.
     * The grammar can match EBNF grammars, ABNF grammars, or (almost) any mix thereof.
     * For more comprehensive documentation of each option, see {@link ParserCreationOptions}.
     *
     * @param options The options.
     * @return The grammar.
     */
    @NotNull
    public static Grammar makeCfg(final @NotNull ParserCreationOptions options) {
        return new CfgGrammar(options).build();
    }
}
