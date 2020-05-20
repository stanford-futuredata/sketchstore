package summary.gen;

import org.eclipse.collections.api.list.primitive.LongList;
import summary.compressor.freq.CoopFreqCompressor;
import summary.compressor.freq.TrackedFreqCompressor;
import summary.compressor.freq.TruncationFreqCompressor;

import java.util.List;

public class FreqSketchGenFactory implements SketchGenFactory<Long, LongList> {
    public SketchGen<Long, LongList> getSketchGen(
            String sketch,
            List<Long> xToTrack
            ) {
        if (sketch.equals("top_values")) {
            return new ItemDictCompressorGen(new TrackedFreqCompressor(xToTrack));
        } else if(sketch.equals("truncation")) {
            return new ItemDictCompressorGen(new TruncationFreqCompressor());
        } else if(sketch.equals("cooperative")) {
            return new ItemDictCompressorGen(new CoopFreqCompressor(0));
        } else {
            return null;
        }
    }
}
