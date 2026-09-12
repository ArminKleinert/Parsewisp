package de.kleinert.parsewisp;

import de.kleinert.parsewisp.error.ParserCreationFailure;
import de.kleinert.parsewisp.grammar.*;
import de.kleinert.parsewisp.parser_options.*;
import de.kleinert.parsewisp.parsing.*;
import de.kleinert.parsewisp.result.*;
import de.kleinert.parsewisp.util.StrParser;
import org.jetbrains.annotations.NotNull;

import java.util.*;

final class Cfg {
    private final @NotNull ParserCreationOptions options;

    private Cfg(final @NotNull ParserCreationOptions options) {
        this.options = options;
    }

    static @NotNull Cfg make(final @NotNull ParserCreationOptions options) {
        return new Cfg(options);
    }

    private static class GrammarBuild extends GrammarBuilder {
        String spec;
        Grammar grammarGrammar;

        GrammarBuild(final @NotNull ParserCreationOptions options,
                     final @NotNull String spec,
                     final @NotNull Grammar grammarGrammar) {
            super(options);
            this.spec = spec;
            this.grammarGrammar = grammarGrammar;
        }

        private @NotNull Rule buildRepRule(final @NotNull ParseTree tree) {
            final @NotNull var partsUncut = (String) tree.getContent().get(0).content();
            @NotNull var parts = partsUncut.split("\\*");
            if (parts.length == 1) {
                // Format at this point is [0-9]+\\* or \\*[0-9]+ or [0-9]+
                final @NotNull var temp = new String[]{"", ""};
                if (partsUncut.charAt(0) == '*') { // Only maximum provided (e.g. `*n p`)
                    temp[1] = parts[0];
                } else if (partsUncut.charAt(partsUncut.length() - 1) == '*') {// Only minimum provided (e.g. `n* p`
                    temp[0] = parts[0];
                } else { // Only an exact number is given: Both minimum and maximum. (e.g. `n p`).
                    temp[0] = parts[0];
                    temp[1] = temp[0];
                }
                parts = temp;
            } else if (parts.length == 0) { // The input was only "*"
                parts = new String[]{"", ""};
            } else if (parts.length > 2) { // The input included more than one "*"
                throw new IllegalArgumentException("Invalid format for repetition rule: " + partsUncut);
            }
            // If none of the cases were true, the input had the usual format [0-9]+\\*[0-9]+
            final int min = parts[0].isBlank() ? 0 : Integer.parseInt(parts[0]);
            final int max = parts[1].isBlank() ? Integer.MAX_VALUE : Integer.parseInt(parts[1]);
            final @NotNull var repeatedRule =
                    (Rule) buildRule((ParseTree) tree.getContent().get(1).content());
            return rep(repeatedRule, min, max);
        }

        private @NotNull Map.Entry<@NotNull Sym, @NotNull Rule> buildRuleRule(
                final @NotNull ParseTree tree) {
            final @NotNull var allContents = tree.getContent();
            final @NotNull var nt = (ParseTree) allContents.get(0).content();
            final @NotNull var altOrOrd = (ParseTree) allContents.get(1).content();
            @NotNull var content = nt.getContent().get(0);

            final @NotNull Sym key;
            final @NotNull Rule rule;

            if (Objects.equals(Sym.sym("hide-nt"), nt.getTag().content())) {
                content = ((ParseTree) content.content()).getContent().get(0);
                key = Sym.sym(content.content().toString());
                rule = ((Rule) buildRule(altOrOrd)).hideTag();
            } else {
                key = Sym.sym((String) content.content());
                rule = (Rule) buildRule(altOrOrd);
            }

            return Map.entry(key, rule);
        }

        private final @NotNull StrParser strParser = new StrParser();

