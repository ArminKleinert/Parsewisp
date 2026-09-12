package de.kleinert.parsewisp.other_formats;

import de.kleinert.parsewisp.Parsewisp;
import de.kleinert.parsewisp.Sym;
import de.kleinert.parsewisp.error.ParserCreationFailure;
import de.kleinert.parsewisp.grammar.Grammar;
import de.kleinert.parsewisp.grammar.GrammarBuilder;
import de.kleinert.parsewisp.parser.Parser;
import de.kleinert.parsewisp.parser_options.ParserCreationOptions;
import de.kleinert.parsewisp.parser_options.RedefinitionOption;
import de.kleinert.parsewisp.parsing.*;
import de.kleinert.parsewisp.result.Node;
import de.kleinert.parsewisp.result.ParseTree;
import de.kleinert.parsewisp.util.StrParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Pattern;

/**
 * See <a href="https://www.rfc-editor.org/info/rfc5234/">https://www.rfc-editor.org/info/rfc5234/</a>
 * and <a href="https://www.rfc-editor.org/info/rfc7405/">https://www.rfc-editor.org/info/rfc7405/</a>
 */
public class ABNF {
    private ABNF() {
    }

    public static class ABNFOptions extends ParserCreationOptions {
            @Nullable Parser whitespaceParser;
            @Nullable Sym startProduction;
            boolean allowLookaheadAndNegations;

                    public ABNFOptions(            @Nullable Parser whitespaceParser,
                                                   @Nullable Sym startProduction,
                                                   boolean allowLookaheadAndNegations) {
                        super(whitespaceParser,startProduction,RedefinitionOption.CHOICE, true, List.of("="));
                this. allowLookaheadAndNegations= allowLookaheadAndNegations;
                    }
    }

    public static @NotNull Parser parser(@NotNull String grammar) {
        return parser(grammar, null);
    }

    public static @NotNull Parser parser(@NotNull String grammar, @Nullable ABNFOptions options) {
        if (options == null) {
            options = new ABNFOptions(null, null, false);
        }

        var abnfGrammarParser = Parsewisp.parser(baseGrammar(options), ParserCreationOptions.getDefault());
        var tree = abnfGrammarParser.parse(grammar);

        if (tree.isFailure()) {
            throw new ParserCreationFailure(tree.castToParseFailure().toString());
        }

        return Parsewisp.parser(new ABNF().transform(tree.castToParseSuccess(), options), null);
    }

    public static @NotNull Grammar baseGrammar(ABNFOptions options) {
        return new AbnfGrammarParserGrammarBuilder(ParserCreationOptions.getDefault(), options).build();
    }


    private @NotNull Grammar transform(final @NotNull ParseTree parsedAbnfGrammar, ABNFOptions abnfOptions) {
        return new Transformer(parsedAbnfGrammar,  abnfOptions).build();
    }

    private static class Transformer extends GrammarBuilder {
        private final @NotNull StrParser strParser = new StrParser();
        private final @NotNull ParseTree parsedAbnfGrammar;

        protected Transformer(final @NotNull ParseTree parsedAbnfGrammar,
                              final @NotNull ABNFOptions options) {
            super(options);
            this.parsedAbnfGrammar = parsedAbnfGrammar;
        }

