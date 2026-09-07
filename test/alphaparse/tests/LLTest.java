package alphaparse.tests;

import alphaparse.Alpha;
import alphaparse.functions.Procedure;
import alphaparse.parser.Parser;
import alphaparse.testutil.TimeUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/*
OLD:
.pDirect____ : 818.3564290666667
.pLeftRecH__ : 5648.402744588889
.pLeftRec___ : 93.51533295555556
.pRightRecEH : 5422.940529233334
.pRightRecE_ : 166.97516004444444
.pRightRecH_ : 5269.696173433334
.pRightRec__ : 154.55672841111112
.pRegex_____ : 0.09188858888888889

NEW:
.pDirect____ : 25.095488355555556
.pLeftRecH__ : 5464.817504477778
.pLeftRec___ : 88.95493266666666
.pRightRecEH : 5563.318510811111
.pRightRecE_ : 168.4477270111111
.pRightRecH_ : 5544.912572888889
.pRightRec__ : 154.1189783111111
.pRegex_____ : 0.07210718888888888
 */

class LLTest {
    final int reps = 30_000;
    final int runsPerMeasure = 100;
    final String s = "b" + "a".repeat(reps);
    final String sReduced = "b" + "a".repeat(reps / 10);

    final Parser pRegex = Alpha.parser("S = #'ba+'");
    final Parser pDirect = Alpha.parser("S = 'b' 'a'+");
    final Parser pLeftRec = Alpha.parser("S = S1\nS1 = S1 'a' | 'b'");
    final Parser pLeftRecH = Alpha.parser("S = S1\n<S1> = S1 'a' | 'b'");
    final Parser pRightRec = Alpha.parser("S = 'b' A\nA = 'a' A | 'a'");
    final Parser pRightRecH = Alpha.parser("S = 'b' A\n<A> = 'a' A | 'a'");
    final Parser pRightRecE = Alpha.parser("S = 'b' A\nA = 'a' A | epsilon");
    final Parser pRightRecEH = Alpha.parser("S = 'b' A\n<A> = 'a' A | epsilon");

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
