package de.kleinert.parsewisp.other_formats;

import de.kleinert.parsewisp.Sym;
import de.kleinert.parsewisp.grammar.GrammarBuilder;
import de.kleinert.parsewisp.parser_options.ParserCreationOptions;
import de.kleinert.parsewisp.parsing.NonTerminal;
import de.kleinert.parsewisp.parsing.Rule;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.regex.Pattern;

public class AbnfBuilder extends GrammarBuilder {

    protected AbnfBuilder(@NotNull ParserCreationOptions options) {
        super(options);
    }

    NonTerminal CRLF = nt("CRLF");
    NonTerminal DQUOTE = nt("DQUOTE");
    NonTerminal VCHAR = nt("VCHAR");
    NonTerminal WSP = nt("WSP");
    NonTerminal alternation = nt("alternation");
    NonTerminal binVal = nt("bin-val");
    NonTerminal cNl = nt("c-nl");
    NonTerminal cWsp = nt("c-wsp");
    NonTerminal charVal = nt("char-val");
    NonTerminal regexp = nt("regexp");
    NonTerminal comment = nt("comment");
    NonTerminal concatenation = nt("concatenation");
    NonTerminal decVal = nt("dec-val");
    NonTerminal definedAs = nt("defined-as");
    NonTerminal element = nt("element");
    NonTerminal elements = nt("elements");
    NonTerminal group = nt("group");
    NonTerminal hide = nt("hide");
    NonTerminal hexVal = nt("hex-val");
    NonTerminal numVal = nt("num-val");
    NonTerminal option = nt("option");
    NonTerminal repeat = nt("repeat");
    NonTerminal repetition = nt("repetition");
    NonTerminal rule = nt("rule");
    NonTerminal rulelist = nt("rulelist");
    NonTerminal nonterm = nt("nonterm");
    NonTerminal hideNt = nt("hide-nt");

    Rule cWspRepeat = zeroOrMore(cWsp).enableHideTag();
    Rule newline = regex(Pattern.compile("\\r?\\n")).enableHideTag();

