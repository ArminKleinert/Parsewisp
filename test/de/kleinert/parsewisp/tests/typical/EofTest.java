package de.kleinert.parsewisp.tests.typical;

import de.kleinert.parsewisp.Parsewisp;
import de.kleinert.parsewisp.parser_options.ParserCreationOptions;
import de.kleinert.parsewisp.parser_options.RulesAvailable;
import de.kleinert.parsewisp.parsing.EOFTerm;
import de.kleinert.parsewisp.testutil.PT;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class EofTest {
    @Test
    void basicTest1() {
        var opts = ParserCreationOptions.getDefault().addAvailableRule(RulesAvailable.EXPLICIT_EOF);

        Assertions.assertEquals(
                PT.create("S"),
                Parsewisp.parser("S = " + EOFTerm.text(), opts).parse(""));

        Assertions.assertEquals(
                PT.create("S"),
                Parsewisp.parser("S = <' '>" + EOFTerm.text(), opts).parse(" "));

        Assertions.assertEquals(
                PT.create("S", "a"),
                Parsewisp.parser("S = 'a' " + EOFTerm.text(), opts).parse("a"));
    }

    @Test
    void eofInParserWithWhitespace() {
        var opts = ParserCreationOptions.newWithStandardWhitespace().addAvailableRule(RulesAvailable.EXPLICIT_EOF);

        Assertions.assertEquals(
                PT.create("S"),
                Parsewisp.parser("S = " + EOFTerm.text(), opts).parse(""));

        Assertions.assertEquals(
                PT.create("S"),
                Parsewisp.parser("S = " + EOFTerm.text(), opts).parse(" "));
    }
}
