package alphaparse.collections;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class FlatResultSeqTest {
    @Test
    void isEmpty() {
        Assertions.assertTrue(FlatResultSeq.make().isEmpty());

        var frs12 = FlatResultSeq.make().appendOrConcat(1).appendOrConcat(2);
        Assertions.assertFalse(frs12.isEmpty());

        var frs1212 = frs12.appendOrConcat(frs12);
        Assertions.assertFalse(frs1212.isEmpty());

        var frs12123 = frs1212.appendOrConcat(3);
        Assertions.assertFalse(frs12123.isEmpty());
    }

    @Test
    void size() {
        Assertions.assertEquals(0, FlatResultSeq.make().size());

        var frs12 = FlatResultSeq.make().appendOrConcat(1).appendOrConcat(2);
        Assertions.assertEquals(2, frs12.size());

        var frs1212 = frs12.appendOrConcat(frs12);
        Assertions.assertEquals(4, frs1212.size());

        var frs12123 = frs1212.appendOrConcat(3);
        Assertions.assertEquals(5, frs12123.size());
    }

    @Test
    void stream() {
        Assertions.assertEquals(new ArrayList<>(), FlatResultSeq.make().stream().toList());

        var frs12 = FlatResultSeq.make().appendOrConcat(1).appendOrConcat(2);
        var frs121231212 = frs12.appendOrConcat(frs12).appendOrConcat(3).appendOrConcat(frs12).appendOrConcat(frs12);

        Assertions.assertEquals(
                List.of(2, 2, 2, 2),
                frs121231212.stream().filter(it -> ((Integer) it) % 2 == 0).toList());

        Assertions.assertEquals(
                frs121231212.stream().toList(),
                frs121231212.stream().toList());
    }
}