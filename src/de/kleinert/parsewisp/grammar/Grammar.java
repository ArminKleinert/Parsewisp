package de.kleinert.parsewisp.grammar;

import de.kleinert.parsewisp.Sym;
import de.kleinert.parsewisp.parsing.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * A type representing a Grammar.
 */
public final class Grammar extends LinkedHashMap<@NotNull Sym, Rule> {
    /**
     * Staring symbol of this grammar. Can be overridden when the Grammar is used in a parser.
     */
    private final @NotNull Sym startSym;

    /**
     * Create a new instance.
     *
     * @param startSym Starting production key.
     * @param m        Map of productions.
     */
    public Grammar(final @NotNull Sym startSym, final @NotNull LinkedHashMap<? extends Sym, ? extends Rule> m) {
        super(m);

        if (m.isEmpty())
            throw new IllegalArgumentException("Empty grammar.");

        this.startSym = startSym;
    }

    /**
     * Return the starting production. This might be null.
     *
     * @return The starting production key.
     */
    public @NotNull Sym getStartSym() {
        return startSym;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Grammar g))
            return false;

        if (g.size() != size())
            return false;

        for (Map.Entry<@NotNull Sym, Rule> symRuleEntry : entrySet()) {
            if (!Objects.equals(
                    g.getProduction(symRuleEntry.getKey()),
                    symRuleEntry.getValue())) {
                return false;
            }
        }

        return true;
    }

    /**
     * Gets the right-hand side of the production associated with the key.
     *
     * @param key The right-hand side of the production.
     * @return The production or null if not found.
     */
    public @Nullable Rule getProduction(final @NotNull Sym key) {
        return getOrDefault(key, null);
    }

    @Override
    public Rule get(Object key) {
        throw new UnsupportedOperationException("Please use getProduction(key) instead of get(key).");
    }

    @Override
    public String toString() {
        return super.toString();
    }

    /**
     * Returns some information about the Grammar.
     *
     * @return An instance of {@link GrammarInfo}.
     * @see GrammarInfo
     */
    public @NotNull GrammarInfo analyze() {
        return new GrammarInfo(this);
    }

    /**
     * Holds some information about a grammar.
     *
     * @param grammar The grammar.
     */
    public record GrammarInfo(@NotNull Grammar grammar) {
        /**
         * All non-terminals of the Grammar.
         *
         * @return A collection of all non-terminals of the Grammar.
         */

        public @NotNull Collection<@NotNull Sym> definedNTs() {
            return grammar.keySet();
        }

        /**
         * All non-terminals which appear on the right-hand side of any production.
         *
         * @return A collection of all non-terminals which appear on the right-hand side of any production.
         */
        public Set<@NotNull Sym> usedNTs() {
            return collect(it -> it instanceof NonTerminal).stream()
                    .map(it -> ((NonTerminal) it).getKeyword())
                    .collect(Collectors.toSet());
        }

        /**
         * Returns a collection of all rules that match the predicate.
         *
         * <pre>
         * {@code
         *     var parser = Parsewisp.parser("""
         *                     S = A*
         *                     A = "a" | "b"
         *                     """);
         *     var grammarInfo = parser.grammar().analyze();
         *     System.out.println(grammarInfo.collect(rule -> rule instanceof Terminal));       // [a, b]
         *     System.out.println(grammarInfo.collect(rule -> rule instanceof ZeroOrMoreRule)); // [A*]
         * }
         * </pre>
         *
         * @param collector The predicate.
         * @return A collection of rules that match the predicate.
         */
        public @NotNull Set<@NotNull Rule> collect(final @NotNull Predicate<@NotNull Rule> collector) {
            final @NotNull Set<Rule> result = new HashSet<>();
            final @NotNull Set<Rule> analyzedRules = Collections.newSetFromMap(new IdentityHashMap<>());
            final @NotNull ArrayList<@NotNull Rule> ruleStack = new ArrayList<>(grammar.values());
            @NotNull Rule rule;

            while (!ruleStack.isEmpty()) {
                rule = ruleStack.remove(ruleStack.size() - 1);
                if (analyzedRules.contains(rule)) {
                    continue;
                }
                analyzedRules.add(rule);

                if (rule instanceof RuleWithManyChildren) {
                    ruleStack.addAll(((RuleWithManyChildren) rule).getRules());
                } else if (rule instanceof RuleWithChild) {
                    ruleStack.add(((RuleWithChild) rule).getRule());
                }

                if (collector.test(rule)) {
                    result.add(rule);
                }
            }

            return result;
        }

        /**
         * Returns all NTs that are defined but unused. Effectively, this is a list of unused productions.
         * <p>
         * Note that the implementation makes no guarantees about the type, except that it's a {@link List} or {@link Set}.
         * However, each key appears only once in the collection.
         * </p>
         * <p>
         * Example:
         * S : 'a' 'b'
         * A : 'c'
         * The key "A" is defined, but never used.
         * </p>
         *
         * @return A collection of unused non-terminals.
         */
        public @NotNull Collection<Sym> getUnusedNTs() {
            var usedNTs = usedNTs();
            return definedNTs().stream()
                    .filter(it -> !usedNTs.contains(it))
                    .filter(it -> !Objects.equals(it, grammar.getStartSym()))
                    .collect(Collectors.toSet());
        }

        /**
         * Returns a Collection of keys that are used, but not defined. For any valid grammar, the returned collection is empty.
         * <p>
         * Example:
         * S : A 'b'
         * The key "A" is used, but never defined.
         * </p>
         *
         * @return A collection which is hopefully empty.
         */
        public @NotNull Collection<Sym> getUndefinedUsedNTs() {
            var definedNTs = definedNTs();
            return usedNTs().stream().filter(it -> !definedNTs.contains(it)).collect(Collectors.toSet());
        }

        /**
         * Checks whether the Grammar is valid.
         *
         * @return true or false
         * @see GrammarInfo#getUndefinedUsedNTs()
         */
        public boolean isValid() {
            return getUndefinedUsedNTs().isEmpty()
                    && !definedTerminals().isEmpty();
        }

        /**
         * Collects all terminals and equivalent rules. If a rule occurs multiple times in the grammar, it still appears only once in the resulting collection.
         *
         * @return A collection of all terminals and special sequences (functions).
         */
        public @NotNull Set<@NotNull Rule> definedTerminals() {
            return collect(it -> it instanceof Terminal || it instanceof SpecialSequenceRule);
        }

        /**
         * Returns a Set of rules that are reachable from the input production name.
         *
         * @param start The production from which to analyze.
         * @return A Set of reachable rules.
         */
        public @NotNull Grammar subGrammar(final @NotNull Sym start) {
            var startRule = grammar.getProduction(start);

            if (startRule == null)
                throw new IllegalArgumentException("Symbol not in grammar: " + start);

            var visited = new HashSet<@NotNull Rule>();
            var stack = new ArrayList<@NotNull Rule>();
            stack.add(startRule);

            var reachable = new LinkedHashSet<Sym>();
            reachable.add(start);

            while (!stack.isEmpty()) {
                var rule = stack.remove(stack.size() - 1);

                // If already in set -> Ignore
                if (!visited.add(rule))
                    continue;

                if (rule instanceof RuleWithManyChildren) {
                    stack.addAll(((RuleWithManyChildren) rule).getRules());
                } else if (rule instanceof RuleWithChild) {
                    stack.add(((RuleWithChild) rule).getRule());
                } else if (rule instanceof NonTerminal) {
                    var key = ((NonTerminal) rule).getKeyword();
                    var prod = grammar.getProduction(key);
                    if (prod == null)
                        throw new IllegalArgumentException("Symbol not in grammar: " + start);
                    stack.add(Objects.requireNonNull(prod));
                    reachable.add(key);
                }
            }

            var subGrammar = new LinkedHashMap<Sym, Rule>(reachable.size());
            for (Sym sym : reachable) {
                subGrammar.put(sym, grammar.getProduction(sym));
            }

            return new Grammar(start, subGrammar);
        }

        /**
         * Return true if the grammar can terminate for the given starting symbol.
         * <p>
         * Sometimes, a grammar can be non-productive, meaning that it would technically not produce an output for any input. For example, the following:
         * <pre>
         * {@code
         *         S = A S
         *         A = epsilon
         * }
         * </pre>
         * With a naive algorithm, the parser could never terminate. You can check for this case by using the following:
         * <pre>
         * {@code
         *         var parser1 = Parsewisp.parser("S = A S ; A = epsilon ;");
         *         var analysis1 = parser1.grammar().analyze();
         *         System.out.println(analysis1.isProductive(Sym.sym("S"))); // false
         * }
         * </pre>
         *
         * @param start The starting symbol.
         * @return true or false
         */
        public boolean isProductive(final @NotNull Sym start) {
            return isProductive(start, new HashMap<>());
        }

        private boolean isProductive(final @NotNull Sym start, final @NotNull Map<Sym, Boolean> productive) {
            boolean changed;

            if (productive.containsKey(start))
                return productive.get(start);

            do {
                changed = false;
                for (var entry : grammar.entrySet()) {
                    var nt = entry.getKey();
                    var rule = entry.getValue();

                    productive.putIfAbsent(nt, false);

                    if (productiveRule(rule, productive) && !productive.get(nt)) {
                        productive.put(nt, true);
                        changed = true;
                    }
                }
            } while (changed);
            return productive.getOrDefault(start, false);
        }

        private boolean productiveRule(final @NotNull Rule rule, final @NotNull Map<Sym, Boolean> productive) {
            if (rule instanceof Terminal || rule instanceof OptionalRule || rule instanceof ZeroOrMoreRule) {
                return true;
            } else if (rule instanceof SpecialSequenceRule) {
                return true;
            } else if (rule instanceof NonTerminal) {
                return productive.getOrDefault(((NonTerminal) rule).getKeyword(), false);
            } else if (rule instanceof ConcatRule) {
                return ((ConcatRule) rule).getRules().stream().allMatch(it -> productiveRule(it, productive));
            } else if (rule instanceof AlternationRule || rule instanceof OrderedChoiceRule) {
                return ((RuleWithManyChildren) rule).getRules().stream().anyMatch(it -> productiveRule(it, productive));
            } else if (rule instanceof OnceOrMoreRule || rule instanceof LookaheadRule || rule instanceof NegativeLookaheadRule) {
                return productiveRule(((RuleWithChild) rule).getRule(), productive);
            } else if (rule instanceof ExclusionRule) {
                return productiveRule(((ExclusionRule) rule).getParserExpected(), productive)
                        && productiveRule(((ExclusionRule) rule).getParserExcluded(), productive);
            } else if (rule instanceof VariableRepetitionRule) {
                return ((VariableRepetitionRule) rule).getMin() == 0 || productiveRule(rule, productive);
            } else {
                throw new IllegalArgumentException("Can not handle value " + rule + " of type " + rule.getClass() + ".");
            }
        }

//        /**
//         * Check whether a grammar can create an infinite number of empty matches through recursion or repetition.
//         * <p>
//         * Algorithm outline:
//         * - {@link AlternationRule}: If any branch can result in an infinite number of trees.
//         * - {@link ConcatRule}:
//         *
//         * @param start
//         * @return
//         */
//        public boolean infiniteEmptyRecursionPossible(final Sym start) {
//            return infiniteEmptyRecursionPossible(grammar.getProduction(start), new HashMap<>());
//        }
//
//        public boolean infiniteEmptyRecursionPossible(final Rule start, Map<Rule, Boolean> searched) {
//            if (searched.containsKey(start)) {
//                return searched.get(start);
//            }
//
//            final boolean res;
//
//            if (start instanceof OrderedChoiceRule) {
//                var children = ((RuleWithManyChildren) start).getRules();
//                res = children.stream().anyMatch(it -> infiniteEmptyRecursionPossible(it, searched));
//            } else if (start instanceof AlternationRule) {
//                var children = ((RuleWithManyChildren) start).getRules();
//                res = children.stream().anyMatch(it -> infiniteEmptyRecursionPossible(it, searched));
//            } else if (start instanceof ConcatRule) {
//                res = ((ConcatRule) start).getRules().stream().allMatch(it -> infiniteEmptyRecursionPossible(it, searched));
//            } else if (start instanceof EpsilonTerm || start instanceof OptionalRule || start instanceof ZeroOrMoreRule) {
//                res = true;
//            } else if (start instanceof VariableRepetitionRule) {
//                res = ((VariableRepetitionRule) start).getMin() == 0;
//            } else if (start instanceof RegexTerm) {
//                res = ((RegexTerm) start).getRegexp().matcher("").matches();
//            } else if (start instanceof NonTerminal) {
//                res = infiniteEmptyRecursionPossible(grammar.getProduction(((NonTerminal) start).getKeyword()), searched);
//            } else if (start instanceof ExclusionRule) {
//                res = infiniteEmptyRecursionPossible(((ExclusionRule) start).getParserExpected(), searched)
//                        && !infiniteEmptyRecursionPossible(((ExclusionRule) start).getParserExcluded(), searched);
//            } else if (start instanceof StringTerm) {
//                res = ((StringTerm) start).getString().isEmpty();
//            } else if (start instanceof ValueRangeTerm) {
//                res = ((ValueRangeTerm) start).getLo() == 0;
//            } else if (start instanceof LookaheadRule) {
//                res = infiniteEmptyRecursionPossible(((LookaheadRule) start).getRule(), searched);
//            } else if (start instanceof NegativeLookaheadRule) {
//                res = !infiniteEmptyRecursionPossible(((NegativeLookaheadRule) start).getRule(), searched);
//            } else {
//                res = false;
//            }
//
//
//            searched.put(start, res);
//            return res;
//        }

        @Override
        public @NotNull String toString() {
            return "GrammarInfo[" +
                    "definedNTs=" + definedNTs() +
                    ", usedNTs=" + usedNTs() +
                    ", unusedNTs=" + getUnusedNTs() +
                    ", undefinedUsedNTs=" + getUndefinedUsedNTs() +
                    ", isValid=" + isValid() +
                    ']';
        }
    }
}
