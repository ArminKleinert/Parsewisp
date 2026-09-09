package de.kleinert.parsewisp.tests;

import de.kleinert.parsewisp.Parsewisp;
import de.kleinert.parsewisp.functions.Procedure;
import de.kleinert.parsewisp.parser.Parser;
import de.kleinert.parsewisp.testutil.TimeUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/*
Array-based:
.pDirect____ : 734.5994077222223
.pLeftRecH__ : 5513.966410888888
.pLeftRec___ : 89.11456878888889
.pRightRecEH : 5935.430014133333
.pRightRecE_ : 168.37849315555556
.pRightRecH_ : 5533.560816744444
.pRightRec__ : 160.0001591333333
.pRegex_____ : 0.1617999

Array+Marker:
.pDirect____ : 24.482446644444444
.pLeftRecH__ : 5373.383967522222
.pLeftRec___ : 98.55333840000002
.pRightRecEH : 5385.678086377778
.pRightRecE_ : 172.39012652222223
.pRightRecH_ : 5309.490438144445
.pRightRec__ : 157.92169719999998
.pRegex_____ : 0.1264512111111111

Linked:
.pDirect____ : 22.615405199999998
.pLeftRecH__ : 5353.148949677778
.pLeftRec___ : 89.34735326666667
.pRightRecEH : 5850.970126155556
.pRightRecE_ : 174.1519648
.pRightRecH_ : 5599.451224055556
.pRightRec__ : 156.4384317777778
.pRegex_____ : 0.1361259
 */

class LLTest {
    final int reps = 300;
    final int runsPerMeasure = 100;
    final String s = "b" + "a".repeat(reps);
    final String sReduced = "b" + "a".repeat(reps / 10);

    final Parser pRegex = Parsewisp.parser("S = #'ba+'");
    final Parser pDirect = Parsewisp.parser("S = 'b' 'a'+");
    final Parser pLeftRec = Parsewisp.parser("S = S1\nS1 = S1 'a' | 'b'");
    final Parser pLeftRecH = Parsewisp.parser("S = S1\n<S1> = S1 'a' | 'b'");
    final Parser pRightRec = Parsewisp.parser("S = 'b' A\nA = 'a' A | 'a'");
    final Parser pRightRecH = Parsewisp.parser("S = 'b' A\n<A> = 'a' A | 'a'");
    final Parser pRightRecE = Parsewisp.parser("S = 'b' A\nA = 'a' A | epsilon");
    final Parser pRightRecEH = Parsewisp.parser("S = 'b' A\n<A> = 'a' A | epsilon");

    @Test
    void pEqualityReduced() {
        var tree = pDirect.parse(sReduced);
        Assertions.assertEquals(tree, pLeftRecH.parse(sReduced));
        Assertions.assertEquals(tree, pRightRecH.parse(sReduced));
        Assertions.assertEquals(tree, pRightRecEH.parse(sReduced));
    }

    @Test
    void pEquality() {
        var tree = pDirect.parse(s);
        Assertions.assertEquals(tree, pLeftRecH.parse(s));
        Assertions.assertEquals(tree, pRightRecH.parse(s));
        Assertions.assertEquals(tree, pRightRecEH.parse(s));
    }

    @Test
    void pRegex_____() {
        printStat(() -> pRegex.parse(s));
    }

    @Test
    void pDirect____() {
        printStat(() -> pDirect.parse(s));
    }

    @Test
    void pLeftRec___() {
        printStat(() -> pLeftRec.parse(s));
    }

    @Test
    void pLeftRecH__() {
        printStat(() -> pLeftRecH.parse(s));
    }

    @Test
    void pRightRec__() {
        printStat(() -> pRightRec.parse(s));
    }

    @Test
    void pRightRecH_() {
        printStat(() -> pRightRecH.parse(s));
    }

    @Test
    void pRightRecE_() {
        printStat(() -> pRightRecE.parse(s));
    }

    @Test
    void pRightRecEH() {
        printStat(() -> pRightRecEH.parse(s));
    }

    private void printStat(Procedure prod) {
        final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
        System.out.println("."
                + ste[2].getMethodName()
                + " : "
                + TimeUtil.measureTimeMillisMean(runsPerMeasure, prod));
    }
}
