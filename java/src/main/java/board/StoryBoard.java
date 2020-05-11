package board;

import org.apache.commons.math3.util.FastMath;
import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;
import org.eclipse.collections.impl.list.mutable.primitive.LongArrayList;
import summary.Sketch;

import java.io.Serializable;
import java.util.List;

public class StoryBoard<T> implements Serializable {
    public FastList<LongArrayList> dimensionCols;
    public FastList<Sketch<T>> sketchCol;
    public DoubleArrayList totalCol;
    public IntArrayList tierCol;

    public StoryBoard(int numDims) {
        dimensionCols = new FastList<>(numDims);
        for (int i = 0; i < numDims; i++) {
            dimensionCols.add(new LongArrayList());
        }
        sketchCol = new FastList<>();
        totalCol = new DoubleArrayList();
        tierCol = new IntArrayList();
    }

    public void addSketches(LongList dims, List<Sketch<T>> sketches, double total) {
        int numDims = dimensionCols.size();
        int numSketches = sketches.size();
        for (int j = 0; j < numSketches; j++) {
            for (int i = 0; i < numDims; i++) {
                dimensionCols.get(i).add(dims.get(i));
            }
            tierCol.add(j);
            sketchCol.add(sketches.get(j));
            totalCol.add(total * FastMath.pow(2, j));
        }
    }
}
