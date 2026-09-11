package de.kleinert.parsewisp.tests.typical;

import de.kleinert.parsewisp.error.ParserCreationFailure;
import de.kleinert.parsewisp.other_formats.ABNF;
import de.kleinert.parsewisp.testutil.PT;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ABNFTest {
//    @Test
//    void lineCommentToEOF() {
//        Assertions.assertEquals(
//                PT.create("S", "a"),
//                Parsewisp.parser("S = \"a\" ; ignore").parse("a")
//        );
//    }
//
//    @Test
//    void lineCommentToLineEnd() {
//        Assertions.assertEquals(
//                PT.create("S",
//                        PT.create("A", "a")),
//                Parsewisp.parser("""
//                        S = A ; ignore
//                        A = "a"
//                        """)
//                        .parse("a")
//        );
//    }
}