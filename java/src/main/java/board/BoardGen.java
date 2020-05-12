package board;

import org.eclipse.collections.api.PrimitiveIterable;
import org.eclipse.collections.api.list.primitive.DoubleList;
import org.eclipse.collections.api.list.primitive.IntList;
import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.impl.list.mutable.FastList;
import summary.Sketch;
import summary.SketchGen;

import java.io.Serializable;

public class BoardGen<T extends Serializable, TL extends PrimitiveIterable> {
    public SketchGen<T,TL> sketchGen;
    public BoardGen(SketchGen<T,TL> sketchGen) {
        this.sketchGen = sketchGen;
    }
    public StoryBoard<T> generate(
            FastList<TL> segments,
            FastList<LongList> dims,
            IntList sizes,
            DoubleList biases
    ) {
        if (dims.size() == 0) {
            return null;
        }
        int ndims = dims.get(0).size();
        int nSegments = segments.size();

        StoryBoard<T> board = new StoryBoard<>(ndims);
        for (int i = 0; i < nSegments; i++) {
            TL curSegment = segments.get(i);
            FastList<Sketch<T>> curSketches = sketchGen.generate(
                    curSegment,
                    sizes.get(i),
                    biases.get(i)
            );
            board.addSketches(dims.get(i), curSketches, curSegment.size());
        }
        return board;
    }
}
