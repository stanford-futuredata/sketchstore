package summary.factory;

import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.impl.factory.primitive.LongLists;
import summary.accumulator.Accumulator;
import summary.accumulator.MapFreqAccumulator;
import summary.accumulator.MergingAccumulator;
import summary.compressor.freq.CoopFreqCompressor;
import summary.compressor.freq.TrackedFreqCompressor;
import summary.compressor.freq.TruncationFreqCompressor;
import summary.custom.YahooMGGen;
import summary.gen.ItemCounterCompressorGen;
import summary.gen.SketchGen;

import java.util.List;

public class FreqSketchGenFactory implements SketchGenFactory<Long, LongList> {
    public SketchGen<Long, LongList> getSketchGen(
            String sketch,
            List<Long> xToTrack
            ) {
        if (sketch.equals("top_values")) {
            return new ItemCounterCompressorGen(new TrackedFreqCompressor(xToTrack));
        } else if(sketch.equals("truncation")) {
            return new ItemCounterCompressorGen(new TruncationFreqCompressor());
        } else if(sketch.equals("cooperative")) {
            return new ItemCounterCompressorGen(new CoopFreqCompressor(0));
        } else if (sketch.equals("yahoo_mg")){
            return new YahooMGGen();
        } else {
            return null;
        }
    }

    @Override
    public Accumulator<Long, LongList> getAccumulator(String sketch) {
        if (sketch.equals("top_values")
                || sketch.equals("truncation")
                || sketch.equals("cooperative")
        ) {
            return new MapFreqAccumulator();
        } else if (sketch.equals("yahoo_mg")) {
            return new MergingAccumulator<>(new YahooMGGen(), LongLists.immutable.empty());
        }
        return null;
    }
}
