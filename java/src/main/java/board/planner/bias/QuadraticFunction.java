package board.planner.bias;

public class QuadraticFunction implements FunctionWithGrad {
    double value;
    double[] grad;
    double[] origin;

    public QuadraticFunction(double[] origin) {
        this.origin = origin;
        int nDim = origin.length;
        grad = new double[nDim];
    }

    @Override
    public void compute(double[] x) {
        value = 0;
        for (int i = 0; i < x.length; i++) {
            double xDelta = x[i] - origin[i];
            value += xDelta*xDelta;
            grad[i] = 2*xDelta;
        }
    }

    @Override
    public int dim() {
        return origin.length;
    }

    @Override
    public double getValue() {
        return value;
    }

    @Override
    public double[] getGradient() {
        return grad;
    }
}
