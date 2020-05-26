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

import java.util.List;

public class CubeAccProcessor<T, TL extends PrimitiveIterable> implements
        CubeQueryProcessor<T> {
    public long[] dimensionFilters;
    public Accumulator<T, TL> acc;

    public CubeAccProcessor(
            Accumulator<T, TL> acc
    ) {
        this.acc = acc;
    }

    @Override
    public DoubleList query(
            StoryBoard<T> board,
            List<T> xToTrack
    ) {
        acc.reset();
        FastList<Sketch<T>> sketchCol = board.sketchCol;
        int nRows = sketchCol.size();
        int nDims = dimensionFilters.length;
        for (int i = 0; i < nRows; i++) {
            boolean matches = true;
            for (int j = 0; j < nDims; j++) {
                long curFilterValue = dimensionFilters[j];
                if (curFilterValue >= 0 && curFilterValue != board.dimensionCols.get(j).get(i)) {
                    matches = false;
                    break;
                }
            }
            if (matches) {
                acc.addSketch(sketchCol.get(i));
            }
        }
        return acc.estimate(xToTrack);
    }

    @Override
    public double total(StoryBoard<T> board) {
        double result = 0;
        DoubleArrayList totalCol = board.totalCol;
        int nRows = totalCol.size();
        int nDims = dimensionFilters.length;
        for (int i = 0; i < nRows; i++) {
            boolean matches = true;
            for (int j = 0; j < nDims; j++) {
                long curFilterValue = dimensionFilters[j];
                if (curFilterValue >= 0 && curFilterValue != board.dimensionCols.get(j).get(i)) {
                    matches = false;
                    break;
                }
            }
            if (matches) {
                result += totalCol.get(i);
            }
        }

        return result;
    }

    @Override
    public void setDimensions(LongList dims) {
        dimensionFilters = dims.toArray();
    }
}
