package board.planner;

import board.query.LinearQueryProcessor;
import org.eclipse.collections.api.PrimitiveIterable;
import org.eclipse.collections.api.list.primitive.DoubleList;
import org.eclipse.collections.api.list.primitive.IntList;
import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;

import java.util.Map;

public class LinearOptimizer<TL extends PrimitiveIterable> implements PlanOptimizer<TL> {
    IntArrayList spaces;
    DoubleArrayList biases;

    public LinearOptimizer() {
        return;
    }

    @Override
    public IntList getSpaces() {
        return spaces;
    }

    @Override
    public DoubleList getBiases() {
        return biases;
    }

    @Override
    public void optimizePlan(FastList<TL> segments, FastList<LongList> segDimensions, int size) {
        int nSegments = segments.size();
        spaces = new IntArrayList(nSegments);
        biases = new DoubleArrayList(nSegments);

        for (int i = 0; i < nSegments; i++) {
            spaces.add(size);
            biases.add(0);
        }
    }

    @Override
    public void setParams(Map<String, Object> params) {
    }
}
