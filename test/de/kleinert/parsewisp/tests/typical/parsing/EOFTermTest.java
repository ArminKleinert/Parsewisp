package de.kleinert.parsewisp.tests.typical.parsing;

import de.kleinert.parsewisp.parsing.EOFTerm;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class EOFTermTest {

    @Test
    void getDefault() {
        Assertions.assertEquals(EOFTerm.getDefault(), EOFTerm.getDefault());
    }


    @Test
    void parse() {        //TODO
    }

    @Test
    void parseError() {        //TODO
    }

    @Test
    void fullParseSingle() {        //TODO
    }

    @Test
    void fullParseConcat() {//TODO
    }

    @Test
    void fullParseError() {//TODO
    }

    @Test
    void withHideTag() {
    }

    @Test
    void withReduction() {
    }
}