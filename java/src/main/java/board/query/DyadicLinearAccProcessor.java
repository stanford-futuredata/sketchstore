package board.query;

import board.StoryBoard;
import org.eclipse.collections.api.PrimitiveIterable;
import org.eclipse.collections.api.list.primitive.DoubleList;
import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;
import org.eclipse.collections.impl.list.mutable.primitive.LongArrayList;
import summary.accumulator.Accumulator;

import java.util.List;

public class DyadicLinearAccProcessor<T, TL extends PrimitiveIterable> implements
        LinearQueryProcessor<T> {
    public int startIdx=0, endIdx=0;
    public Accumulator<T, TL> acc;
    public int base = 2;
    public int maxHeight;
    public int accumulatorSize;
    public int span;
    public double total;

    public DyadicLinearAccProcessor(
            Accumulator<T, TL> acc, int maxHeight, int accumulatorSize
    ) {
        this.acc = acc;
        this.maxHeight = maxHeight;
        this.accumulatorSize = accumulatorSize;
        this.span = 0;
        this.total = 0;
    }

    public FastList<LongArrayList> getDyadicBreakdown(int startIdx, int endIdx) {
        FastList<LongArrayList> tierIndices = new FastList<>(maxHeight);
        for (int i = 0; i < maxHeight; i++){
            tierIndices.add(new LongArrayList());
        }
        while (startIdx < endIdx) {
            int tierIdx = 0;
            long curTierLength = 1;
            while (
                    (tierIdx+1 < maxHeight)
                            && (startIdx % (curTierLength*base) == 0)
                            && (startIdx + (curTierLength*base) < endIdx)
            ) {
                tierIdx++;
                curTierLength *= base;
            }
            tierIndices.get(tierIdx).add(startIdx+curTierLength-1);
            startIdx += curTierLength;
        }
        return tierIndices;
    }

    @Override
    public DoubleList query(
            StoryBoard<T> board,
            List<T> xToTrack
    ) {
        acc.reset();
        span=0;
        total = 0;
        LongList tValues = board.dimensionCols.get(0);
        DoubleArrayList totalCol = board.totalCol;

        FastList<LongArrayList> tierIndices = getDyadicBreakdown(startIdx, endIdx);
        for (int i = 0; i < tValues.size(); i++) {
            long curT = tValues.get(i);
            int curTier = board.tierCol.get(i);
            LongList curTierLocations = tierIndices.get(curTier);
            boolean matched = curTierLocations.anySatisfy((long x) -> (x == curT));
            if (matched) {
                acc.addSketch(board.sketchCol.get(i));
                span++;
                if (accumulatorSize > 0) {
                    acc.compress(accumulatorSize);
                }
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
    public void setRange(int startIdx, int endIdx) {
        this.startIdx = startIdx;
        this.endIdx = endIdx;
    }
}
