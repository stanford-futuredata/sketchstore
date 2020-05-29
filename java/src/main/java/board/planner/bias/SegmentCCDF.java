package board.planner.bias;

import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.api.tuple.primitive.DoubleDoublePair;
import org.eclipse.collections.impl.map.mutable.primitive.DoubleLongHashMap;
import org.eclipse.collections.impl.map.mutable.primitive.LongLongHashMap;
import org.eclipse.collections.impl.tuple.primitive.PrimitiveTuples;

public class SegmentCCDF {
    long[] occCounts;
    // total weight of items that occur less than a count # of times
    long[] occCountFrequency;
    public SegmentCCDF(long[] occCounts, long[] occCountFrequency) {
        this.occCounts = occCounts;
        this.occCountFrequency = occCountFrequency;
    }
    public static SegmentCCDF fromItems(LongList xs) {
        int nItems = xs.size();
        DoubleLongHashMap xCounts = new DoubleLongHashMap();
        for (int i = 0; i < nItems; i++) {
            double curX = xs.get(i);
            xCounts.addToValue(curX, 1);
        }
        LongLongHashMap countCounts = new LongLongHashMap();
        xCounts.forEachValue((long count) -> {
            countCounts.addToValue(count, 1);
        });
        long[] curCounts = countCounts.keySet().toSortedArray();
        long[] curCountFrequencies = new long[curCounts.length];
        for (int i = 0; i < curCounts.length; i++) {
            curCountFrequencies[i] = countCounts.get(curCounts[i]);
        }
        return new SegmentCCDF(curCounts, curCountFrequencies);
    }
    public DoubleDoublePair total(double bias) {
        int n = occCounts.length;
        double adjTotal = 0;
        double adjTotalDeriv = 0;
        for (int i = 0;i < n; i++) {
            if (occCounts[i] > bias) {
                adjTotal += (occCounts[i]-bias) * occCountFrequency[i];
                adjTotalDeriv -= occCountFrequency[i];
            }
        }
        return PrimitiveTuples.pair(adjTotal, adjTotalDeriv);
    }
    @Override
    public String toString() {
        int n = occCounts.length;
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < n; i++) {
            out.append(String.valueOf(occCounts[i]));
            out.append(":");
            out.append(String.valueOf(occCountFrequency[i]));
            out.append(" ");
        }
        return out.toString();
    }
}
