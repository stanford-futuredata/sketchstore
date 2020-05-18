package board.planner;

import board.BoardGen;
import org.eclipse.collections.api.PrimitiveIterable;
import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.junit.Test;
import summary.gen.ItemDictCompressorGen;
import summary.gen.SketchGen;
import summary.compressor.freq.CoopFreqCompressor;
import tech.tablesaw.api.ColumnType;
import tech.tablesaw.api.Table;
import tech.tablesaw.io.csv.CsvReadOptions;

import java.io.IOException;

import static org.junit.Assert.*;

public class LinearFreqPlannerTest {
    @Test
    public void simpleTest() throws IOException {
        ColumnType[] types = {ColumnType.LONG};
        Table t = Table.read().usingOptions(CsvReadOptions
                .builder("src/test/resources/small.csv")
                .columnTypes(types));

        int numSegments = 4;
        LinearFreqPlanner planner = new LinearFreqPlanner();
        planner.plan(
                t, "x", numSegments, 2
        );

        SketchGen<Long, LongList> sGen = new ItemDictCompressorGen(
                new CoopFreqCompressor(0)
        );
        BoardGen<Long, LongList> bGen = new BoardGen<>(sGen);
        FastList<LongList> segments = planner.getSegments();
        FastList<LongList> dims = planner.getDimensions();

        long totalLen = segments.collectInt(PrimitiveIterable::size).sum();
        assertEquals(t.rowCount(), totalLen);
        assertEquals(numSegments, segments.size());
    }
}