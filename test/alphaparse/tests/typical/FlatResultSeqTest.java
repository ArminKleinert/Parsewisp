package alphaparse.tests.typical;

import alphaparse.collections.FlatResultSeq;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FlatResultSeqTest {
    @Test void emptyIsEmpty() {
        Assertions.assertTrue(FlatResultSeq.make().isEmpty());
    }
    @Test void emptyLength(){Assertions.assertEquals(0, FlatResultSeq.make().size());}
    @Test void appendSingle(){
        var seq = FlatResultSeq.make().appendOrConcat("a");
        Assertions.assertEquals(1, seq.size());
        var seq2 = seq.appendOrConcat("b");
        Assertions.assertEquals(1, seq.size());
        Assertions.assertEquals(2, seq2.size());
        var seq3 = seq2.appendOrConcat("c");
        Assertions.assertEquals(1, seq.size());
        Assertions.assertEquals(2, seq2.size());
        Assertions.assertEquals(3, seq3.size());
    }
    @Test void appendMulti(){
        var seq = FlatResultSeq.make().appendOrConcat("a").appendOrConcat("b");
        var seq2 = FlatResultSeq.make().appendOrConcat("c").appendOrConcat("d");
        var seqSeq = seq.appendOrConcat(seq2);
        Assertions.assertEquals(2, seq.size());
        Assertions.assertEquals(4, seqSeq.size());
        var seq3 = FlatResultSeq.make().appendOrConcat("e").appendOrConcat("f");
        var seq4 = FlatResultSeq.make().appendOrConcat("g");
        var seqSeqSeqG = seq.appendOrConcat(seq2).appendOrConcat(seq3).appendOrConcat(seq4);
        Assertions.assertEquals(7, seqSeqSeqG.size());
    }
}