        @Override
        protected void make() {
            for (@NotNull Node node : parsedAbnfGrammar.getContent()) {
                if (!Objects.equals(node.tree().getTag().content(), Sym.sym("rule"))) {
                    continue;
                }
                var prod = rule(node.tree());
                var lhs = prod.getKey().isHidden() ? prod.getValue().hideTag() : prod.getValue();
                addProduction(prod.getKey().getKeyword(), lhs);
            }

            {
                var CRLF = string("\r\n", false);
                var WSP = regex(Pattern.compile("[\\u0020\\u0009]"));
                addProduction(Sym.sym("ALPHA"), regex(Pattern.compile("[a-zA-Z]")));
                addProduction(Sym.sym("BIT"), regex(Pattern.compile("[01]")));
                addProduction(Sym.sym("CHAR"), regex(Pattern.compile("[\\u0001-\\u007F]")));
                addProduction(Sym.sym("CR"), string("\r", false));
                addProduction(Sym.sym("CRLF"), CRLF);
                addProduction(Sym.sym("CTL"), regex(Pattern.compile("[\\u0000-\\u001F|\\u007F]")));
                addProduction(Sym.sym("DIGIT"), regex(Pattern.compile("[0-9]")));
                addProduction(Sym.sym("DQUOTE"), string("\"", false));
                addProduction(Sym.sym("HEXDIG"), regex(Pattern.compile("[0-9a-fA-F]")));
                addProduction(Sym.sym("HTAB"), regex(Pattern.compile("\t")));
                addProduction(Sym.sym("LF"), regex(Pattern.compile("\n")));
                addProduction(Sym.sym("LWSP"), zeroOrMore(alt(WSP, cat(CRLF, WSP))));
                addProduction(Sym.sym("OCTET"), regex(Pattern.compile("[\\u0000-\\u00FF]")));
                addProduction(Sym.sym("SP"), string(" ", false));
                addProduction(Sym.sym("VCHAR"), regex(Pattern.compile("[\\u0021-\\u007E]")));
                addProduction(Sym.sym("WSP"), WSP);
            }
        }

        // Tree has format
        //    [:rule, [:nonterm, "A"], "=", [:alternation, ...]]
        //    [:rule, [:hide-nt, "<", "A", ">"], "=", [:alternation, ...]]
        private @NotNull Map.Entry<NonTerminal, Rule> rule(
                final @NotNull ParseTree tree) {
            var name = ntOrHideNt(tree.getNode(0).tree());
            var rhs = makeAlternation(tree.getNode(2).tree());
            return Map.entry(name, rhs);
        }

        private @NotNull NonTerminal ntOrHideNt(final @NotNull ParseTree tree) {
            if (Objects.equals(Sym.sym("hide-nt"), tree.getTag().content())) {
                return (NonTerminal) nt(Sym.sym(tree.getNode(1).string())).enableHideTag();
            }
            return nt(Sym.sym(tree.getNode(0).string()));
        }

        // Format: [:alternation, [:concatenation, ...], ...]
        private @NotNull Rule makeAlternation(final @NotNull ParseTree tree) {
            return altList(
                    tree.getContent().stream()
                            .filter(it -> it.content() instanceof ParseTree)
                            .map(Node::tree)
                            .map(this::concatenation)
                            .toList());
        }

        // Format: [:concatenation, [:repetition, ...], [:repetition, ...]]
        private @NotNull Rule concatenation(final @NotNull ParseTree tree) {
            return cat(
                    tree.getContent().stream()
                            .filter(it -> it.content() instanceof ParseTree)
                            .map(Node::tree)
                            .map(this::repetition)
                            .toList());
        }

        // [:repetition, [:element, ...]]
        // [:repetition, "*", [:element, ...]]
        // [:repetition, "...*", [:element, ...]]
        // [:repetition, "*...", [:element, ...]]
        // [:repetition, "...*...", [:element, ...]]
        private @NotNull Rule repetition(final @NotNull ParseTree tree) {
            if (tree.size() == 2) {
                return element(tree.getNode(0).tree());
            }
            var rule = element(tree.getNode(1).tree());
            var s = tree.getNode(0).string();

            if (s.isEmpty()) {
                return rule;
            }

            var parts = s.split("\\*");
            final int min, max;

            if (parts.length == 1) {
                // Format at this point is [0-9]+\\* or \\*[0-9]+ or [0-9]+

                if (s.charAt(0) == '*') {
                    // Only maximum given
                    min = 0;
                    max = Integer.parseInt(parts[0]);
                } else if (s.charAt(s.length() - 1) == '*') {
                    // Only minimum given
                    min = Integer.parseInt(parts[0]);
                    max = Integer.MAX_VALUE;
                } else {
                    // Exact number given
                    min = Integer.parseInt(parts[0]);
                    max = min;
                }
            } else if (parts.length == 0) {
                // No minimum, no maximum
                return zeroOrMore(rule);
            } else {
                min = parts[0].isEmpty() ? 0 : Integer.parseInt(parts[0]);
                max = Integer.parseInt(parts[1]);
            }

            return rep(rule, min, max);
        }

