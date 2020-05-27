package board.query;

import board.BoardGen;
import board.StoryBoard;
import board.planner.CubeFreqPlanner;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.list.primitive.DoubleList;
import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.factory.primitive.LongLists;
import org.junit.Test;
import runner.factory.FreqSketchGenFactory;
import summary.gen.SketchGen;
import tech.tablesaw.api.ColumnType;
import tech.tablesaw.api.Table;
import tech.tablesaw.io.csv.CsvReadOptions;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

public class CubeAccProcessorTest {
    @Test
    public void testEnd() throws IOException {
        ColumnType[] types = {ColumnType.LONG, ColumnType.LONG, ColumnType.LONG};
        Table t = Table.read().usingOptions(CsvReadOptions
                .builder("src/test/resources/small_cube.csv")
                .columnTypes(types));

        CubeFreqPlanner planner = new CubeFreqPlanner();
        planner.plan(
                t, "x", 10,
                Maps.mutable.of(
                        "dimension_cols",
                        Lists.mutable.of("d1", "d2"),
                        "workload_prob",
                        .2
                )
        );

        List<Long> xToTrack = org.eclipse.collections.api.factory.Lists.mutable.of(1L,2L,3L);
        String curSketchName = "top_values";
        FreqSketchGenFactory factory = new FreqSketchGenFactory();
        SketchGen<Long, LongList> sGen = factory.getSketchGen(curSketchName, xToTrack, 0);
        BoardGen<Long, LongList> bGen = new BoardGen<>(sGen);
        StoryBoard<Long> board = bGen.generate(
                planner.getSegments(),
                planner.getDimensions(),
                planner.getSpaces(),
                planner.getBiases()
        );

        CubeQueryProcessor<Long> qp = factory.getCubeQueryProcessor(curSketchName);
        qp.setDimensions(LongLists.mutable.of(1, -1));
        DoubleList results = qp.query(board, xToTrack);
        assertEquals(xToTrack.size(), results.size());
        assertEquals(6.0, results.get(0), 1e-10);
    }

}