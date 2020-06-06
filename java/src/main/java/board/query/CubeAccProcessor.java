package board.query;

import board.StoryBoard;
import org.eclipse.collections.api.PrimitiveIterable;
import org.eclipse.collections.api.list.primitive.DoubleList;
import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;
import org.eclipse.collections.impl.list.mutable.primitive.LongArrayList;
import summary.Sketch;
import summary.accumulator.Accumulator;

import java.util.Arrays;
import java.util.List;

public class CubeAccProcessor<T, TL extends PrimitiveIterable> implements
        CubeQueryProcessor<T> {
    public long[] dimensionFilters;
    public Accumulator<T, TL> acc;
    public int span;
    public double total;

    public CubeAccProcessor(
            Accumulator<T, TL> acc
    ) {
        this.acc = acc;
        this.span = 0;
    }

    @Override
    public DoubleList query(
            StoryBoard<T> board,
            List<T> xToTrack
    ) {
        acc.reset();
        span=0;
        total=0;
        FastList<Sketch<T>> sketchCol = board.sketchCol;
        int nRows = sketchCol.size();
        int nDims = dimensionFilters.length;

        int[] failedConditions = new int[nRows];
        for (int j = 0; j < nDims; j++) {
            long curFilterValue = dimensionFilters[j];
            if (curFilterValue >= 0) {
                LongArrayList curDimCol = board.dimensionCols.get(j);
                for (int i = 0; i < nRows; i++) {
                    if (curDimCol.get(i) != curFilterValue) {
                        failedConditions[i]++;
                    }
                }
            }
        }

        DoubleArrayList totalCol = board.totalCol;
        for (int i = 0; i < nRows; i++) {
            if (failedConditions[i] == 0) {
                span++;
                Sketch<T> curSketch = sketchCol.get(i);
                acc.addSketch(curSketch);
                total += totalCol.get(i);
            }
        }
        return acc.estimate(xToTrack);
    }

    @Override
    public double total() {
        return total;
    }

    @Override
    public int span() {
        return span;
    }

    @Override
    public void setDimensions(LongList dims) {
        dimensionFilters = dims.toArray();
    }
}
