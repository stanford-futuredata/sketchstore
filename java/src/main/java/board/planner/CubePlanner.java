package board.planner;

import org.eclipse.collections.api.PrimitiveIterable;
import org.eclipse.collections.api.list.primitive.DoubleList;
import org.eclipse.collections.api.list.primitive.IntList;
import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.impl.factory.primitive.LongLists;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;
import org.eclipse.collections.impl.list.mutable.primitive.LongArrayList;
import tech.tablesaw.api.LongColumn;
import tech.tablesaw.api.Table;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public abstract class CubePlanner<TL extends PrimitiveIterable> implements Planner<TL> {
    public List<String> dimensionCols;
    public FastList<TL> segmentValues;
    public FastList<LongList> segmentDimensions;

    public abstract TL getColumnRange(Table data, String metricCol, IntList idxs);

    @Override
    public void plan(
            Table t, String metricCol
    ) {
        Table data = t.sortOn(dimensionCols.toArray(new String[0]));
        int nRows = t.rowCount();

        LongColumn[] dimCols = new LongColumn[dimensionCols.size()];
        for (int i = 0; i < dimensionCols.size(); i++) {
            dimCols[i] = data.longColumn(dimensionCols.get(i));
        }
        segmentValues = new FastList<>();
        segmentDimensions = new FastList<>();

        IntArrayList curSegmentIdxs = new IntArrayList();
        long[] curSegmentDimensions = null;
        long[] newRowDimensions = new long[dimCols.length];
        for (int rowIdx = 0; rowIdx < nRows; rowIdx++) {
            for (int j = 0; j < dimCols.length; j++) {
                newRowDimensions[j] = dimCols[j].getLong(rowIdx);
            }
            boolean sameAsPrevious = false;
            if (rowIdx > 0) {
                sameAsPrevious = Arrays.equals(newRowDimensions, curSegmentDimensions);
            }
            if (!sameAsPrevious) {
                if (curSegmentIdxs.size() > 0) {
                    segmentValues.add(getColumnRange(data, metricCol, curSegmentIdxs));
                    segmentDimensions.add(new LongArrayList(curSegmentDimensions));
                    curSegmentIdxs.clear();
                }
                curSegmentDimensions = newRowDimensions;
                newRowDimensions = new long[dimCols.length];
            }
            curSegmentIdxs.add(rowIdx);
        }
        segmentValues.add(getColumnRange(data, metricCol, curSegmentIdxs));
        segmentDimensions.add(new LongArrayList(curSegmentDimensions));
        curSegmentIdxs = null;
    }

    @Override
    public FastList<TL> getSegments() {
        return segmentValues;
    }

    @Override
    public FastList<LongList> getDimensions() {
        return segmentDimensions;
    }

    @Override
    public void setParams(Map<String, Object> params) {
        this.dimensionCols = (List<String>) params.get("dimension_cols");
    }

}
