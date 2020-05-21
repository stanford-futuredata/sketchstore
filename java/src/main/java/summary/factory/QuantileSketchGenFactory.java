package summary.factory;

import org.eclipse.collections.api.list.primitive.DoubleList;
import org.eclipse.collections.impl.factory.primitive.DoubleLists;
import summary.accumulator.Accumulator;
import summary.accumulator.MapQuantileAccumulator;
import summary.accumulator.MergingAccumulator;
import summary.compressor.quantile.CoopQuantileCompressor;
import summary.compressor.quantile.SkipQuantileCompressor;
import summary.compressor.quantile.TrackedQuantileCompressor;
import summary.custom.YahooKLLGen;
import summary.gen.SeqCounterCompressorGen;
import summary.gen.SketchGen;

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
        } else if(sketch.equals("kll")) {
            return new YahooKLLGen();
        }
        return null;
    }

    @Override
    public Accumulator<Double, DoubleList> getAccumulator(String sketch) {
        if (sketch.equals("top_values")
                || sketch.equals("truncation")
                || sketch.equals("cooperative")
        ) {
            return new MapQuantileAccumulator();
        } else if (sketch.equals("kll")) {
            return new MergingAccumulator<>(new YahooKLLGen(), DoubleLists.immutable.empty());
        }
        return null;
    }
}
