package board.planner;

import org.eclipse.collections.api.list.primitive.DoubleList;
import org.eclipse.collections.api.list.primitive.IntList;
import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.impl.list.mutable.FastList;
import tech.tablesaw.api.Table;

import java.io.Serializable;

public interface Planner<TL> {
    void plan(
            Table t, String metricCol, FastList<String> dimCols
    );
    public FastList<TL> getSegments();
    public FastList<LongList> getDimensions();
    public IntList getSizes();
    public DoubleList getBiases();
}
