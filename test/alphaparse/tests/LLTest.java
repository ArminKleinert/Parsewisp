package alphaparse.tests;

import alphaparse.Alpha;
import alphaparse.parser.Parser;
import alphaparse.testutil.TimeUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/*
OLD:

.pDirect____ : 739.3471753222223
.pLeftRecH__ : 5409.360537688889
.pLeftRec___ : 93.36093935555556
.pRightRecEH : 5727.797799855555
.pRightRecE_ : 170.1513648222222
.pRightRecH_ : 5411.653376633333
.pRightRec__ : 159.00848392222224
.pRegex_____ : 0.15353901111111112

Process finished with exit code 0

 */

public class LLTest {

    static final int reps = 30000;
    static final int runsPerMeasure = 100;
    static final String s = "b" + "a".repeat(reps);

    static final Parser pRegex = Alpha.parser("S = #'ba+'");
    static final Parser pDirect = Alpha.parser("S = 'b' 'a'+");
    static final Parser pLeftRec = Alpha.parser("S = S1\nS1 = S1 'a' | 'b'");
    static final Parser pLeftRecH = Alpha.parser("S = S1\n<S1> = S1 'a' | 'b'");
    static final Parser pRightRec = Alpha.parser("S = 'b' A\nA = 'a' A | 'a'");
    static final Parser pRightRecH = Alpha.parser("S = 'b' A\n<A> = 'a' A | 'a'");
    static final Parser pRightRecE = Alpha.parser("S = 'b' A\nA = 'a' A | epsilon");
    static final Parser pRightRecEH = Alpha.parser("S = 'b' A\n<A> = 'a' A | epsilon");

    @BeforeEach
    void clean() {
        System.gc();
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
