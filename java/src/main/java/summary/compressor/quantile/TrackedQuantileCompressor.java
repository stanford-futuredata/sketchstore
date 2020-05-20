package summary.compressor.quantile;

import org.apache.commons.math3.stat.Frequency;
import org.eclipse.collections.api.list.primitive.DoubleList;
import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;
import org.eclipse.collections.impl.map.mutable.primitive.DoubleDoubleHashMap;
import summary.CDFSketch;

public class TrackedQuantileCompressor implements SeqDictCompressor {
    public DoubleArrayList xToTrack;

    public TrackedQuantileCompressor(DoubleList xTracked) {
        double[] xValues = xTracked.toSortedArray();
        xToTrack = new DoubleArrayList(xTracked.size());
        for (double x : xValues) {
            if (xToTrack.isEmpty() || x != xToTrack.getLast()) {
                xToTrack.add(x);
            }
        }
    }

    @Override
    public CDFSketch compress(DoubleArrayList xs, int size) {
        double[] weights = new double[xToTrack.size()];
        int n = xs.size();
        int trackedIdx = 0;
        int nTracked = xToTrack.size();
        for (int i = 0; i < n; i++) {
            double curX = xs.get(i);
            while (trackedIdx < nTracked && xToTrack.get(trackedIdx) < curX) {
                trackedIdx++;
            }
            if (trackedIdx >= nTracked) {
                break;
            }
            weights[trackedIdx]++;
        }
        return CDFSketch.fromWeights(
                xToTrack,
                new DoubleArrayList(weights)
        );
    }
}
