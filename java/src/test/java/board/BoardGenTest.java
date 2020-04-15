package board;

import board.query.LinearFreqAccProcessor;
import org.eclipse.collections.api.list.primitive.DoubleList;
import org.eclipse.collections.api.list.primitive.IntList;
import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;
import org.eclipse.collections.impl.list.mutable.primitive.LongArrayList;
import org.junit.Test;
import summary.SketchGen;
import summary.ItemDictCompressorGen;
import summary.accumulator.ExactFreqAccumulator;
import summary.compressor.CoopFreqCompressor;

import static org.junit.Assert.*;

public class BoardGenTest {
    @Test
    public void testDirectGen() {
        SketchGen<Long, LongList> sGen = new ItemDictCompressorGen(
                new CoopFreqCompressor(0)
        );
        BoardGen<Long, LongList> bGen = new BoardGen<>(sGen);
        FastList<LongList> segments = FastList.newListWith(
                LongArrayList.newListWith(5, 5, 5, 1, 2),
                LongArrayList.newListWith(5, 5, 5, 1, 2)
        );
        FastList<LongList> dims = FastList.newListWith(
                LongArrayList.newListWith(0),
                LongArrayList.newListWith(1)
        );
        IntList sizes = IntArrayList.newListWith(2, 2);
        DoubleList bias = DoubleArrayList.newWithNValues(2, 0.0);
        SketchBoard<Long> board = bGen.generate(
                segments,
                dims,
                sizes,
                bias
        );

        LinearFreqAccProcessor qp = new LinearFreqAccProcessor(
                new ExactFreqAccumulator()
        );
        qp.setRange(0, 2);
        FastList<Long> xToTrack = FastList.newListWith(5L, 1L, 2L);
        FastList<Double> xResults = qp.query(board, xToTrack);
//        System.out.println(xResults);
        assertEquals(6.0, xResults.get(0), 1e-10);
        double sum = xResults.sumOfDouble((x) -> x);
        assertEquals(9.0, sum, 1e-10);
    }
}