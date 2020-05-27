package board.planner;

import org.eclipse.collections.api.list.primitive.IntList;
import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.impl.list.mutable.primitive.LongArrayList;
import tech.tablesaw.api.LongColumn;
import tech.tablesaw.api.Table;

import java.util.List;

public class CubeFreqPlanner extends CubePlanner<LongList> {
    @Override
    public LongList getColumnRange(Table data, String metricCol, IntList idxs) {
        LongColumn col = (LongColumn) data.column(metricCol);

        int nIdxs = idxs.size();
        LongArrayList colValues = new LongArrayList(nIdxs);
        for (int i = 0; i < nIdxs; i++) {
            colValues.add(col.getLong(idxs.get(i)));
        }
        return colValues;
    }
}
