package de.kleinert.parsewisp.tests.typical;

import de.kleinert.parsewisp.util.StrParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class StrParserTest {
    private final StrParser strParser = new StrParser();

    @Test
    void testStr1() {
        var s = strParser.processString("\"a\"");
        Assertions.assertEquals(1, s.length());
        Assertions.assertEquals("a", s);
    }

    @Test
    void testStr2() {
        var s = strParser.processString("'a'");
        Assertions.assertEquals(1, s.length());
        Assertions.assertEquals("a", s);
    }

    @Test
    void testStr3() {
        var s = strParser.processString("'\\u1234'");
        Assertions.assertEquals("ሴ", s);
    }

    @Test
    void testReg1() {
        var s = strParser.processString("#\"[a]\"");
        Assertions.assertEquals(4, s.length());
        Assertions.assertEquals("\"[a]", s);
    }

    @Test
    void testReg2() {
        var s = strParser.processString("#'[a]'");
        //Assertions.assertEquals(4, s.length());
        Assertions.assertEquals("'[a]", s);
    }
}
