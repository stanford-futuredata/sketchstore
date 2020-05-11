package board.query;

import board.StoryBoard;
import org.eclipse.collections.api.list.primitive.DoubleList;
import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.impl.list.mutable.FastList;
import summary.DictSketch;
import summary.accumulator.FreqAccumulator;

import java.util.List;

public class LinearFreqAccProcessor implements
        LinearSelector, QueryProcessor<Long> {
    public int startIdx=0, endIdx=0;
    public FreqAccumulator acc;

    public LinearFreqAccProcessor(
            FreqAccumulator acc
    ) {
        this.acc = acc;
    }

    @Override
    public DoubleList query(
            StoryBoard<Long> board,
            List<Long> xToTrack
    ) {
        acc.reset();
        LongList tValues = board.dimensionCols.get(0);
        for (int i = 0; i < tValues.size(); i++) {
            long curT = tValues.get(i);
            if (curT >= startIdx && curT < endIdx) {
                DictSketch curSketch = (DictSketch)board.sketchCol.get(i);
                acc.add(curSketch.vals);
            }
        }
        return acc.estimate(xToTrack);
    }

    @Override
    public double total(StoryBoard<Long> board) {
        double result = 0;
        LongList tValues = board.dimensionCols.get(0);
        for (int i = 0; i < tValues.size(); i++) {
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
