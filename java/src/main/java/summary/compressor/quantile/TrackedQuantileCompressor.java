package summary.compressor.quantile;

import org.eclipse.collections.api.list.primitive.DoubleList;
import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;
import summary.CounterDoubleSketch;

import java.util.List;

public class TrackedQuantileCompressor implements SeqCDFCompressor {
    public DoubleArrayList xToTrack;

    public TrackedQuantileCompressor(List<Double> xTracked) {
        xToTrack = new DoubleArrayList(xTracked.size());
        for (double x : xTracked) {
            if (xToTrack.isEmpty() || x != xToTrack.getLast()) {
                xToTrack.add(x);
            }
        }
    }

    @Override
    public CounterDoubleSketch compress(DoubleList xs, int size) {
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
        return new CounterDoubleSketch(
                xToTrack.toArray(),
                weights
        );
    }
}
