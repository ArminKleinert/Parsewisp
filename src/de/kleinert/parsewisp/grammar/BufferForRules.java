package de.kleinert.parsewisp.grammar;

import de.kleinert.parsewisp.Sym;
import de.kleinert.parsewisp.parsing.NonTerminal;
import de.kleinert.parsewisp.parsing.RegexTerm;
import de.kleinert.parsewisp.parsing.Rule;
import de.kleinert.parsewisp.parsing.StringTerm;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * As long as this buffer exists, the following is true:
 * {@code if (Objects.equals(x, y)) { buffer.getOrAdd(x) == buffer.getOrAdd(y); }}
 */
final class BufferForRules {
    BufferForRules() {
    }

    private final @NotNull Map<@NotNull NonTerminal, @NotNull NonTerminal>
            nonTerminalMap = new HashMap<>();

    @NotNull NonTerminal getOrAdd(final @NotNull NonTerminal nonTerminal) {
        final var temp = nonTerminalMap.putIfAbsent(nonTerminal, nonTerminal);
        return temp == null ? nonTerminal : temp;
    }

    private final @NotNull Map<@NotNull RegexTerm, @NotNull RegexTerm>
            regexTermMap = new HashMap<>();

    @NotNull RegexTerm getOrAdd(final @NotNull RegexTerm rule) {
        final var temp = regexTermMap.putIfAbsent(rule, rule);
        return temp == null ? rule : temp;
    }

    private final @NotNull Map<@NotNull StringTerm, @NotNull StringTerm>
            stringTermMap = new HashMap<>();

    @NotNull StringTerm getOrAdd(final @NotNull StringTerm rule) {
        final var temp = stringTermMap.putIfAbsent(rule, rule);
        return temp == null ? rule : temp;
    }

    private final @NotNull Map<@NotNull Sym, @NotNull NonTerminal>
            symToNtSet = new HashMap<>();

    @NotNull NonTerminal getOrAddNt(final @NotNull Sym keyword) {
        return symToNtSet.computeIfAbsent(keyword, NonTerminal::create);
    }

    private final @NotNull Map<@NotNull String, @NotNull Rule>
            stringCsTerms = new HashMap<>();
    private final @NotNull Map<@NotNull String, @NotNull Rule>
            stringCiTerms = new HashMap<>();

    @NotNull Rule getOrAddString(final @NotNull String string, final boolean caseInsensitive) {
        if (caseInsensitive) {
            return stringCiTerms.computeIfAbsent(string, s -> StringTerm.create(s, true));
        } else {
            return stringCsTerms.computeIfAbsent(string, s -> StringTerm.create(s, false));
        }
    }

    private final @NotNull Map<@NotNull Pattern, @NotNull Rule>
            regexTermMap1 = new HashMap<>();

    @NotNull Rule getOrAddRegex(final @NotNull Pattern pattern) {
        return regexTermMap1.computeIfAbsent(pattern, RegexTerm::create);
    }
}
