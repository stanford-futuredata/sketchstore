package board;

import org.eclipse.collections.api.list.primitive.DoubleList;
import org.eclipse.collections.api.list.primitive.IntList;
import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.impl.list.mutable.FastList;
import summary.BoardSketch;
import summary.SketchGen;

import java.io.Serializable;

public class BoardGen<T extends Serializable,TL> {
    public SketchGen<T,TL> sketchGen;
    public BoardGen(SketchGen<T,TL> sketchGen) {
        this.sketchGen = sketchGen;
    }
    public SketchBoard<T> generate(
            FastList<TL> segments,
            FastList<LongList> dims,
            IntList sizes,
            DoubleList biases
    ) {
        if (dims.size() == 0) {
            return null;
        }
        int ndims = dims.get(0).size();
        int nrows = segments.size();
        SketchBoard<T> board = new SketchBoard<>(ndims);
        for (int i = 0; i < nrows; i++) {
            FastList<BoardSketch<T>> curSketches = sketchGen.generate(
                    segments.get(i),
                    sizes.get(i),
                    biases.get(i)
            );
            board.addSketches(dims.get(i), curSketches);
        }
        return board;
    }
}
