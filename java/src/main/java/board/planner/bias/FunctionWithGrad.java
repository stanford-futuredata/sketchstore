package board.planner.bias;

public interface FunctionWithGrad {
    void compute(double[] x);
    int dim();
    double getValue();
    double[] getGradient();
}
