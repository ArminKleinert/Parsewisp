package de.kleinert.parsewisp.tests.typical;

import de.kleinert.parsewisp.Parsewisp;
import de.kleinert.parsewisp.parser_options.ParserCreationOptions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class IdentifierNamesTest {
    @Test
    void extendedNamesAllowed() {
        var opts = ParserCreationOptions.getDefault();
        Assertions.assertThrows(Exception.class, () -> Parsewisp.parser("1 = 'a'", opts));
        Assertions.assertDoesNotThrow(() -> Parsewisp.parser("S = 'a'", opts));
        Assertions.assertDoesNotThrow(() -> Parsewisp.parser("\uD83C\uDF81 = 'a'", opts));
        Assertions.assertDoesNotThrow(() -> Parsewisp.parser("a123 = 'a'", opts));
        Assertions.assertDoesNotThrow(() -> Parsewisp.parser("a_123 = 'a'", opts));
    }
}