        private @NotNull ParseTree findAlternationTreeInNodes(final @NotNull ParseTree pt) {
            var content = pt.getContent();
            for (var sub : content) {
                if (sub.content() instanceof ParseTree
                        && ((ParseTree) sub.content()).getTag().content().equals(Sym.sym("alternation"))) {
                    return sub.tree();
                }
            }
            throw new IllegalStateException();
        }

        // element        =  nonterm / hide / group / option / char-val / num-val
        private @NotNull Rule element(final @NotNull ParseTree tree) {
            var inner = tree.getNode(0).tree();
            var innerTag = inner.getTag().content();
            if (innerTag.equals(Sym.sym("nonterm"))) {
                // nonterm        = #"[a-zA-Z][a-zA-Z0-9\\-]*"
                return nt(inner.getNode(0).string());
            } else if (innerTag.equals(Sym.sym("hide"))) {
                // hide           =  "<" *c-wsp alternation *c-wsp ">"
                return makeAlternation(findAlternationTreeInNodes(inner)).enableHideTag();
            } else if (innerTag.equals(Sym.sym("group"))) {
                // group          =  "(" *c-wsp alternation *c-wsp ")"
                return makeAlternation(findAlternationTreeInNodes(inner));
            } else if (innerTag.equals(Sym.sym("option"))) {
                // option         =  "[" *c-wsp alternation *c-wsp "]"
                return opt(makeAlternation(findAlternationTreeInNodes(inner)));
            } else if (innerTag.equals(Sym.sym("char-val"))) {
                // char-val       =  DQUOTE *(%x20-21 / %x23-7E) DQUOTE ; quoted string of SP and VCHAR without DQUOTE
                return makeCharVal(inner);
            } else if (innerTag.equals(Sym.sym("regexp"))) {
                // regexp         = #'[^'\\\\]*(?:\\\\.[^'\\\\]*)*' / #\\\"[^\\\"\\\\]*(?:\\\\.[^\\\"\\\\]*)*\\\"
                return makeRegexp(inner);
            } else if (innerTag.equals(Sym.sym("num-val"))) {
                // num-val        =  "%" (bin-val / dec-val / hex-val)
                return makeNumVal(inner);
            } else if (innerTag.equals(Sym.sym("look"))) {
                // look = <'&' opt-whitespace> element;
                return look(element(inner.getNode(1).tree()));
            } else if (innerTag.equals(Sym.sym("neg"))) {
                // neg = <'!' opt-whitespace> element;
                return neg(element(inner.getNode(1).tree()));
            }
            throw new IllegalStateException();
        }

        // Actual implementation: char-val       =  #"(%[is])?\\\"[^\\\"\\\\]*(?:\\\\.[^\\\"\\\\]*)*\\\"";
        private @NotNull Rule makeCharVal(final @NotNull ParseTree tree) {
            var string = tree.getNode(0).string();
            if (string.startsWith("%")) {
                return string(
                        strParser.processString(string.substring(2)), string.charAt(1) == 'i');
            }
            return string(strParser.processString(string), true);

        }

        // regexp         = #'[^'\\\\]*(?:\\\\.[^'\\\\]*)*' / #\\\"[^\\\"\\\\]*(?:\\\\.[^\\\"\\\\]*)*\\\"
        private @NotNull Rule makeRegexp(final @NotNull ParseTree tree) {
            return regex(
                    strParser.processRegexp((String)
                            tree.getContent().get(0).content()));
        }

        // num-val        =  "%" (bin-val / dec-val / hex-val)
        // bin-val        =  "b" 1*BIT [ 1*("." 1*BIT) / ("-" 1*BIT) ] ; series of concatenated bit values or single ONEOF range
        // dec-val        =  "d" 1*DIGIT [ 1*("." 1*DIGIT) / ("-" 1*DIGIT) ]
        // hex-val        =  "x" 1*HEXDIG [ 1*("." 1*HEXDIG) / ("-" 1*HEXDIG) ]
        private @NotNull Rule makeNumVal(final @NotNull ParseTree tree) {
            var sub = tree.getNode(1).tree();
            var prefix = sub.getNode(0).string();

            final int radix = switch (prefix) {
                case "b" -> 2;
                case "d" -> 10;
                case "x" -> 16;
                default -> throw new IllegalStateException();
            };

            var digitStr = sub.getNode(1).string();
            var minusIndex = digitStr.indexOf('-');
            if (minusIndex < 0) {
                var sb = new StringBuilder();
                for (String part : digitStr.split("\\.")) {
                    sb.appendCodePoint(Integer.parseInt(part, radix));
                }
                return string(sb.toString());
            }

            var parts = digitStr.split("-");
            var min = Integer.parseInt(parts[0], radix);
            var max = Integer.parseInt(parts[1], radix);
            return numVal(min, max);
        }
    }

