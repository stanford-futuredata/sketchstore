package board.planner.bias;

import org.eclipse.collections.api.PrimitiveIterable;
import org.eclipse.collections.impl.list.mutable.FastList;

public class NopBiasOptimizer<TL extends PrimitiveIterable> implements BiasOptimizer<TL>{
    double[] bias;

    @Override
    public double[] getBias() {
        return bias;
    }

    @Override
    public void compute(int[] segmentSpaces, FastList<TL> segmentValues) {
        int nDims = segmentSpaces.length;
        bias = new double[nDims];
    }
}