        private @NotNull Object buildRule(final @NotNull ParseTree tree1) {
            @NotNull ParseTree tree = tree1;
            for (; ; ) {
                if (tree.getTag().equals(ParseTree.NULL_TAG)) {
                    tree = (ParseTree) tree.getContent().get(0).content();
                    continue;
                }

                final @NotNull var tag = tree.getTag().content().name();
                switch (tag) {
                    case "rule" -> {
                        return buildRuleRule(tree);
                    }
                    case "nt" -> {
                        var name = (String) tree.getContent().get(0).content();
                        return nt(Sym.sym(name));
                    }
                    case "paren" -> {
                        // The parse tree is wrapped in hidden "(" ")".
                        tree = (ParseTree) tree.getContent().get(0).content();
                        continue; // Open up the grouping and take it to the top.
                    }
                    case "alt" -> {
                        return altList(tree
                                .getContent()
                                .stream()
                                .map((c) -> (Rule) buildRule((ParseTree) c.content()))
                                .toList());
                    }
                    case "ord" -> {
                        return ordAlt(tree
                                .getContent()
                                .stream()
                                .map((c) -> buildRule((ParseTree) c.content()))
                                .toList());
                    }
                    case "hide" -> {
                        return ((Rule) buildRule(
                                ((Node.NodeParseTree) tree.getContent().get(0)).content())).enableHideTag();
                    }
                    case "cat" -> {
                        return cat(tree
                                .getContent()
                                .stream()
                                .map((c) -> (Rule) buildRule((ParseTree) c.content()))
                                .toList());
                    }
                    case "string" -> {
                        String s = (String) tree.getContent().get(0).content();
                        if (s.startsWith("%")) {
                            boolean caseInsensitive = switch (s.charAt(1)) {
                                case 'i' -> true;
                                case 's' -> false;
                                default -> throw new IllegalStateException();
                            };
                            return string(
                                    strParser.processString(s.substring(2)),
                                    caseInsensitive);
                        }
                        return string(strParser.processString(s));
                    }
                    case "string-cs" -> {
                        return stringCS(
                                strParser.processString((String) tree.getContent().get(0).content()));
                    }
                    case "string-ci" -> {
                        return stringCI(
                                strParser.processString((String)
                                        tree.getContent().get(0).content()));
                    }
                    case "regexp" -> {
                        return regex(
                                strParser.processRegexp((String)
                                        tree.getContent().get(0).content()));
                    }
                    case "neg" -> {
                        return neg(buildRule(
                                (ParseTree) tree.getContent().get(0).content()));
                    }
                    case "opt", "opt_query" -> {
                        return opt((Rule) buildRule(
                                (ParseTree) tree.getContent().get(0).content()));
                    }
                    case "star", "opt_rep" -> {
                        return zeroOrMore((Rule) buildRule(
                                (ParseTree) tree.getContent().get(0).content()));
                    }
                    case "plus" -> {
                        return onceOrMore((Rule) buildRule(
                                (ParseTree) tree.getContent().get(0).content()));
                    }
                    case "look" -> {
                        return look(buildRule(
                                (ParseTree) tree.getContent().get(0).content()));
                    }
                    case "rep" -> {
                        try {
                            return buildRepRule(tree);
                        } catch (IllegalArgumentException exception) {
                            throw new ParserCreationFailure(exception);
                        }
                    }
                    case "abnf-range" -> {
                        var content = tree.getContent();
                        var parts = ((String) content.get(0).content()).split("-");
                        var prefix = parts[0]; // "%b..."/"%d..."/"%x..."
                        final int radix = switch (prefix.charAt(1)) {
                            case 'b' -> 2;
                            case 'd' -> 10;
                            case 'x' -> 16;
                            default -> throw new ParserCreationFailure("Invalid format for value range.");
                        };
                        var rangeFirst = Integer.parseInt(
                                parts[0].substring(2),
                                radix);
                        var rangeLast = parts.length == 1
                                ? rangeFirst
                                : Integer.parseInt(parts[1], radix);
                        return numVal(rangeFirst, rangeLast);
                    }
                    case "epsilon" -> {
                        return EpsilonTerm.getDefault();
                    }
                    case "exclude" -> {
                        var rule1 = (Rule) buildRule((ParseTree)
                                tree.getContent().get(0).content());
                        var rule2 = (Rule) buildRule((ParseTree)
                                tree.getContent().get(1).content());
                        return exclude(rule1, rule2);
                    }
                    case "eof" -> {
                        return EOFTerm.getDefault();
                    }
                }
                throw new UnsupportedOperationException(tag);
            }
        }

        @Override
        protected void make() {
            final @NotNull ParseResult rules = Gll.parse(
                    grammarGrammar,
                    Sym.sym("rules"),
                    spec, false, false);

            if (rules instanceof ParseFailure) {
                throw new ParserCreationFailure(
                        "Error parsing grammar specification:\n" + rules + "\n");
            }

            for (final Node rule : rules.castToParseSuccess().getContent()) {
                var sc = buildRuleRule((ParseTree) rule.content());
                addProduction(sc.getKey(), sc.getValue());
            }
        }
    }

    @NotNull Grammar buildGrammar(final @NotNull String spec,
                                  final @NotNull Grammar grammarGrammar) {
        return new GrammarBuild(options, spec, grammarGrammar).build();
    }
}