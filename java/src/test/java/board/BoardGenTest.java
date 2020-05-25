package board;

import board.planner.LinearFreqPlanner;
import board.query.LinearAccProcessor;
import board.query.LinearQueryProcessor;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.list.primitive.DoubleList;
import org.eclipse.collections.api.list.primitive.IntList;
import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;
import org.eclipse.collections.impl.list.mutable.primitive.LongArrayList;
import org.junit.Test;
import runner.factory.FreqSketchGenFactory;
import summary.compressor.freq.TruncationFreqCompressor;
import summary.gen.DyadicItemDictCompressorGen;
import summary.gen.SketchGen;
import summary.gen.ItemDictCompressorGen;
import summary.accumulator.MapFreqAccumulator;
import summary.compressor.freq.CoopFreqCompressor;
import tech.tablesaw.api.ColumnType;
import tech.tablesaw.api.Table;
import tech.tablesaw.io.csv.CsvReadOptions;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

public class BoardGenTest {
    @Test
    public void testFreqPlannedGen() throws IOException {
        ColumnType[] types = {ColumnType.LONG};
        Table t = Table.read().usingOptions(CsvReadOptions
                .builder("src/test/resources/small.csv")
                .columnTypes(types));
        LinearFreqPlanner planner = new LinearFreqPlanner();
        int nSegments = 4;
        planner.plan(
                t, "x", 2,
                Maps.mutable.of("num_segments",nSegments)
        );

        FreqSketchGenFactory factory = new FreqSketchGenFactory();
        List<String> sketchNames = Lists.mutable.of(
                "cooperative",
                "dyadic_truncation"
        );
        int nSummaries = sketchNames.size();
        List<Long> xToTrack = Lists.mutable.of(1L,2L,3L);

        for (int trialIdx = 0; trialIdx < nSummaries; trialIdx++) {
            String curSketchName = sketchNames.get(trialIdx);
            SketchGen<Long, LongList> sGen = factory.getSketchGen(curSketchName, xToTrack, nSegments);
            BoardGen<Long, LongList> bGen = new BoardGen<>(sGen);
            StoryBoard<Long> board = bGen.generate(
                    planner.getSegments(),
                    planner.getDimensions(),
                    planner.getSizes(),
                    planner.getBiases()
            );
            LinearQueryProcessor<Long> qp = factory.getLinearQueryProcessor(
                    curSketchName,
                    nSegments
            );

            qp.setRange(0, 3);
            DoubleList xResults = qp.query(board, xToTrack);

            assertEquals(15.0, xResults.get(2), 1e-10);
//            System.out.println(curSketchName+": "+xResults);
            double sum = xResults.sum();
            assertEquals(30.0, sum, 10);
        }
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

        LinearQueryProcessor<Long> qp = new LinearAccProcessor<Long, LongList>(
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