    private static class AbnfGrammarParserGrammarBuilder extends GrammarBuilder {
        private final ABNFOptions abnfOptions;

        protected AbnfGrammarParserGrammarBuilder(final @NotNull ParserCreationOptions options, ABNFOptions abnfOptions) {
            super(options);
            this.abnfOptions = abnfOptions;
        }

        NonTerminal alternation = nt("alternation");
        NonTerminal binVal = nt("bin-val");
        NonTerminal cNl = nt("c-nl");
        NonTerminal cWsp = nt("c-wsp");
        NonTerminal charVal = nt("char-val");
        NonTerminal comment = nt("comment");
        NonTerminal concatenation = nt("concatenation");
        NonTerminal decVal = nt("dec-val");
        NonTerminal element = nt("element");
        NonTerminal group = nt("group");
        NonTerminal hexVal = nt("hex-val");
        NonTerminal hide = nt("hide");
        NonTerminal hideNt = nt("hide-nt");
        NonTerminal nonterm = nt("nonterm");
        NonTerminal numVal = nt("num-val");
        NonTerminal option = nt("option");
        NonTerminal regexp = nt("regexp");
        NonTerminal repetition = nt("repetition");
        NonTerminal rule = nt("rule");
        NonTerminal rulelist = nt("rulelist");
        NonTerminal WSP = nt("WSP");

        Rule cWspRepeat = zeroOrMore(cWsp).enableHideTag();
        Rule newline = regex(Pattern.compile("\\r?\\n"));

