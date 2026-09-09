package de.kleinert.parsewisp.tests.typical;

import de.kleinert.parsewisp.collections.LazySupplierList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class LazySupplierListTest {
    @Test
    void simpleTest() {
        @SuppressWarnings("MismatchedQueryAndUpdateOfCollection") final var lsl = new LazySupplierList<>((i) -> i < 15 ? i : null, Integer.MAX_VALUE);

        Assertions.assertFalse(lsl.isFullyEvaluated());

        Assertions.assertEquals(0, lsl.getFirst());
        Assertions.assertEquals(5, lsl.get(5));
        Assertions.assertEquals(15, lsl.size());
        Assertions.assertEquals(5, lsl.get(5));

        Assertions.assertTrue(lsl.isFullyEvaluated());

        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> lsl.get(-1));
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> lsl.get(15));
    }

    @Test
    void emptyTest() {
        @SuppressWarnings("MismatchedQueryAndUpdateOfCollection") final var lsl = new LazySupplierList<>((i) -> null, Integer.MAX_VALUE);

        Assertions.assertFalse(lsl.isFullyEvaluated());
        Assertions.assertNull(lsl.getOrNull(0));
        Assertions.assertTrue(lsl.isFullyEvaluated());
        Assertions.assertEquals(0, lsl.size());
        Assertions.assertTrue(lsl.isFullyEvaluated());

        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> lsl.get(1));
    }
}
