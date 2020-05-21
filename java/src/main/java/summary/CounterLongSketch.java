package summary;

public class CounterLongSketch implements Sketch<Long> {
    public long[] vals;
    public double[] weights;
    public CounterLongSketch(long[] vals, double[] weights) {
        this.vals = vals;
        this.weights = weights;
    }

    @Override
    public String name() {
        return "counter_long";
    }

    @Override
    public Sketch<Long> merge(Sketch<Long> otherArg) {
        throw new RuntimeException("Not Implemented");
    }

    @Override
    public double estimate(Long xQuery) {
        long xQueryVal = xQuery;
        int n = vals.length;
        for (int i = 0; i < n; i++) {
            double x = vals[i];
            if (x == xQueryVal) {
                return weights[i];
            }
        }
        return 0;
    }
}
