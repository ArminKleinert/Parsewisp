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
    void catToString() {
        var p = Parsewisp.parser("S = \"a\" \"b\"");
        var text = p.show();
        var p2 = Parsewisp.parser(text);
        Assertions.assertEquals(p.grammar(), p2.grammar());
    }

    @Test
    void eofToString() {
        var p = Parsewisp.parser("S = EOF");
        var text = p.show();
        var p2 = Parsewisp.parser(text);
        Assertions.assertEquals(p.grammar(), p2.grammar());
    }

    @Test
    void epsToString() {
        var p = Parsewisp.parser("S = epsilon");
        var text = p.show();
        var p2 = Parsewisp.parser(text);
        Assertions.assertEquals(p.grammar(), p2.grammar());
    }

    @Test
    void exclusionToString() {
        var p = Parsewisp.parser("S = \"a\"* - \"a\"");
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
        var p = Parsewisp.parser("S = A\nA = \"a\"");
        var text = p.show();
        var p2 = Parsewisp.parser(text);
        Assertions.assertEquals(p.grammar(), p2.grammar());
    }

    @Test
    void onceOrMoreToString() {
        var p = Parsewisp.parser("S = \"a\"+");
        var text = p.show();
        var p2 = Parsewisp.parser(text);
        Assertions.assertEquals(p.grammar(), p2.grammar());
    }

    @Test
    void optToString() {
        var p = Parsewisp.parser("S = \"a\"?");
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
    void regexpToString() {
        var p = Parsewisp.parser("S = #\"a\"");
        var text = p.show();
        var p2 = Parsewisp.parser(text);
        Assertions.assertEquals(p.grammar(), p2.grammar());
    }

    @Test
    void specialToString() {
        // TODO
    }

    @Test
    void literalToString() {
        var p = Parsewisp.parser("S = \"a\"");
        var text = p.show();
        var p2 = Parsewisp.parser(text);
        Assertions.assertEquals(p.grammar(), p2.grammar());
    }

    @Test
    void valRangeToString() {
        var p = Parsewisp.parser("S = %x41-42 %d65");
        var text = p.show();
        var p2 = Parsewisp.parser(text);
        Assertions.assertEquals(p.grammar(), p2.grammar());
    }

    @Test
    void repToString() {
        var p = Parsewisp.parser("S = 1*4\"a\"");
        var text = p.show();
        var p2 = Parsewisp.parser(text);
        Assertions.assertEquals(p.grammar(), p2.grammar());
    }

    @Test
    void zeroOrMoreToString() {
        var p = Parsewisp.parser("S = \"a\"*");
        var text = p.show();
        var p2 = Parsewisp.parser(text);
        Assertions.assertEquals(p.grammar(), p2.grammar());
    }
}