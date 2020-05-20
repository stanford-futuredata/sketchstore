package board;

import board.planner.LinearFreqPlanner;
import board.query.LinearAccProcessor;
import org.eclipse.collections.api.list.primitive.DoubleList;
import org.eclipse.collections.api.list.primitive.IntList;
import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;
import org.eclipse.collections.impl.list.mutable.primitive.LongArrayList;
import org.junit.Test;
import summary.gen.SketchGen;
import summary.gen.ItemDictCompressorGen;
import summary.accumulator.MapFreqAccumulator;
import summary.compressor.freq.CoopFreqCompressor;
import tech.tablesaw.api.ColumnType;
import tech.tablesaw.api.Table;
import tech.tablesaw.io.csv.CsvReadOptions;

import java.io.IOException;

import static org.junit.Assert.*;

public class BoardGenTest {
    @Test
    public void testPlannedGen() throws IOException {
        ColumnType[] types = {ColumnType.LONG};
        Table t = Table.read().usingOptions(CsvReadOptions
                .builder("src/test/resources/small.csv")
                .columnTypes(types));

        LinearFreqPlanner planner = new LinearFreqPlanner();
        planner.plan(
                t, "x", 4, 2
        );

        SketchGen<Long, LongList> sGen = new ItemDictCompressorGen(
                new CoopFreqCompressor(0)
        );
        BoardGen<Long, LongList> bGen = new BoardGen<>(sGen);
        StoryBoard<Long> board = bGen.generate(
                planner.getSegments(),
                planner.getDimensions(),
                planner.getSizes(),
                planner.getBiases()
        );

        LinearAccProcessor qp = new LinearAccProcessor(
                new MapFreqAccumulator()
        );
        qp.setRange(0, 2);
        FastList<Long> xToTrack = FastList.newListWith(1L, 2L, 3L);
        DoubleList xResults = qp.query(board, xToTrack);

        assertEquals(10.0, xResults.get(2), 1e-10);
        double sum = xResults.sum();
        assertEquals(17.0, sum, 1e-10);
    }

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
        StoryBoard<Long> board = bGen.generate(
                segments,
                dims,
                sizes,
                bias
        );

        LinearAccProcessor qp = new LinearAccProcessor(
                new MapFreqAccumulator()
        );
        qp.setRange(0, 2);
        FastList<Long> xToTrack = FastList.newListWith(5L, 1L, 2L);
        DoubleList xResults = qp.query(board, xToTrack);
//        System.out.println(xResults);
        assertEquals(6.0, xResults.get(0), 1e-10);
        double sum = xResults.sum();
        assertEquals(9.0, sum, 1e-10);
    }
}