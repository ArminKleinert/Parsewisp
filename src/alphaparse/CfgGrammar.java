package alphaparse;

import alphaparse.grammar.Grammar;
import alphaparse.grammar.GrammarBuilder;
import alphaparse.parsing.*;
import alphaparse.parser_options.ParserCreationOptions;
import alphaparse.parser_options.RulesAvailable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A builder for grammars for parsing context free grammars (CFGs).
 */
public final class CfgGrammar extends GrammarBuilder {
    final @NotNull Set<RulesAvailable> rulesAvailable;
    final @NotNull ParserCreationOptions options;

    private CfgGrammar(final @NotNull ParserCreationOptions options) {
        super(options);
        this.options = options;
        this.rulesAvailable = options.usableRules();
    }

    private final @NotNull Rule optWhitespace =
            makeCfgOptWhitespaceRhs().enableHideTag();

    private @NotNull List<@NotNull Rule> cListOf(Rule... elements) {
        return Arrays.stream(elements).filter(Objects::nonNull).toList();
    }

    private @Nullable NonTerminal makeNT(
            final @NotNull String symString, final @NotNull RulesAvailable ra) {
        return rulesAvailable.contains(ra) ? nt(symString) : null;
    }

    private final @NotNull NonTerminal factorNt = nt("factor");

    private final @NotNull NonTerminal ntNt = nt("nt");
    private final @NotNull NonTerminal altOrOrdNt = nt("alt-or-ord");

    /*
     * These rules are added later if {@link RulesAvailable.ABNF_CORE} is in the Set of available rules when creating a parser.
     */
    static @NotNull List<Map.Entry<Sym, Rule>> makeAbnfCoreRules() {
        var CRLF = StringTerm.create("\r\n", false);
        var WSP = RegexTerm.create(Pattern.compile("[\\u0020\\u0009]"));

        final @NotNull List<Map.Entry<Sym, Rule>> m = List.of(
                Map.entry(Sym.sym("ALPHA"), RegexTerm.create(Pattern.compile("[a-zA-Z]"))),
                Map.entry(Sym.sym("BIT"), RegexTerm.create(Pattern.compile("[01]"))),
                Map.entry(Sym.sym("CHAR"), RegexTerm.create(Pattern.compile("[\\u0001-\\u007F]"))),
                Map.entry(Sym.sym("CR"), StringTerm.create("\r", false)),
                Map.entry(Sym.sym("CRLF"), CRLF),
                Map.entry(Sym.sym("CTL"), RegexTerm.create(Pattern.compile("[\\u0000-\\u001F|\\u007F]"))),
                Map.entry(Sym.sym("DIGIT"), RegexTerm.create(Pattern.compile("[0-9]"))),
                Map.entry(Sym.sym("DQUOTE"), StringTerm.create("\"", false)),
                Map.entry(Sym.sym("HEXDIG"), RegexTerm.create(Pattern.compile("[0-9a-fA-F]"))),
                Map.entry(Sym.sym("HTAB"), RegexTerm.create(Pattern.compile("\t"))),
                Map.entry(Sym.sym("LF"), RegexTerm.create(Pattern.compile("\n"))),
                Map.entry(Sym.sym("LWSP"), ZeroOrMoreRule.create(AlternationRule.create(List.of(WSP, ConcatRule.create(List.of(CRLF, WSP)))))),
                Map.entry(Sym.sym("OCTET"), RegexTerm.create(Pattern.compile("[\\u0000-\\u00FF]"))),
                Map.entry(Sym.sym("SP"), StringTerm.create(" ", false)),
                Map.entry(Sym.sym("VCHAR"), RegexTerm.create(Pattern.compile("[\\u0021-\\u007E]"))),
                Map.entry(Sym.sym("WSP"), WSP)
        );
        return m;
    }

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
    private Rule makeCfgEpsilonRhs() {
        var epsilonNames = options.epsilonNames();

        // If no epsilon names are provided, use string terminal which matches the empty string `""`.
        // Empty string terminals are simplified to Epsilon later.
        if (epsilonNames.isEmpty())
            return string("\"\"");

        return specialSequence(
                "One of " + epsilonNames,
                text -> epsilonNames.stream().filter(text::startsWith).max(Comparator.comparingInt(String::length))
        );
    }

