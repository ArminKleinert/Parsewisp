package alphaparse.tests.typical;

import alphaparse.Alpha;
import alphaparse.error.ParserCreationFailure;
import alphaparse.parser_options.ParserCreationOptions;
import alphaparse.testutil.PT;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ABNFTest {
//    @Test
//    void lineCommentToEOF() {
//        Assertions.assertEquals(
//                PT.create("S", "a"),
//                Alpha.parser("S = \"a\" ; ignore", ParserCreationOptions.abnf()).parse("a")
//        );
//    }
//
//    @Test
//    void lineCommentToLineEnd() {
//        Assertions.assertEquals(
//                PT.create("S",
//                        PT.create("A", "a")),
//                Alpha.parser("""
//                        S = A ; ignore
//                        A = "a"
//                        """, ParserCreationOptions.abnf())
//                        .parse("a")
//        );
//    }

    @Test
    void singleQuotesForStringTerminalsNotAllowed() {
        Assertions.assertThrows(
                ParserCreationFailure.class,
                () -> Alpha.parser("S = 'abc'", ParserCreationOptions.abnf()));
    }

    @Test
    void caseSensitivityTest() {
        {
            var p = Alpha.parser("S = \"abc\"", ParserCreationOptions.abnf());
            Assertions.assertEquals(PT.create("S", "abc"), p.parse("abc"));
            Assertions.assertEquals(PT.create("S", "abc"), p.parse("AbC"));
            Assertions.assertEquals(PT.create("S", "abc"), p.parse("ABC"));
        }
        {
            var p = Alpha.parser("S = \"A\" \"B\"", ParserCreationOptions.abnf());
            Assertions.assertEquals(PT.create("S", "A", "B"), p.parse("ab"));
            Assertions.assertEquals(PT.create("S", "A", "B"), p.parse("Ab"));
            Assertions.assertEquals(PT.create("S", "A", "B"), p.parse("AB"));
        }
    }

    @Test
    void countedRepetitionTestExact() {
        var p = Alpha.parser("S = 2 \"A\"", ParserCreationOptions.abnf());
        Assertions.assertTrue(p.parse("").isFailure());
        Assertions.assertTrue(p.parse("A").isFailure());
        Assertions.assertEquals(PT.create("S", "A", "A"), p.parse("aa"));
        Assertions.assertTrue(p.parse("AAA").isFailure());
    }

    @Test
    void countedRepetitionTestSameSides() {
        var p = Alpha.parser("S = 2*2 \"A\"", ParserCreationOptions.abnf());
        Assertions.assertTrue(p.parse("").isFailure());
        Assertions.assertTrue(p.parse("a").isFailure());
        Assertions.assertEquals(PT.create("S", "A", "A"), p.parse("aa"));
        Assertions.assertTrue(p.parse("aaa").isFailure());
    }

    @Test
    void countedRepetitionTestRightOnly() {
        var p = Alpha.parser("S = *2 \"A\"", ParserCreationOptions.abnf());
        Assertions.assertEquals(PT.create("S"), p.parse(""));
        Assertions.assertEquals(PT.create("S", "A"), p.parse("a"));
        Assertions.assertEquals(PT.create("S", "A", "A"), p.parse("aa"));
        Assertions.assertTrue(p.parse("aaa").isFailure());
    }

    @Test
    void countedRepetitionTestLeftOnly() {
        var p = Alpha.parser("S = 2* \"A\"", ParserCreationOptions.abnf());
        Assertions.assertTrue(p.parse("").isFailure());
        Assertions.assertTrue(p.parse("a").isFailure());
        Assertions.assertEquals(PT.create("S", "A", "A"), p.parse("aa"));
        Assertions.assertEquals(PT.create("S", "A", "A", "A"), p.parse("aaa"));
    }

    @Test
    void countedRepetitionTestBoth() {
        var p = Alpha.parser("S = 1*2 \"A\"", ParserCreationOptions.abnf());
        Assertions.assertTrue(p.parse("").isFailure());
        Assertions.assertEquals(PT.create("S", "A"), p.parse("a"));
        Assertions.assertEquals(PT.create("S", "A", "A"), p.parse("aa"));
        Assertions.assertTrue(p.parse("aaa").isFailure());
    }

    @Test
    void countedRepetitionTestStarOnly() {
        var p = Alpha.parser("S = * \"A\"", ParserCreationOptions.abnf());
        Assertions.assertEquals(PT.create("S"), p.parse(""));
        Assertions.assertEquals(PT.create("S", "A"), p.parse("a"));
        Assertions.assertEquals(PT.create("S", "A", "A"), p.parse("aa"));
        Assertions.assertEquals(PT.create("S", "A", "A", "A"), p.parse("aaa"));
    }

    @Test
    void incrementalExtensionTest() {
        var p = Alpha.parser("""
                S =  "a" S
                S =/ "b" S
                S =/ ε
                """, ParserCreationOptions.abnf());
        var epsTree = PT.create("S");
        Assertions.assertEquals(
                PT.create("S", "a", epsTree),
                p.parse("a")
        );
        Assertions.assertEquals(
                PT.create("S", "a", PT.create("S", "b", epsTree)),
                p.parse("ab")
        );
        Assertions.assertEquals(
                PT.create("S", "a", PT.create("S", "a", epsTree)),
                p.parse("aa")
        );
    }

    @Test
    void codepointsTest() {
        {
            var p = Alpha.parser("""
                    S = %x41-43
                    """, ParserCreationOptions.abnf());
            Assertions.assertEquals(
                    PT.create("S", "A"),
                    p.parse("A")
            );
        }
        {
            var p = Alpha.parser("""
                    S = %d65-67
                    """, ParserCreationOptions.abnf());
            Assertions.assertEquals(
                    PT.create("S", "A"),
                    p.parse("A")
            );
        }
        {
            var p = Alpha.parser("S = 1* A\n<A> = %d66", ParserCreationOptions.abnf());
            Assertions.assertEquals(
                    PT.create("S", "B"),
                    p.parse("B")
            );
        }
        {
            var p = Alpha.parser("S = 1* (%d65-67)", ParserCreationOptions.abnf());
            Assertions.assertEquals(
                    PT.create("S", "B", "B", "B"),
                    p.parse("BBB")
            );
        }
    }

    @Test
    void codepointFailureTest() {
        {
            var p = Alpha.parser("S = 1* (%d65-67)", ParserCreationOptions.abnf());
            Assertions.assertEquals(
                    PT.create("S", "B"),
                    p.parse("B")
            );
        }
    }
}