package board.planner;

import org.eclipse.collections.api.list.primitive.DoubleList;
import org.eclipse.collections.api.list.primitive.IntList;
import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.impl.factory.primitive.LongLists;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;
import org.eclipse.collections.impl.list.mutable.primitive.LongArrayList;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.LongColumn;
import tech.tablesaw.api.Table;

import java.util.Map;

public class LinearQuantilePlanner implements Planner<DoubleList> {
    public int numSegments;
    public int size;

    public String metricCol;
    public Table data;

    @Override
    public void plan(
            Table t, String metricCol, int size, Map<String, Object> params
    ) {
        data = t;
        this.metricCol = metricCol;
        this.numSegments = (Integer)params.get("num_segments");
        this.size = size;
    }

    @Override
    public FastList<DoubleList> getSegments() {
        DoubleColumn col = (DoubleColumn)data.column(metricCol);
        FastList<DoubleList> segments = new FastList<>(numSegments);
        int n = col.size();
        int segLength = n / numSegments;
        for (int i = 0; i < numSegments; i++) {
            int startIdx = i*segLength;
            int endIdx = (i+1)*segLength;
            if (i == numSegments - 1) {
                endIdx = n;
            }
            DoubleArrayList curSegment = new DoubleArrayList(endIdx-startIdx);
            for (int curIdx = startIdx; curIdx < endIdx; curIdx++) {
                curSegment.add(col.getDouble(curIdx));
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
    public IntList getSpaces() {
        return IntArrayList.newWithNValues(numSegments, size);
    }

    @Override
    public DoubleList getBiases() {
        int n = data.rowCount();
        return DoubleArrayList.newWithNValues(numSegments, 0.0);
    }
}
