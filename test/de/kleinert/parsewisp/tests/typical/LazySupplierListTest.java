package de.kleinert.parsewisp.tests.typical;

import de.kleinert.parsewisp.collections.LazySupplierList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class LazySupplierListTest {
    @Test
    void simpleTest() {
        //noinspection MismatchedQueryAndUpdateOfCollection
        final var lsl = new LazySupplierList<>((i) -> i < 15 ? i : null, Integer.MAX_VALUE);

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
        //noinspection MismatchedQueryAndUpdateOfCollection
        final var lsl = new LazySupplierList<>((i) -> null, Integer.MAX_VALUE);

        Assertions.assertFalse(lsl.isFullyEvaluated());
        Assertions.assertNull(lsl.getOrNull(0));
        Assertions.assertTrue(lsl.isFullyEvaluated());
        Assertions.assertEquals(0, lsl.size());
        Assertions.assertTrue(lsl.isFullyEvaluated());

        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> lsl.get(1));
    }

    @Test
    void streamOnInfinite() {
        // The list is not infinite, but it would still be too big to fit into an array, so it's practically infinite.

        //noinspection MismatchedQueryAndUpdateOfCollection
        final var lsl = new LazySupplierList<>((i) -> i, Integer.MAX_VALUE);

        Assertions.assertEquals(List.of(0, 1, 2, 3, 4), lsl.stream().limit(5).toList());
    }
}
