//package alphaparse.tests.typical;
//
//import alphaparse.Alpha;
//import alphaparse.Sym;
//import alphaparse.result.PT;
//import org.junit.Rule;
//import org.junit.Test;
//import org.junit.jupiter.api.Assertions;
//import org.junit.rules.Timeout;
//
//import java.util.List;
//
//public class InfiniteEpsilonAnalysisTest {
//    /**
//     * Do I really need to document default constructors for test cases?
//     */
//    public InfiniteEpsilonAnalysisTest() {
//    }
//
//    /**
//     * Timeout for all test cases in this class.
//     */
//    @Rule
//    public Timeout timeout = Timeout.millis(1000);
//
//    /**
//     * <pre>
//     *     Mode: All parses.
//     *     Grammar: {@code S = S | epsilon}
//     *     Text: {@code ""}
//     *     Expect: Does something other than timing out.
//     *     Status: Timeout.
//     *     Note: This is technically correct behavior, but...
//     * </pre>
//     */
//    @Test
//    public void infiniteEpsilon() {
//        var p = Alpha.parser("S = S | epsilon");
//        Assertions.assertEquals(PT.create("S"), p.parse(""));
//        Assertions.assertEquals(
//                List.of(PT.create("S"),
//                        PT.create("S", PT.create("S")),
//                        PT.create("S", PT.create("S", PT.create("S")))),
//                p.parses("").stream().limit(3).toList());
//        Assertions.assertTrue(p.grammar().analyze().infiniteEmptyRecursionPossible(Sym.sym("S")));
//        //Assertions.assertDoesNotThrow(() -> p.parses("").size());
//    }
//
//    @Test
//    public void infiniteEpsilon2() {
//        var p = Alpha.parser("S = A epsilon | epsilon\nA = S");
//        Assertions.assertEquals(PT.create("S"), p.parse(""));
////        Assertions.assertEquals(
////                List.of(PT.create("S"),
////                        PT.create("S",PT.create("S")),
////                        PT.create("S",PT.create("S",PT.create("S")))),
////                p.parses("").stream().limit(3).toList());
//        Assertions.assertTrue(p.grammar().analyze().infiniteEmptyRecursionPossible(Sym.sym("S")));
////        Assertions.assertDoesNotThrow(() -> p.parses("").size());
//    }
//}
