package board.planner;

import org.eclipse.collections.api.PrimitiveIterable;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.list.primitive.IntList;
import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.junit.Test;
import tech.tablesaw.api.ColumnType;
import tech.tablesaw.api.Table;
import tech.tablesaw.io.csv.CsvReadOptions;

import java.io.IOException;

import static org.junit.Assert.*;

public class CubeFreqPlannerTest {    @Test
public void simpleTest() throws IOException {
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

    FastList<LongList> segments = planner.getSegments();
    FastList<LongList> dims = planner.getDimensions();
    IntList sizes = planner.getSizes();
    assertEquals(3, segments.size());
    assertEquals(3, dims.size());
    assertEquals(8, segments.get(0).size());
    assertTrue(sizes.sum() <= 10);

    long totalLen = segments.collectInt(PrimitiveIterable::size).sum();
    assertEquals(t.rowCount(), totalLen);
}
}