    private @NotNull Rule makeCfgFactorRhs() {
        final @NotNull Rule rulesRule =
                alternationGuaranteeDistinctAndNotEmpty(
                        cListOf(
                                nt("string"), /// {@link #makeCfgStringRhs}
                                makeNT("regexp", RulesAvailable.REGEX), /// {@link #makeCfgRegexRhs}
                                makeNT("opt", RulesAvailable.OPTIONAL), /// {@link #makeCfgOptRhs}
                                makeNT("opt_query", RulesAvailable.OPTIONAL_QUERY), /// {@link #makeCfgOptQueryRhs}
                                makeNT("star", RulesAvailable.OPTIONAL_REPETITION_STAR), /// {@link #makeCfgZeroOrMoreStarRhs}
                                makeNT("opt_rep", RulesAvailable.OPTIONAL_REPETITION), /// {@link #makeCfgZeroOrMoreStdRhs}
                                makeNT("plus", RulesAvailable.PLUS), /// {@link #makeCfgPlusRhs}
                                nt("paren"), /// {@link #makeCfgParenRhs}
                                nt("hide"), /// {@link #makeCfgHideRhs}
                                nt("epsilon"), /// {@link #makeCfgEpsilonRhs}
                                makeNT("rep", RulesAvailable.VARIABLE_REPEAT), /// ABNF feature {@link #makeCfgRepRhs}
                                makeNT("abnf-range", RulesAvailable.VALUE_RANGE), /// ABNF feature {@link #makeABNFValueRange}
                                makeNT("eof", RulesAvailable.EXPLICIT_EOF), /// {@link #makeEofRhs}
                                ntNt, /// {@link #makeCfgNtRhs}
                                null
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
        return alternationC(
                options.ruleDefinitionOpts()
                        .stream()
                        .map(it -> string(it, false))
                        .toList());
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
        final boolean hasCiPrefixAvailable =
                options.usableRules().contains(RulesAvailable.STRING_CASE_SENSITIVITY_PREFIX);

        final @NotNull String doubleQuoteString = "\\\"[^\\\"\\\\]*(?:\\\\.[^\\\"\\\\]*)*\\\"";
        final @NotNull String doubleQuoteStringPrefixed = "(%[is])?" + doubleQuoteString;
        final @NotNull String singleQuoteString = "'[^'\\\\]*(?:\\\\.[^'\\\\]*)*'";
        final @NotNull String singleQuoteStringPrefixed = "(%[is])?" + singleQuoteString;

        final @NotNull Pattern doubleQuotedString = hasCiPrefixAvailable
                ? regexDoc(doubleQuoteStringPrefixed, "Prefixed double-quoted string")
                : regexDoc(doubleQuoteString, "Double-quoted string");
        var doubleQuoteStringRegexRule = regex(doubleQuotedString);

        if (!options.usableRules().contains(RulesAvailable.SINGLY_QUOTED))
            return doubleQuoteStringRegexRule;

        final @NotNull Pattern singleQuotedString = hasCiPrefixAvailable
                ? regexDoc(singleQuoteStringPrefixed, "Prefixed single-quoted string")
                : regexDoc(singleQuoteString, "Single-quoted string");

        return alternationC(List.of(
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
        final var regex = rulesAvailable.contains(RulesAvailable.EXTENDED_IDENTIFIERS)
                ? Pattern.compile("[^, \\r\\t\\n<>(){}\\[\\]+*?:=|'\"#&!;./%\\-0-9][^, \\r\\t\\n<>(){}\\[\\]+*?:=|'\"#&!;./%]*")
                : (rulesAvailable.contains(RulesAvailable.ABNF_IDENTIFIERS)
                ? Pattern.compile("[a-zA-Z][a-zA-Z0-9\\-]*")
                : Pattern.compile("[a-zA-Z][a-zA-Z0-9_]*"));
        final boolean eofPossible = options.usableRules().contains(RulesAvailable.EXPLICIT_EOF);

        return specialSequence(
                "matches " + regex + " but is not reserved for other purposes",
                text -> {
                    final @NotNull Matcher matcher = regex.matcher(text);
                    if (!matcher.lookingAt()) {
                        return Optional.empty();
                    }
                    final String matched = matcher.group();
                    if (options.epsilonNames().contains(matched)) {
                        return Optional.empty();
                    }
                    if (eofPossible && EOFTerm.text().equals(matched)) {
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
        final @NotNull Rule repRegexChoice;
        if (!rulesAvailable.contains(RulesAvailable.OPTIONAL_REPETITION_STAR)) {
            repRegexChoice =
                    regex(Pattern.compile("\\d*\\*?\\d*"));
        } else {
            repRegexChoice =
                    regex(Pattern.compile("\\d+(?:\\*\\d*)?|\\*\\d+"));
        }
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
        int i = 0;
        Rule[] l = new Rule[2];

        if (options.usableRules().contains(RulesAvailable.ALTERNATION))
            l[i++] = nt("alt"); /// {@link #makeCfgAltRhs}
        if (options.usableRules().contains(RulesAvailable.ORDERED_CHOICE))
            l[i++] = nt("ord"); /// {@link #makeCfgOrdRhs}

        if (i == 0) return onceOrMore(nt("cat")).hideTag(); /// {@link #makeCfgCatRhs}

        if (i == 1) return buffer(l[0].hideTag());

        final @NotNull Rule rulesRule = alternationGuaranteeDistinctAndNotEmpty(Arrays.asList(l)).hideTag();
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
        final @NotNull Rule factorLookNeg = alternationGuaranteeDistinctAndNotEmpty(cListOf(
                factorNt, /// {@link #makeCfgFactorRhs}
                makeNT("look", RulesAvailable.LOOKAHEAD), /// {@link #makeCfgLookRhs}
                makeNT("neg", RulesAvailable.NEGATIVE_LOOKAHEAD), /// {@link #makeCfgNegRhs}
                makeNT("exclude", RulesAvailable.EXCLUSION) /// {@link #makeCfgExclude}
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
    public void make() {
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

        if (rulesAvailable.contains(RulesAvailable.ALTERNATION))
            addProduction("alt", makeCfgAltRhs());

        if (rulesAvailable.contains(RulesAvailable.ORDERED_CHOICE))
            addProduction("ord", makeCfgOrdRhs()); // Technically ABNF, but should be included without it as a PAKRAT extension.

        if (rulesAvailable.contains(RulesAvailable.VARIABLE_REPEAT))
            addProduction("rep", makeCfgRepRhs()); // ABNF

        if (rulesAvailable.contains(RulesAvailable.REGEX))
            addProduction("regexp", makeCfgRegexRhs());

        if (rulesAvailable.contains(RulesAvailable.OPTIONAL))
            addProduction("opt", makeCfgOptRhs());

        if (rulesAvailable.contains(RulesAvailable.OPTIONAL_QUERY))
            addProduction("opt_query", makeCfgOptQueryRhs());

        if (rulesAvailable.contains(RulesAvailable.OPTIONAL_REPETITION_STAR))
            addProduction("star", makeCfgZeroOrMoreStarRhs());

        if (rulesAvailable.contains(RulesAvailable.OPTIONAL_REPETITION))
            addProduction("opt_rep", makeCfgZeroOrMoreStdRhs());

        if (rulesAvailable.contains(RulesAvailable.PLUS))
            addProduction("plus", makeCfgPlusRhs());

        if (rulesAvailable.contains(RulesAvailable.LOOKAHEAD))
            addProduction("look", makeCfgLookRhs());

        if (rulesAvailable.contains(RulesAvailable.NEGATIVE_LOOKAHEAD))
            addProduction("neg", makeCfgNegRhs());

        if (rulesAvailable.contains(RulesAvailable.VALUE_RANGE))
            addProduction("abnf-range", makeABNFValueRange()); // ABNF

        if (rulesAvailable.contains(RulesAvailable.EXCLUSION))
            addProduction("exclude", makeCfgExclude());

        if (rulesAvailable.contains(RulesAvailable.EXPLICIT_EOF))
            addProduction("eof", makeEofRhs());
    }

    /**
     * Returns the grammar, which is constructed based on the options provided.
     * The grammar can match EBNF grammars, ABNF grammars, or (almost) any mix thereof.
     * For more comprehensive documentation of each option, see {@link ParserCreationOptions}.
     * @param options The options.
     * @return The grammar.
     */
    @NotNull
    public static Grammar makeCfg(final @NotNull ParserCreationOptions options) {
        return new CfgGrammar(options).build();
    }
}
