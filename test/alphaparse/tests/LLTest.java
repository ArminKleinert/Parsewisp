package alphaparse.tests;

import alphaparse.Alpha;
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
    static final int reps = 30000;
    static final int runsPerMeasure = 100;
    static final String s = "b" + "a".repeat(reps);
    static final String sReduced = "b" + "a".repeat(reps / 10);

    static final Parser pRegex = Alpha.parser("S = #'ba+'");
    static final Parser pDirect = Alpha.parser("S = 'b' 'a'+");
    static final Parser pLeftRec = Alpha.parser("S = S1\nS1 = S1 'a' | 'b'");
    static final Parser pLeftRecH = Alpha.parser("S = S1\n<S1> = S1 'a' | 'b'");
    static final Parser pRightRec = Alpha.parser("S = 'b' A\nA = 'a' A | 'a'");
    static final Parser pRightRecH = Alpha.parser("S = 'b' A\n<A> = 'a' A | 'a'");
    static final Parser pRightRecE = Alpha.parser("S = 'b' A\nA = 'a' A | epsilon");
    static final Parser pRightRecEH = Alpha.parser("S = 'b' A\n<A> = 'a' A | epsilon");

    @Test
    void pEquality() {
        var tree = pDirect.parse(sReduced);
        Assertions.assertEquals(tree, pLeftRecH.parse(sReduced));
        Assertions.assertEquals(tree, pRightRecH.parse(sReduced));
        Assertions.assertEquals(tree, pRightRecEH.parse(sReduced));
    }

    @Test
    void pRegex_____() {
        System.out.println("."
                + getMethodName()
                + " : "
                + TimeUtil.measureTimeMillisMean(runsPerMeasure, () -> pRegex.parse(s)));
    }

    @Test
    void pDirect____() {
        System.out.println("."
                + getMethodName()
                + " : "
                + TimeUtil.measureTimeMillisMean(runsPerMeasure, () -> pDirect.parse(s)));
    }

    @Test
    void pLeftRec___() {
        System.out.println("."
                + getMethodName()
                + " : "
                + TimeUtil.measureTimeMillisMean(runsPerMeasure, () -> pLeftRec.parse(s)));
    }

    @Test
    void pLeftRecH__() {
        System.out.println("."
                + getMethodName()
                + " : "
                + TimeUtil.measureTimeMillisMean(runsPerMeasure, () -> pLeftRecH.parse(s)));
    }

    @Test
    void pRightRec__() {
        System.out.println("."
                + getMethodName()
                + " : "
                + TimeUtil.measureTimeMillisMean(runsPerMeasure, () -> pRightRec.parse(s)));
    }

    @Test
    void pRightRecH_() {
        System.out.println("."
                + getMethodName()
                + " : "
                + TimeUtil.measureTimeMillisMean(runsPerMeasure, () -> pRightRecH.parse(s)));
    }

    @Test
    void pRightRecE_() {
        System.out.println("."
                + getMethodName()
                + " : "
                + TimeUtil.measureTimeMillisMean(runsPerMeasure, () -> pRightRecE.parse(s)));
    }

    @Test
    void pRightRecEH() {
        System.out.println("."
                + getMethodName()
                + " : "
                + TimeUtil.measureTimeMillisMean(runsPerMeasure, () -> pRightRecEH.parse(s)));
    }

    private static String getMethodName() {
        final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
        return ste[2].getMethodName();
    }
}
