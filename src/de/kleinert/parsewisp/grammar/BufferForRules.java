package de.kleinert.parsewisp.grammar;

import de.kleinert.parsewisp.Sym;
import de.kleinert.parsewisp.parsing.*;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * As long as this buffer exists, the following is true:
 * {@code if (Objects.equals(x, y)) { buffer.getOrAdd(x) == buffer.getOrAdd(y); }}
 */
final class BufferForRules {
    private final @NotNull Map<@NotNull NonTerminal, @NotNull NonTerminal>
            nonTerminalMap = new HashMap<>();

    /**
     * Adds a {@link NonTerminal}. If the object was already present in the buffer, returns the old one.
     * @param nonTerminal A rule.
     * @return The input or a previously buffered equivalent.
     */
    @NotNull NonTerminal getOrAdd(final @NotNull NonTerminal nonTerminal) {
        final var temp = nonTerminalMap.putIfAbsent(nonTerminal, nonTerminal);
        return temp == null ? nonTerminal : temp;
    }

    private final @NotNull Map<@NotNull RegexTerm, @NotNull RegexTerm>
            regexTermMap = new HashMap<>();

    /**
     * Adds a {@link RegexTerm}. If the object was already present in the buffer, returns the old one.
     * @param rule A rule.
     * @return The input or a previously buffered equivalent.
     */
    @NotNull RegexTerm getOrAdd(final @NotNull RegexTerm rule) {
        final var temp = regexTermMap.putIfAbsent(rule, rule);
        return temp == null ? rule : temp;
    }

    private final @NotNull Map<@NotNull StringTerm, @NotNull StringTerm>
            stringTermMap = new HashMap<>();

    /**
     * Adds a {@link StringTerm}. If the object was already present in the buffer, returns the old one.
     * @param rule A rule.
     * @return The input or a previously buffered equivalent.
     */
    @NotNull StringTerm getOrAdd(final @NotNull StringTerm rule) {
        final var temp = stringTermMap.putIfAbsent(rule, rule);
        return temp == null ? rule : temp;
    }

    private final @NotNull Map<@NotNull Sym, @NotNull NonTerminal>
            symToNtSet = new HashMap<>();

    /**
     * Adds a {@link NonTerminal} based on the keyword. If the object was already present in the buffer, returns the old one.
     * @param keyword The key for a {@link NonTerminal}.
     * @return The input or a previously buffered equivalent.
     * @see NonTerminal#create(Sym)
     */
    @NotNull NonTerminal getOrAddNt(final @NotNull Sym keyword) {
        return symToNtSet.computeIfAbsent(keyword, NonTerminal::create);
    }

    private final @NotNull Map<@NotNull String, @NotNull Rule>
            stringCsTerms = new HashMap<>();
    private final @NotNull Map<@NotNull String, @NotNull Rule>
            stringCiTerms = new HashMap<>();

    /**
     * Adds a {@link Rule} based on the string. If the object was already present in the buffer, returns the old one.
     * @param string A string.
     * @param caseInsensitive Whether the terminal will be case-insensitive.
     * @return A {@link StringTerm} or whatever {@link StringTerm#create(String, boolean)} would return for the same inputs.
     * @see StringTerm#create(String, boolean)
     */
    @NotNull Rule getOrAddString(final @NotNull String string, final boolean caseInsensitive) {
        if (caseInsensitive) {
            return stringCiTerms.computeIfAbsent(string, s -> StringTerm.create(s, true));
        } else {
            return stringCsTerms.computeIfAbsent(string, s -> StringTerm.create(s, false));
        }
    }

    private final @NotNull Map<@NotNull Pattern, @NotNull Rule>
            regexTermMap1 = new HashMap<>();

    /**
     * Adds a {@link Rule} based on the string. If the object was already present in the buffer, returns the old one.
     * @param pattern A pattern (regex).
     * @return A {@link RegexTerm} or whatever {@link RegexTerm#create(Pattern)} would return for the same inputs.
     * @see RegexTerm#create(Pattern)
     */
    @NotNull Rule getOrAddRegex(final @NotNull Pattern pattern) {
        return regexTermMap1.computeIfAbsent(pattern, RegexTerm::create);
    }
}
