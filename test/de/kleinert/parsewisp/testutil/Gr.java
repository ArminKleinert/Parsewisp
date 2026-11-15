package de.kleinert.parsewisp.testutil;

import de.kleinert.parsewisp.Sym;
import de.kleinert.parsewisp.grammar.Grammar;
import de.kleinert.parsewisp.parsing.Rule;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Helper class for creating Grammars for test cases.
 */
public class Gr {
    public static @NotNull Grammar grammar(Map<String, Rule>g){
        if (g.isEmpty())
            throw new IllegalArgumentException();
        var lhm = new LinkedHashMap<Sym, Rule>();
        Sym start = null;
        for (Map.Entry<String, Rule> stringRuleEntry : g.entrySet()) {
            if (start == null) start = Sym.sym(stringRuleEntry.getKey());
            lhm.put(start, stringRuleEntry.getValue());
        }
        return new Grammar(start, lhm);
    }
}
