package board.planner;

import org.eclipse.collections.api.list.primitive.DoubleList;
import org.eclipse.collections.api.list.primitive.ImmutableLongList;
import org.eclipse.collections.api.list.primitive.IntList;
import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.impl.factory.primitive.IntLists;
import org.eclipse.collections.impl.factory.primitive.LongLists;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;
import org.eclipse.collections.impl.list.mutable.primitive.LongArrayList;
import org.eclipse.collections.impl.list.mutable.primitive.UnmodifiableLongList;
import tech.tablesaw.api.LongColumn;
import tech.tablesaw.api.Table;

import java.util.List;

public class LinearFreqPlanner implements Planner<LongList> {
    public int numSegments;
    public int size;

    public String metricCol;
    public Table data;

    public LinearFreqPlanner(
            int numSegments, int size
    ) {
        this.numSegments = numSegments;
        this.size = size;
    }

    @Override
    public void plan(
            Table t, String metricCol, List<String> dimCols
    ) {
        data = t;
        this.metricCol = metricCol;
    }

    @Override
    public FastList<LongList> getSegments() {
        LongColumn col = (LongColumn)data.column(metricCol);
        FastList<LongList> segments = new FastList<>(numSegments);
        int n = col.size();
        int segLength = n / numSegments;
        for (int i = 0; i < numSegments; i++) {
            int startIdx = i*segLength;
            int endIdx = (i+1)*segLength;
            if (i == numSegments - 1) {
                endIdx = n;
            }
            LongArrayList curSegment = new LongArrayList(endIdx-startIdx);
            for (int curIdx = startIdx; curIdx < endIdx; curIdx++) {
                curSegment.add(col.getLong(curIdx));
            }
            segments.add(curSegment);
        }
        return segments;
    }

    @Override
    public FastList<LongList> getDimensions() {
        int n = numSegments;
        FastList<LongList> dims = new FastList<>(n);
        for (int i = 0; i < n; i++) {
            dims.add(LongLists.immutable.of(i));
        }
        return dims;
    }

    @Override
    public IntList getSizes() {
        return IntArrayList.newWithNValues(numSegments, size);
    }

    @Override
    public DoubleList getBiases() {
        int n = data.rowCount();
        return DoubleArrayList.newWithNValues(numSegments, 0.0);
    }
}
