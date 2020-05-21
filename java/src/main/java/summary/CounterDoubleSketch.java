package summary;

public class CounterDoubleSketch implements Sketch<Double> {
    public double[] values;
    public double[] weights;

    public CounterDoubleSketch(double[] values, double[] weights) {
        this.values = values;
        this.weights = weights;
    }

    @Override
    public String toString() {
        int n = values.length;
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < n; i++) {
            out.append(String.valueOf(values[i]));
            out.append(":");
            out.append(String.valueOf(weights[i]));
            out.append(" ");
        }
        return out.toString();
    }

    @Override
    public String name() {
        return "counter_double";
    }

    @Override
    public Sketch<Double> merge(Sketch<Double> otherArg) {
        throw new RuntimeException("Not Implemented");
    }

    @Override
    public double estimate(Double xquery) {
        double xQVal = xquery;
        double total = 0.0;
        for (int i = 0; i < values.length; i++) {
            double x = values[i];
            if (x > xQVal) {
                break;
            }
            total += weights[i];
        }
        return total;
    }
}
