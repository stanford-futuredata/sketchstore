package board.planner;

import org.eclipse.collections.api.PrimitiveIterable;
import org.eclipse.collections.api.list.primitive.DoubleList;
import org.eclipse.collections.api.list.primitive.IntList;
import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.impl.list.mutable.FastList;

import java.util.Map;

public interface PlanOptimizer<TL extends PrimitiveIterable> {
    IntList getSpaces();
    DoubleList getBiases();
    void setParams(Map<String, Object> params);
    void optimizePlan(FastList<TL> segments, FastList<LongList> segDimensions, int size);
}
