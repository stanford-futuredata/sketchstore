package board.planner;

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

public class CubeFreqPlanner implements Planner<LongList> {
    public int size;
    public String metricCol;
    public Table data;

    public List<String> dimensionCols;
    public double workloadProb;

    public FastList<LongList> segmentValues;
    public FastList<LongList> segmentDimensions;
    public IntList segmentSizes;
    public IntList segmentBiases;

    @Override
    public void plan(
            Table t, String metricCol, int size, Map<String, Object> params
    ) {
        this.metricCol = metricCol;
        this.size = size;
        this.dimensionCols = (List<String>) params.get("dimension_cols");
        this.data = t.sortOn(dimensionCols.toArray(new String[0]));
        this.workloadProb = (Double) params.get("workload_prob");

        LongColumn col = (LongColumn) data.column(metricCol);
        int n = col.size();
        LongColumn[] dimCols = new LongColumn[dimensionCols.size()];
        for (int i = 0; i < dimensionCols.size(); i++) {
            dimCols[i] = data.longColumn(dimensionCols.get(i));
        }
        segmentValues = new FastList<>();
        segmentDimensions = new FastList<>();

        LongArrayList curSegment = new LongArrayList();
        long[] curSegmentDimensions = null;
        long[] newRowDimensions = new long[dimCols.length];
        for (int rowIdx = 0; rowIdx < n; rowIdx++) {
            for (int j = 0; j < dimCols.length; j++) {
                newRowDimensions[j] = dimCols[j].getLong(rowIdx);
            }
            boolean sameAsPrevious = false;
            if (rowIdx > 0) {
                sameAsPrevious = Arrays.equals(newRowDimensions, curSegmentDimensions);
            }
            if (!sameAsPrevious) {
                if (curSegment.size() > 0) {
                    segmentValues.add(curSegment);
                    segmentDimensions.add(new LongArrayList(curSegmentDimensions));
                    curSegment = new LongArrayList();
                }
                curSegmentDimensions = newRowDimensions;
                newRowDimensions = new long[dimCols.length];
            }
            curSegment.add(col.getLong(rowIdx));
        }
        segmentValues.add(curSegment);
        segmentDimensions.add(new LongArrayList(curSegmentDimensions));
        curSegment = null;
    }

    @Override
    public FastList<LongList> getSegments() {
        return segmentValues;
    }

    @Override
    public FastList<LongList> getDimensions() {
        return segmentDimensions;
    }

    @Override
    public IntList getSizes() {
        int numSegments = segmentValues.size();
        int sizePerSegment = size / numSegments;
        return IntArrayList.newWithNValues(numSegments, sizePerSegment);
    }

    @Override
    public DoubleList getBiases() {
        int numSegments = segmentValues.size();
        return DoubleArrayList.newWithNValues(numSegments, 0.0);
    }
}
