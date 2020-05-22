package board.query;

import board.StoryBoard;
import org.eclipse.collections.api.PrimitiveIterable;
import org.eclipse.collections.api.list.primitive.DoubleList;
import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.api.tuple.primitive.IntIntPair;
import org.eclipse.collections.api.tuple.primitive.LongIntPair;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.collections.impl.list.mutable.primitive.LongArrayList;
import org.eclipse.collections.impl.tuple.primitive.PrimitiveTuples;
import summary.Sketch;
import summary.accumulator.Accumulator;

import java.util.List;

public class DyadicLinearAccProcessor<T, TL extends PrimitiveIterable> implements
        LinearSelector, QueryProcessor<T> {
    public int startIdx=0, endIdx=0;
    public Accumulator<T, TL> acc;
    public int base = 2;
    public int maxHeight;

    public DyadicLinearAccProcessor(
            Accumulator<T, TL> acc, int maxHeight
    ) {
        this.acc = acc;
        this.maxHeight = maxHeight;
    }

    public FastList<LongArrayList> getDyadicBreakdown(int startIdx, int endIdx) {
        FastList<LongArrayList> tierIndices = new FastList<>(maxHeight);
        for (int i = 0; i < maxHeight; i++){
            tierIndices.add(new LongArrayList());
        }
        long curTierLength = 1;
        while (startIdx < endIdx) {
            int tierIdx=0;
            for (tierIdx = 0; tierIdx < maxHeight; tierIdx++) {
                if (startIdx % (curTierLength * base) == 0) {
                    curTierLength *= base;
                }
            }
            tierIndices.get(tierIdx).add(startIdx);
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
        LongList tValues = board.dimensionCols.get(0);
        FastList<LongArrayList> tierIndices = getDyadicBreakdown(startIdx, endIdx);
        for (int i = 0; i < tValues.size(); i++) {
            long curT = tValues.get(i);
            int curTier = board.tierCol.get(i);
            LongList curTierLocations = tierIndices.get(curTier);
            boolean matched = curTierLocations.anySatisfy((long x) -> (x == curT));
            if (matched) {
                acc.addSketch(board.sketchCol.get(i));
            }
        }
        return acc.estimate(xToTrack);
    }

    @Override
    public double total(StoryBoard<T> board) {
        double result = 0;
        LongList tValues = board.dimensionCols.get(0);
        for (int i = 0; i < tValues.size(); i++) {
            if (board.tierCol.get(i) > 0) {
                continue;
            }
            long curT = tValues.get(i);
            if (curT >= startIdx && curT < endIdx) {
                result += board.totalCol.get(i);
            }
        }
        return result;
    }

    @Override
    public void setRange(int startIdx, int endIdx) {
        this.startIdx = startIdx;
        this.endIdx = endIdx;
    }
}
