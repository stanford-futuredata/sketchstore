package board.planner.bias;

import org.eclipse.collections.api.PrimitiveIterable;
import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.impl.list.mutable.FastList;

public interface BiasOptimizer<TL extends PrimitiveIterable> {
    double[] getBias();
    void compute(int[] segmentSpaces, FastList<TL> segmentValues);
}
