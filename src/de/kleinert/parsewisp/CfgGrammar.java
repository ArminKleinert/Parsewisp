package de.kleinert.parsewisp;

import de.kleinert.parsewisp.grammar.Grammar;
import de.kleinert.parsewisp.grammar.GrammarBuilder;
import de.kleinert.parsewisp.parser_options.RedefinitionOption;
import de.kleinert.parsewisp.parsing.EOFTerm;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A builder for grammars with Parsewisp's default format.
 *
 * @since 0.9.7
 */
public final class CfgGrammar extends GrammarBuilder {
    CfgGrammar() {
        super(RedefinitionOption.ERROR);
    }

    @Override
    protected void make() {
        var cWsp = hide(nt("cWsp"));
        var ntNt = nt("nt");
        var factorNt = nt("factor");
        var altNt = nt("alt");

        addProduction("rules", catUnsafe(List.of(
                cWsp, onceOrMore(catUnsafe(List.of(nt("rule"), cWsp))))));

        addProduction("rule", catUnsafe(List.of(
                altUnsafe(List.of(ntNt, nt("hide-nt"))),
                cWsp,
                altUnsafe(List.of(
                        stringCS(":="), stringCS("::="), stringCS("=/"),
                        stringCS("="), stringCS(":"))).hideInResult(),
                cWsp,
                altNt,
                opt(altUnsafe(List.of(string(";"), string(".")))))));

        addProduction("alt", catUnsafe(List.of(
                nt("ord"),
                cWsp,
                zeroOrMore(catUnsafe(List.of(
                        hide(string("|")),
                        cWsp,
                        nt("ord")))))));

        addProduction("ord", catUnsafe(List.of(
                nt("cat"),
                cWsp,
                zeroOrMore(catUnsafe(List.of(
                        hide(string("/")),
                        cWsp,
                        nt("cat")))))));

        addProduction("cat", catUnsafe(List.of(
                nt("term"),
                zeroOrMore(catUnsafe(List.of(cWsp, nt("term")))))));

        addProduction("term", altUnsafe(List.of(
                nt("exclude"), nt("look"), nt("neg"))).mergeToTop());

        addProduction("exclude", catUnsafe(List.of(
                factorNt, cWsp, opt(catUnsafe(List.of(string("-").hideInResult(), cWsp, factorNt))))));

        addProduction("look", catUnsafe(List.of(string("&").hideInResult(), cWsp, factorNt)));
        addProduction("neg", catUnsafe(List.of(string("!").hideInResult(), cWsp, factorNt)));

        addProduction("factor", catUnsafe(List.of(
                nt("primary"),
                zeroOrMore(catUnsafe(List.of(
                        cWsp,
                        altUnsafe(List.of(
                                string("?"), string("*"), string("+"),
                                regex(Pattern.compile("\\{\\d*(,\\d*)?}(?x) # Numbered repetition")))))
                )))));

        addProduction("primary", altUnsafe(List.of(
                nt("string"), nt("regexp"), nt("opt"),
                nt("opt_rep"), nt("paren"), nt("hide"),
                //nt("epsilon"),
                nt("abnf-range"), //nt("eof"),
                nt("ntOrEpsOrEof"))).mergeToTop());

        addProduction("string", altUnsafe(List.of(
                regex(Pattern.compile("(%[is])?\"[^\"\\\\]*(?:\\\\.[^\"\\\\]*)*\"(?x) # Double quoted string")),
                regex(Pattern.compile("(%[is])?'[^'\\\\]*(?:\\\\.[^'\\\\]*)*'(?x) # Single quoted string")))));

        addProduction("regexp", altUnsafe(List.of(
                regex(Pattern.compile("#'[^'\\\\]*(?:\\\\.[^'\\\\]*)*'(?x) # Single quoted string")),
                regex(Pattern.compile("#\"[^\"\\\\]*(?:\\\\.[^\"\\\\]*)*\"(?x) # Double quoted regex")))));

        addProduction("opt", catUnsafe(List.of(
                string("[").hideInResult(), cWsp, altNt, cWsp, string("]").hideInResult())));

        addProduction("opt_rep", catUnsafe(List.of(
                string("{").hideInResult(), cWsp, altNt, cWsp, string("}").hideInResult())));

        addProduction("paren", catUnsafe(List.of(
                string("(").hideInResult(), cWsp, altNt, cWsp, string(")").hideInResult())));

        addProduction("hide", catUnsafe(List.of(
                string("<").hideInResult(), cWsp, altNt, cWsp, string(">").hideInResult())));

        addProduction("abnf-range", regex(Pattern.compile(
                "%b[01]+(-[01]+)?|%d[0-9]+(-[0-9]+)?|%x[0-9a-fA-F]+(-[0-9a-fA-F]+)?(?x) # ABNF range")));

        final Pattern ntRegex = Pattern.compile(
                "[^, \\r\\t\\n<>(){}\\[\\]+*?:=|'\"#&!;./%\\-0-9][^, \\r\\t\\n<>(){}\\[\\]+*?:=|'\"#&!;./%]*");
        addProduction("ntOrEpsOrEof", regex(ntRegex));

        var epsTerms = Set.of("Epsilon", "epsilon", "EPSILON", "eps", "ε");
        addProduction("nt", specialSequence(
                "Valid nonterminal name, excluding any variant of epsilon or EOF",
                text -> {
                    final @NotNull Matcher matcher = ntRegex.matcher(text);
                    if (!matcher.lookingAt()) {
                        return Optional.empty();
                    }
                    final String matched = matcher.group();
                    if (epsTerms.contains(matched) || EOFTerm.text().equals(matched)) {
                        return Optional.empty();
                    }
                    return Optional.of(matched);
                }));

        addProduction("hide-nt", catUnsafe(List.of(
                string("<").hideInResult(),
                cWsp,
                ntNt,
                cWsp,
                string(">").hideInResult())));

        addProduction("cWsp", hide(onceOrMore(altUnsafe(List.of(
                nt("comment"),
                regex(Pattern.compile("[,\\s]*")))))));

        addProduction("comment", catUnsafe(List.of(
                string("(*"),
                onceOrMore(altUnsafe(List.of(
                        regex("(?s)(?:(?!\\(\\*|\\*\\)).)*(?x) # Comment text"),
                        nt("comment")
                ))),
                string("*)"))));
    }

    /**
     * Returns the base grammar for Parsewisp.
     *
     * @return The base grammar for Parsewisp.
     * @since 0.9.7
     */
    @NotNull
    public static Grammar makeCfg() {
        return new CfgGrammar().build();
    }
}