        @Override
        public void make() {
            // rulelist       =  1*( rule / (*WSP c-nl) )
            addProduction(
                    rulelist.getKeyword(),
                    cat(zeroOrMore(cWspRepeat), rule,
                            onceOrMore(alt(rule, cWspRepeat))));

            // rule           =  (nonterm / hide-nt) defined-as elements c-nl
            // defined-as     =  *c-wsp ("=" / "=/") *c-wsp
            // elements       =  alternation *WSP
            addProduction(
                    rule.getKeyword(),
                    cat(alt(hideNt, nonterm),
                            cWspRepeat,
                            alt("=", "=/"),
                            cWspRepeat,
                            alternation,
                            zeroOrMore(WSP),
                            opt(cNl)));

            // nonterm       =  ALPHA *(ALPHA / DIGIT / "-")
            addProduction(
                    nonterm.getKeyword(),
                    regex("[a-zA-Z][a-zA-Z0-9\\-]*(?x) # NonTerminal"));
            addProduction(
                    hideNt.getKeyword(),
                    cat("<", regex("[a-zA-Z][a-zA-Z0-9\\-]*(?x) # Nonterminal"), ">"));

            // c-wsp          =  WSP / (c-nl WSP)
            addProduction(
                    cWsp.getKeyword(),
                    alt(regex(Pattern.compile("\\s+")), cat(cNl)));

            // c-nl           =  comment / CRLF ; comment or newline
            addProduction(
                    cNl.getKeyword(),
                    alt(comment, newline));

            // comment        =  ";" *(WSP / VCHAR) CRLF
            addProduction(
                    comment.getKeyword(),
                    cat(";", zeroOrMore(alt(WSP, regex(Pattern.compile("^\\S+(?x) # Comment until newline/eof")))), alt(newline, eof())));

            // alternation    =  concatenation *(*c-wsp "/" *c-wsp concatenation)
            addProduction(
                    alternation.getKeyword(),
                    cat(concatenation, zeroOrMore(cat(cWspRepeat, "/", cWspRepeat, concatenation))));

            // concatenation  =  repetition *(1*c-wsp repetition)
            addProduction(
                    concatenation.getKeyword(),
                    cat(repetition, zeroOrMore(cat(cWspRepeat, repetition))));

            // repetition     =  [repeat] element
            addProduction(
                    repetition.getKeyword(),
                    cat(opt(regex(Pattern.compile("[0-9]*(\\*[0-9]*)?"))), cWspRepeat, element));

            // element        =  nonterm / hide / group / option / char-val / num-val
            var elementAlternatives = new ArrayList<Rule>(List.of(
                    nonterm, hide, group, option, charVal, regexp, numVal));
            if (abnfOptions.allowLookaheadAndNegations) {
                elementAlternatives.addAll(List.of(nt("look"), nt("neg")));
            }
            addProduction(
                    element.getKeyword(),
                    altList(elementAlternatives));

            // group          =  "(" *c-wsp alternation *c-wsp ")"
            addProduction(
                    group.getKeyword(),
                    cat("(", cWspRepeat, alternation, cWspRepeat, ")"));

            // hide          =  "<" *c-wsp alternation *c-wsp ">"
            addProduction(
                    hide.getKeyword(),
                    cat("<", cWspRepeat, alternation, cWspRepeat, ">"));

            // option         =  "[" *c-wsp alternation *c-wsp "]"
            addProduction(
                    option.getKeyword(),
                    cat("[", cWspRepeat, alternation, cWspRepeat, "]"));

            // look = <'&' opt-whitespace> element;
            addProduction(
                    Sym.sym("look"),
                    cat("&", cWspRepeat, element));

            // neg = <'!' opt-whitespace> element;
            addProduction(
                    Sym.sym("neg"),
                    cat("!", cWspRepeat, element));

            // char-val       =  [ "%i" / "%s" ] DQUOTE *(%x20-21 / %x23-7E) DQUOTE ; quoted string of SP and VCHAR without DQUOTE
            // char-val       =  #"(%[is])?\\\"[^\\\"\\\\]*(?:\\\\.[^\\\"\\\\]*)*\\\"";
            addProduction(
                    charVal.getKeyword(),
                    regex(Pattern.compile("(%[is])?\"[^\"\\\\]*(?:\\\\.[^\"\\\\]*)*\"(?x) # String")));

            // regexp         = #'[^'\\\\]*(?:\\\\.[^'\\\\]*)*' / #\\\"[^\\\"\\\\]*(?:\\\\.[^\\\"\\\\]*)*\\\"
            final @NotNull Rule rulesRule =
                    alternationGuaranteeDistinctAndNotEmpty(
                            List.of(regex("#'[^'\\\\]*(?:\\\\.[^'\\\\]*)*'(?x) # Regex"),
                                    regex("#\\\"[^\\\"\\\\]*(?:\\\\.[^\\\"\\\\]*)*\\\"(?x) # Regex")));
            addProduction(
                    regexp.getKeyword(),
                    rulesRule);

            // num-val        =  "%" (bin-val / dec-val / hex-val)
            addProduction(
                    numVal.getKeyword(),
                    cat("%", alt(binVal, decVal, hexVal)));

            // bin-val        =  "b" 1*BIT [ 1*("." 1*BIT) / ("-" 1*BIT) ] ; series of concatenated bit values or single ONEOF range
            // b [0-1]+
            // b [0-1]+ ( "." [0-1]+ )+
            // b [0-1]+ "-" [0-1]+
            addProduction(
                    binVal.getKeyword(),
                    cat("b", regex(Pattern.compile("[01]+([.01]*[01]|-[01]+)?(?x) # Binary num-val"))));

            // dec-val        =  "d" 1*DIGIT [ 1*("." 1*DIGIT) / ("-" 1*DIGIT) ]
            addProduction(
                    decVal.getKeyword(),
                    cat("d", regex(Pattern.compile("[0-9]+([.0-9]*[0-9]|-[0-9]+)?(?x) # Decimal num-val"))));

            // hex-val        =  "x" 1*HEXDIG [ 1*("." 1*HEXDIG) / ("-" 1*HEXDIG) ]
            addProduction(
                    hexVal.getKeyword(),
                    cat("x", regex(Pattern.compile("[a-zA-Z0-9]+([.a-zA-Z0-9]*[a-zA-Z0-9]|-[a-zA-Z0-9]+)?(?x) # Hexadecimal num-val"))));

            addProduction(WSP.getKeyword(), regex(Pattern.compile("[\\u0020\\u0009](?x) # Whitespace")));
        }
    }
}
