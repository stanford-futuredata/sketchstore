package board;

import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;
import org.eclipse.collections.impl.list.mutable.primitive.LongArrayList;
import summary.BoardSketch;

import java.io.Serializable;

public class SketchBoard<T> implements Serializable {
    public FastList<LongArrayList> dimensionCols;
    public FastList<BoardSketch<T>> sketchCol;
    public IntArrayList tierCol;

    public SketchBoard(int numDims) {
        dimensionCols = new FastList<>(numDims);
        for (int i = 0; i < numDims; i++) {
            dimensionCols.add(new LongArrayList());
        }
        sketchCol = new FastList<>();
        tierCol = new IntArrayList();
    }

    public void addSketch(LongList dims, BoardSketch<T> sketch) {
        int numDims = dimensionCols.size();
        for (int i = 0; i < numDims; i++) {
            dimensionCols.get(i).add(dims.get(i));
        }
        tierCol.add(0);
        sketchCol.add(sketch);
    }

    public void addSketches(LongList dims, FastList<BoardSketch<T>> sketches) {
        int numDims = dimensionCols.size();
        int numSketches = sketches.size();
        for (int j = 0; j < numSketches; j++) {
            for (int i = 0; i < numDims; i++) {
                dimensionCols.get(i).add(dims.get(i));
            }
            tierCol.add(j);
            sketchCol.add(sketches.get(j));
        }
    }
}
