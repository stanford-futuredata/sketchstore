package board.query;

import board.SketchBoard;
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
    public FastList<Double> query(
            SketchBoard<Long> board,
            List<Long> xToTrack
    ) {
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
    public void setRange(int startIdx, int endIdx) {
        this.startIdx = startIdx;
        this.endIdx = endIdx;
    }
}
