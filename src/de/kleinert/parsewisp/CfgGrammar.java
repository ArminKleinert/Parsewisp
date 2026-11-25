package de.kleinert.parsewisp;

import de.kleinert.parsewisp.grammar.Grammar;
import de.kleinert.parsewisp.grammar.GrammarBuilder;
import de.kleinert.parsewisp.parser_options.ParserCreationOptions;
import de.kleinert.parsewisp.parser_options.RedefinitionOption;
import de.kleinert.parsewisp.parsing.EOFTerm;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
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

        addProduction("rules", cat(List.of(
                cWsp, onceOrMore(cat(nt("rule"), cWsp)))));

        addProduction("rule", cat(List.of(
                alt(List.of(nt("nt"), nt("hide-nt"))),
                cWsp,
                altUnsafe(List.of(
                        stringCS(":="), stringCS("::="), stringCS("=/"),
                        stringCS("="), stringCS(":"))).hideInResult(),
                cWsp,
                nt("alt"),
                opt(alt(List.of(string(";"), string(".")))))));

        addProduction("alt", cat(List.of(
                nt("ord"),
                cWsp,
                zeroOrMore(cat(List.of(
                        hide(string("|")),
                        cWsp,
                        nt("ord")))))));

        addProduction("ord", cat(List.of(
                nt("cat"),
                cWsp,
                zeroOrMore(cat(List.of(
                        hide(string("/")),
                        cWsp,
                        nt("cat")))))));

        addProduction("cat", cat(List.of(
                nt("term"),
                zeroOrMore(cat(List.of(cWsp, nt("term")))))));

        addProduction("term", altUnsafe(List.of(
                nt("exclude"), nt("look"), nt("neg"))).mergeToTop());

        addProduction("exclude", cat(List.of(
                nt("factor"), cWsp, opt(cat(List.of(string("-").hideInResult(), cWsp, nt("factor")))))));

        addProduction("look", cat(List.of(string("&").hideInResult(), cWsp, nt("factor"))));
        addProduction("neg", cat(List.of(string("!").hideInResult(), cWsp, nt("factor"))));

        addProduction("factor", cat(List.of(
                nt("primary"),
                zeroOrMore(cat(List.of(
                        cWsp,
                        altUnsafe(List.of(
                                string("?"), string("*"), string("+"),
                                regex(Pattern.compile("\\{\\d*(,\\d*)?}")))))
                )))));

//        addProduction("opt_query", cat(List.of(nt("primary"), cWsp, string("?"))));
//        addProduction("star", cat(List.of(nt("primary"), cWsp, string("*"))));
//        addProduction("plus", cat(List.of(nt("primary"), cWsp, string("+"))));
//        addProduction("rep", cat(List.of(nt("primary"), cWsp, regex(Pattern.compile("\\{\\d*(,\\d*)?}")))));

        addProduction("primary", altUnsafe(List.of(
                nt("string"), nt("regexp"), nt("opt"),
                nt("opt_rep"), nt("paren"), nt("hide"),
                nt("epsilon"), nt("abnf-range"), //nt("eof"),
                nt("ntOrEof"))).mergeToTop());

        addProduction("string", altUnsafe(List.of(
                regex(Pattern.compile("(%[is])?\\\"[^\\\"\\\\]*(?:\\\\.[^\\\"\\\\]*)*\\\"")),
                regex(Pattern.compile("(%[is])?'[^'\\\\]*(?:\\\\.[^'\\\\]*)*'")))));

        addProduction("regexp", altUnsafe(List.of(
                regex(Pattern.compile("#'[^'\\\\]*(?:\\\\.[^'\\\\]*)*'")),
                regex(Pattern.compile("#\\\"[^\\\"\\\\]*(?:\\\\.[^\\\"\\\\]*)*\\\"")))));

        addProduction("opt", cat(List.of(
                string("[").hideInResult(), cWsp, nt("alt"), cWsp, string("]").hideInResult())));

        addProduction("opt_rep", cat(List.of(
                string("{").hideInResult(), cWsp, nt("alt"), cWsp, string("}").hideInResult())));

        addProduction("paren", cat(List.of(
                string("(").hideInResult(), cWsp, nt("alt"), cWsp, string(")").hideInResult())));

        addProduction("hide", cat(List.of(
                string("<").hideInResult(), cWsp, nt("alt"), cWsp, string(">").hideInResult())));

        var epsTerms = List.of("Epsilon", "epsilon", "EPSILON", "eps", "ε");
        addProduction("epsilon", specialSequence(
                "One of " + epsTerms,
                text -> {
                    var longest = "";
                    for (String epsTerm : epsTerms) {
                        if (text.startsWith(epsTerm) && longest.length() < epsTerm.length()) {
                            longest = epsTerm;
                        }
                    }
                    if (longest.isEmpty()) return Optional.empty();
                    return Optional.of(longest);
                }
        ));

        addProduction("abnf-range", regex(Pattern.compile(
                "%b[01]+(\\-[01]+)?|%d[0-9]+(\\-[0-9]+)?|%x[0-9a-fA-F]+(\\-[0-9a-fA-F]+)?")));

        addProduction("eof", string(EOFTerm.text()));

        final Pattern regex = Pattern.compile(
                "[^, \\r\\t\\n<>(){}\\[\\]+*?:=|'\"#&!;./%\\-0-9][^, \\r\\t\\n<>(){}\\[\\]+*?:=|'\"#&!;./%]*");
        addProduction("ntOrEof", specialSequence(
                "string which starts with a unicode-letter and continues with a sequence of unicode-letters or numbers",
                text -> {
                    final @NotNull Matcher matcher = regex.matcher(text);
                    if (!matcher.lookingAt()) {
                        return Optional.empty();
                    }
                    final String matched = matcher.group();
                    if (epsTerms.contains(matched)) {
                        return Optional.empty();
                    }
//                    if (EOFTerm.text().equals(matched)) {
//                        return Optional.empty();
//                    }
                    return Optional.of(matched);
                }));

        addProduction("nt", specialSequence(
                "string which starts with a unicode-letter and continues with a sequence of unicode-letters or numbers",
                text -> {
                    final @NotNull Matcher matcher = regex.matcher(text);
                    if (!matcher.lookingAt()) {
                        return Optional.empty();
                    }
                    final String matched = matcher.group();
                    if (epsTerms.contains(matched)) {
                        return Optional.empty();
                    }
                    if (EOFTerm.text().equals(matched)) {
                        return Optional.empty();
                    }
                    return Optional.of(matched);
                }));

        addProduction("hide-nt", cat(List.of(buffer(string("<").hideInResult()),
                cWsp,
                nt("nt"),
                cWsp,
                buffer(string(">").hideInResult()))));

        addProduction("cWsp", hide(onceOrMore(alt(
                nt("comment"),
                regex(Pattern.compile("[,\\s]*"))))));

        addProduction("comment", cat(List.of(
                string("(*"),
                onceOrMore(alt(
                        regex("(?s)(?:(?!\\(\\*|\\*\\)).)*(?x) # Comment text"),
                        nt("comment")
                )),
                string("*)"))));
    }

    /**
     * Returns the base grammar for Parsewisp.
     *
     * @param ignoredOptions The options.
     * @return The base grammar for Parsewisp.
     * @since 0.9.7
     */
    @NotNull
    public static Grammar makeCfg(final @NotNull ParserCreationOptions ignoredOptions) {
        return new CfgGrammar().build();
    }
}