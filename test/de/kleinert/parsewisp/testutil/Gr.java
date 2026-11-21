package de.kleinert.parsewisp.testutil;

import de.kleinert.parsewisp.Sym;
import de.kleinert.parsewisp.grammar.Grammar;
import de.kleinert.parsewisp.parsing.Rule;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.LinkedHashMap;

/**
 * Helper class for creating Grammars for test cases.
 */
public class Gr {
    private Gr() {
    }

    /**
     * Helper method for creating Grammars for test cases.
     * @param g Input.
     * @return A grammar.
     */
    public static @NotNull Grammar grammar(Object... g) {
        if (g.length == 0 || (g.length & 1) == 1)
            throw new IllegalArgumentException();
        var lhm = new LinkedHashMap<Sym, Rule>();
        Sym start = null;
        var iter = Arrays.stream(g).iterator();
        do {
            var key = Sym.sym((String) iter.next());
            if (start == null) start = key;
            var val = (Rule) iter.next();
            lhm.put(key, val);
        } while (iter.hasNext());
        return new Grammar(start, lhm);
    }

    /**
     * Helper method for creating Grammars for test cases.
     * @param startName Name of the start sym.
     * @param g Input.
     * @return A grammar.
     */
    public static @NotNull Grammar grammarS(String startName, Object... g) {
        if (g.length == 0 || (g.length & 1) == 1)
            throw new IllegalArgumentException();
        var lhm = new LinkedHashMap<Sym, Rule>();
        Sym start = Sym.sym(startName);
        var iter = Arrays.stream(g).iterator();
        do {
            var key = Sym.sym((String) iter.next());
            var val = (Rule) iter.next();
            lhm.put(key, val);
        } while (iter.hasNext());
        return new Grammar(start, lhm);
    }
}
