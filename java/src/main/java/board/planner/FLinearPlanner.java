package board.planner;

import org.eclipse.collections.api.list.primitive.DoubleList;
import org.eclipse.collections.api.list.primitive.IntList;
import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.impl.list.mutable.FastList;
import tech.tablesaw.api.LongColumn;
import tech.tablesaw.api.Table;

public class FLinearPlanner implements Planner<LongList> {
    public int numSegments;

    public String metricCol;
    public Table data;

    public FLinearPlanner(
            int numSegments
    ) {
        this.numSegments = numSegments;
    }

    @Override
    public void plan(
            Table t, String metricCol, FastList<String> dimCols
    ) {
        data = t;
        this.metricCol = metricCol;
    }

    @Override
    public FastList<LongList> getSegments() {
        LongColumn col = (LongColumn)data.column(metricCol);
        FastList<LongList> segments = new FastList<>(numSegments);
        for (int i = 0; i < numSegments; i++) {

        }
        return segments;
    }

    @Override
    public FastList<LongList> getDimensions() {
        return null;
    }

    @Override
    public IntList getSizes() {
        return null;
    }

    @Override
    public DoubleList getBiases() {
        return null;
    }
}
