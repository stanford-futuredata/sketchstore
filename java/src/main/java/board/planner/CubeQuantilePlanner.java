package board.planner;

import org.eclipse.collections.api.list.primitive.DoubleList;
import org.eclipse.collections.api.list.primitive.IntList;
import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;
import org.eclipse.collections.impl.list.mutable.primitive.LongArrayList;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.LongColumn;
import tech.tablesaw.api.Table;

public class CubeQuantilePlanner extends CubePlanner<DoubleList> {
    @Override
    public DoubleList getColumnRange(Table data, String metricCol, IntList idxs) {
        DoubleColumn col = (DoubleColumn) data.column(metricCol);

        int nIdxs = idxs.size();
        DoubleArrayList colValues = new DoubleArrayList(nIdxs);
        for (int i = 0; i < nIdxs; i++) {
            colValues.add(col.getDouble(idxs.get(i)));
        }
        return colValues;
    }
}