    @Override
    public void make() {
        // rulelist       =  1*( rule / (*WSP c-nl) )
        addProduction(
                rulelist.getKeyword(),
                concat(optional(cWspRepeat), rule,
                        onceOrMore(alternation(rule, cWspRepeat))));

        // rule           =  (nonterm / hide-nt) defined-as elements c-nl
        // defined-as     =  *c-wsp ("=" / "=/") *c-wsp
        // elements       =  alternation *WSP
        addProduction(
                rule.getKeyword(),
                concat(alternation(hideNt, nonterm), cWspRepeat, alternation("=", "=/"), cWspRepeat, alternation, zeroOrMore(WSP), optional(cNl)));

        // nonterm       =  ALPHA *(ALPHA / DIGIT / "-")
        addProduction(
                nonterm.getKeyword(),
                regex("[a-zA-Z][a-zA-Z0-9\\-]*"));
        addProduction(
                hideNt.getKeyword(),
                concat("<", regex("[a-zA-Z][a-zA-Z0-9\\-]*"), ">")        );

        // c-wsp          =  WSP / (c-nl WSP)
        addProduction(
                cWsp.getKeyword(),
                alternation(WSP, concat(cNl, WSP)));

        // c-nl           =  comment / CRLF ; comment or newline
        addProduction(
                cNl.getKeyword(),
                alternation(comment, newline));

        // comment        =  ";" *(WSP / VCHAR) CRLF
        addProduction(
                comment.getKeyword(),
                concat(";", zeroOrMore(alternation(WSP, unicodeChar(0x23, 0x7E))), newline));

        // alternation    =  concatenation *(*c-wsp "/" *c-wsp concatenation)
        addProduction(
                alternation.getKeyword(),
                concat(concatenation, zeroOrMore(concat(cWspRepeat, "/", cWspRepeat, concatenation))));

        // concatenation  =  repetition *(1*c-wsp repetition)
        addProduction(
                concatenation.getKeyword(),
                concat(repetition, zeroOrMore(concat(cWspRepeat, repetition))));

        // repetition     =  [repeat] element
        addProduction(
                repetition.getKeyword(),
                concat(optional(regex(Pattern.compile("[0-9]*(\\*[0-9]*)?"))),cWspRepeat, element));

        // element        =  nonterm / hide / group / option / char-val / num-val
        addProduction(
                element.getKeyword(),
                alternation(nonterm, hide, group, option, charVal, regexp, numVal));

        // group          =  "(" *c-wsp alternation *c-wsp ")"
        addProduction(
                group.getKeyword(),
                concat("(", cWspRepeat, alternation, cWspRepeat, ")"));

        // hide          =  "<" *c-wsp alternation *c-wsp ">"
        addProduction(
                Sym.sym("hide"),
                concat("<", cWspRepeat, alternation, cWspRepeat, ">"));

        // option         =  "[" *c-wsp alternation *c-wsp "]"
        addProduction(
                option.getKeyword(),
                concat("[", cWspRepeat, alternation, cWspRepeat, "]"));

        // char-val       =  [ "%i" / "%s" ] DQUOTE *(%x20-21 / %x23-7E) DQUOTE ; quoted string of SP and VCHAR without DQUOTE
        // char-val       =  #"(%[is])?\\\"[^\\\"\\\\]*(?:\\\\.[^\\\"\\\\]*)*\\\"";
        addProduction(
                charVal.getKeyword(),
                regex(Pattern.compile("(%[is])?\"[^\"\\\\]*(?:\\\\.[^\"\\\\]*)*\"")));

        // regexp         = #'[^'\\\\]*(?:\\\\.[^'\\\\]*)*' / #\\\"[^\\\"\\\\]*(?:\\\\.[^\\\"\\\\]*)*\\\"
        final @NotNull Rule rulesRule =
                alternationGuaranteeDistinctAndNotEmpty(
                        List.of(regex("#'[^'\\\\]*(?:\\\\.[^'\\\\]*)*'"),
                                regex("#\\\"[^\\\"\\\\]*(?:\\\\.[^\\\"\\\\]*)*\\\"")));
        addProduction(
                regexp.getKeyword(),
                rulesRule);

        // num-val        =  "%" (bin-val / dec-val / hex-val)
        addProduction(
                numVal.getKeyword(),
                concat("%", alternation(binVal, decVal, hexVal)));

        var bitRepeat = regex(Pattern.compile("[01]+"));
        // bin-val        =  "b" 1*BIT [ 1*("." 1*BIT) / ("-" 1*BIT) ] ; series of concatenated bit values or single ONEOF range
        // b [0-1]+
        // b [0-1]+ ( "." [0-1]+ )+
        // b [0-1]+ "-" [0-1]+
        addProduction(
                binVal.getKeyword(),
                concat("b", regex(Pattern.compile("[01]+([.01]*[01]|-[01]+)?"))));

        var digitRepeat = regex(Pattern.compile("[0-9]+"));
        // dec-val        =  "d" 1*DIGIT [ 1*("." 1*DIGIT) / ("-" 1*DIGIT) ]
        addProduction(
                decVal.getKeyword(),
                concat("d", regex(Pattern.compile("[0-9]+([.0-9]*[0-9]|-[0-9]+)?"))));

        var hexdigRepeat = regex(Pattern.compile("[0-9a-zA-Z]+"));
        // hex-val        =  "x" 1*HEXDIG [ 1*("." 1*HEXDIG) / ("-" 1*HEXDIG) ]
        addProduction(
                hexVal.getKeyword(),
                concat("x", regex(Pattern.compile("[a-zA-Z0-9]+([.a-zA-Z0-9]*[a-zA-Z0-9]|-[a-zA-Z0-9]+)?"))));

        addProduction(CRLF.getKeyword(), string("\r\n", false));
        addProduction(WSP.getKeyword(), regex(Pattern.compile("[\\u0020\\u0009]")));
    }
}
