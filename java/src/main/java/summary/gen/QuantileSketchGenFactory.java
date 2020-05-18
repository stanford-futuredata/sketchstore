package summary.gen;

import org.eclipse.collections.api.list.primitive.DoubleList;

import java.util.List;

public class QuantileSketchGenFactory implements SketchGenFactory<Double, DoubleList> {
    public SketchGen<Double, DoubleList> getSketchGen(
            String sketch,
            List<Double> xToTrack
            ) {
//        if (sketch.equals("top_values")) {
//            return new ItemDictCompressorGen(new TopValuesCompressor(xToTrack));
//        } else if(sketch.equals("truncation")) {
//            return new ItemDictCompressorGen(new TruncationCompressor());
//        } else if(sketch.equals("cooperative")) {
//            return new ItemDictCompressorGen(new CoopFreqCompressor(0));
//        } else {
//            return null;
//        }
        return null;
    }
}
