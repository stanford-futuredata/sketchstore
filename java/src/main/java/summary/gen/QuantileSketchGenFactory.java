package summary.gen;

import org.eclipse.collections.api.list.primitive.DoubleList;
import summary.compressor.quantile.CoopQuantileCompressor;
import summary.compressor.quantile.SkipQuantileCompressor;
import summary.compressor.quantile.TrackedQuantileCompressor;

import javax.sound.midi.Track;
import java.util.List;

public class QuantileSketchGenFactory implements SketchGenFactory<Double, DoubleList> {
    public SketchGen<Double, DoubleList> getSketchGen(
            String sketch,
            List<Double> xToTrack
            ) {
        if (sketch.equals("top_values")) {
            return new SeqCDFCompressorGen(new TrackedQuantileCompressor(xToTrack));
        } else if(sketch.equals("truncation")) {
            return new SeqCDFCompressorGen(new SkipQuantileCompressor(false, 0));
        } else if(sketch.equals("cooperative")) {
            return new SeqCDFCompressorGen(new CoopQuantileCompressor());
        } else {
            return null;
        }
    }
}
