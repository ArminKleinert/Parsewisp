package de.kleinert.parsewisp.tests.typical;

import de.kleinert.parsewisp.Parsewisp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class GrammarPrinterTest {
    @Test
    void parens() {
        var p = Parsewisp.parser("S = \"a\" \"b\" | \"c\" \"d\"");
        Assertions.assertEquals(
                p.grammar(), Parsewisp.parser(p.show()).grammar());

        var p2 = Parsewisp.parser("S = (\"a\" | \"b\") (\"c\" | \"d\")");
        Assertions.assertEquals(
                p2.grammar(), Parsewisp.parser(p2.show()).grammar());
    }

    @Test
    void altToString() {
        var p = Parsewisp.parser("S = \"a\" | \"b\"");
        var text = p.show();
        var p2 = Parsewisp.parser(text);
        Assertions.assertEquals(p.grammar(), p2.grammar());
    }

    @Test
    void ordToString() {
        var p = Parsewisp.parser("S = \"a\" / \"b\"");
        var text = p.show();
        var p2 = Parsewisp.parser(text);
        Assertions.assertEquals(p.grammar(), p2.grammar());
    }

    @Test
    void catToString() {
        var p = Parsewisp.parser("S = \"a\" \"b\"");
        var text = p.show();
        var p2 = Parsewisp.parser(text);
        Assertions.assertEquals(p.grammar(), p2.grammar());
    }

    @Test
    void epsToString() {
        // complicated structure because a simple `S = eps epsilon ε ...` might be optimized to `S = ε`, but we want to test all forms.
        var p = Parsewisp.parser("""
                S = E1 E2 E3 E4 E5 E6
                E1 = eps
                E2 = epsilon
                E3 = Epsilon
                E4 = EPSILON
                E5 = ε
                E6 = ""
                """);
        var text = p.show();
        var p2 = Parsewisp.parser(text);
        Assertions.assertEquals(p.grammar(), p2.grammar());
    }

    @Test
    void lookToString() {
        var p = Parsewisp.parser("S = &\"a\" \"a\"*");
        var text = p.show();
        var p2 = Parsewisp.parser(text);
        Assertions.assertEquals(p.grammar(), p2.grammar());
    }

    @Test
    void negToString() {
        var p = Parsewisp.parser("S = !\"a\" \"b\"");
        var text = p.show();
        var p2 = Parsewisp.parser(text);
        Assertions.assertEquals(p.grammar(), p2.grammar());
    }

    @Test
    void ntToString() {
        var p = Parsewisp.parser("S = A\nA = \"a\" S | \"\"");
        var text = p.show();
        var p2 = Parsewisp.parser(text);
        Assertions.assertEquals(p.grammar(), p2.grammar());
    }

    @Test
    void optToString() {
        {
            var p = Parsewisp.parser("S = [ \"a\" ]");
            var text = p.show();
            var p2 = Parsewisp.parser(text);
            Assertions.assertEquals(p.grammar(), p2.grammar());
        }
        {
            var p = Parsewisp.parser("S = \"a\"?");
            var text = p.show();
            var p2 = Parsewisp.parser(text);
            Assertions.assertEquals(p.grammar(), p2.grammar());
        }
    }

    @Test
    void regexpToString() {
        {
            var p = Parsewisp.parser("S = #\"a\"");
            var text = p.show();
            var p2 = Parsewisp.parser(text);
            Assertions.assertEquals(p.grammar(), p2.grammar());
        }
        {
            var p = Parsewisp.parser("S = #'a'");
            var text = p.show();
            var p2 = Parsewisp.parser(text);
            Assertions.assertEquals(p.grammar(), p2.grammar());
        }
    }

    @Test
    void literalToString() {
        {
            var p = Parsewisp.parser("S = \"a\"");
            var text = p.show();
            var p2 = Parsewisp.parser(text);
            Assertions.assertEquals(p.grammar(), p2.grammar());
        }
        {
            var p = Parsewisp.parser("S = 'a'");
            var text = p.show();
            var p2 = Parsewisp.parser(text);
            Assertions.assertEquals(p.grammar(), p2.grammar());
        }
        {
            var p = Parsewisp.parser("S = %s\"a\"");
            var text = p.show();
            var p2 = Parsewisp.parser(text);
            Assertions.assertEquals(p.grammar(), p2.grammar());
        }
        {
            var p = Parsewisp.parser("S = %s'a'");
            var text = p.show();
            var p2 = Parsewisp.parser(text);
            Assertions.assertEquals(p.grammar(), p2.grammar());
        }
        {
            var p = Parsewisp.parser("S = %i\"a\"");
            var text = p.show();
            var p2 = Parsewisp.parser(text);
            Assertions.assertEquals(p.grammar(), p2.grammar());
        }
        {
            var p = Parsewisp.parser("S = %i'a'");
            var text = p.show();
            var p2 = Parsewisp.parser(text);
            Assertions.assertEquals(p.grammar(), p2.grammar());
        }
    }

    @Test
    void zeroOrMoreToString() {
        {
            var p = Parsewisp.parser("S = { \"a\" }");
            var text = p.show();
            var p2 = Parsewisp.parser(text);
            Assertions.assertEquals(p.grammar(), p2.grammar());
        }
        {
            var p = Parsewisp.parser("S = \"a\"*");
            var text = p.show();
            var p2 = Parsewisp.parser(text);
            Assertions.assertEquals(p.grammar(), p2.grammar());
        }
    }
}