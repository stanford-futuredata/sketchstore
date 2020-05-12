package summary;

import org.eclipse.collections.api.list.primitive.LongList;
import summary.compressor.CoopFreqCompressor;
import summary.compressor.TopValuesCompressor;
import summary.compressor.TruncationCompressor;

import java.util.List;
import java.util.Map;

public class FreqSketchGenFactory implements SketchGenFactory<Long, LongList> {
    public SketchGen<Long, LongList> getSketchGen(
            String sketch,
            List<Long> xToTrack
            ) {
        if (sketch.equals("top_values")) {
            return new ItemDictCompressorGen(new TopValuesCompressor(xToTrack));
        } else if(sketch.equals("truncation")) {
            return new ItemDictCompressorGen(new TruncationCompressor());
        } else if(sketch.equals("cooperative")) {
            return new ItemDictCompressorGen(new CoopFreqCompressor(0));
        } else {
            return null;
        }
    }
}
