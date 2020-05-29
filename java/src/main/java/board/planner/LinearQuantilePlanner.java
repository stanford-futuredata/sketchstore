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
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.LongColumn;
import tech.tablesaw.api.Table;

import java.util.List;
import java.util.Map;

public class LinearQuantilePlanner implements Planner<DoubleList> {
    public int numSegments = 0;
    public int size;

    FastList<LongList> segmentDimensions;
    FastList<DoubleList> segmentValues;

    @Override
    public void plan(
            Table t, String metricCol
    ) {
        Table data = t;
        DoubleColumn col = (DoubleColumn)data.column(metricCol);

        segmentValues = new FastList<>(numSegments);
        segmentDimensions = new FastList<>(numSegments);

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
            segmentValues.add(curSegment);
            segmentDimensions.add(LongLists.immutable.of(i));
        }

    }

    @Override
    public FastList<DoubleList> getSegments() {
        return segmentValues;
    }

    @Override
    public FastList<LongList> getDimensions() {
        return segmentDimensions;
    }

    @Override
    public void setParams(Map<String, Object> params) {
        this.numSegments = (Integer)params.get("num_segments");
    }
}
