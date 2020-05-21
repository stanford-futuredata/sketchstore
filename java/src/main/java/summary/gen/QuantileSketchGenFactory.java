package summary.gen;

import org.eclipse.collections.api.list.primitive.DoubleList;
import summary.compressor.quantile.CoopQuantileCompressor;
import summary.compressor.quantile.SkipQuantileCompressor;
import summary.compressor.quantile.TrackedQuantileCompressor;

import java.util.List;

public class QuantileSketchGenFactory implements SketchGenFactory<Double, DoubleList> {
    public SketchGen<Double, DoubleList> getSketchGen(
            String sketch,
            List<Double> xToTrack
            ) {
        if (sketch.equals("top_values")) {
            return new SeqCounterCompressorGen(new TrackedQuantileCompressor(xToTrack));
        } else if(sketch.equals("truncation")) {
            return new SeqCounterCompressorGen(new SkipQuantileCompressor(false, 0));
        } else if(sketch.equals("cooperative")) {
            return new SeqCounterCompressorGen(new CoopQuantileCompressor());
        } else {
            return null;
        }
    }
}
