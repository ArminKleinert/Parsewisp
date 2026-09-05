package alphaparse.tests;

import alphaparse.Alpha;
import org.junit.jupiter.api.Test;

public class LL1Test {

    static final int reps = 35000;
    static final String s = "b" + "a".repeat(reps);

//    @BeforeEach
//    public void warmUp() {
//        var grammar = "S = S1\n<S1> = S1 'a' | 'b'";
//        var p = Alpha.parser(grammar);
//        p.parse(s);
//        p.parse(s);
//        p.parse(s);
//    }

    @Test
    void testLeftRec() {
        var grammar = "S = S1\nS1 = S1 'a' | 'b'";
        var p = Alpha.parser(grammar);
        p.parse(s);
    }

    @Test
    void testLeftRec1() {
        var grammar = "S = S1\nS1 = S1 'a' | 'b'";
        var p = Alpha.parser(grammar);
        p.parse(s);
    }

    @Test
    void testRightRec() {
        var grammar = "S = 'b' A\nA = 'a' A | 'a'";
        var p = Alpha.parser(grammar);
        p.parse(s);
    }

    @Test
    void testRightRec1() {
        var grammar = "S = 'b' A\nA = 'a' A | 'a'";
        var p = Alpha.parser(grammar);
        p.parse(s);
    }

    @Test
    void testRightRecEps() {
        var grammar = "S = 'b' A\nA = 'a' A | epsilon";
        var p = Alpha.parser(grammar);
        p.parse(s);
    }

    @Test
    void testRightRecEps1() {
        var grammar = "S = 'b' A\nA = 'a' A | epsilon";
        var p = Alpha.parser(grammar);
        p.parse(s);
    }

    @Test
    void testDirect() {
        var grammar = "S = 'b' 'a'+";
        var p = Alpha.parser(grammar);
        p.parse(s);
    }

    @Test
    void testDirect1() {
        var grammar = "S = 'b' 'a'+";
        var p = Alpha.parser(grammar);
        p.parse(s);
    }
}
