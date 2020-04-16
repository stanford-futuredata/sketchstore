package board.planner;

import board.BoardGen;
import board.SketchBoard;
import org.eclipse.collections.api.PrimitiveIterable;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.primitive.IntList;
import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.junit.Test;
import summary.BoardSketch;
import summary.ItemDictCompressorGen;
import summary.SketchGen;
import summary.compressor.CoopFreqCompressor;
import tech.tablesaw.api.ColumnType;
import tech.tablesaw.api.Table;
import tech.tablesaw.io.csv.CsvReadOptions;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

public class LinearFreqPlannerTest {
    @Test
    public void simpleTest() throws IOException {
        ColumnType[] types = {ColumnType.LONG};
        Table t = Table.read().usingOptions(CsvReadOptions
                .builder("src/test/resources/small.csv")
                .columnTypes(types));

        int numSegments = 4;
        LinearFreqPlanner planner = new LinearFreqPlanner(
                numSegments,
                2
        );
        List<String> dimCols = Lists.fixedSize.<String>empty();
        planner.plan(
                t, "x", dimCols
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