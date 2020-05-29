package runner.factory;

import board.planner.CubeOptimizer;
import board.planner.LinearOptimizer;
import board.planner.PlanOptimizer;
import board.planner.bias.BiasOptimizer;
import board.planner.bias.FreqBiasOptimizer;
import board.planner.bias.NopBiasOptimizer;
import board.planner.size.CoopSizeOptimizer;
import board.planner.size.NopSizeOptimizer;
import board.planner.size.PropSizeOptimizer;
import board.planner.size.SizeOptimizer;
import board.query.*;
import org.apache.commons.math3.util.FastMath;
import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.impl.factory.primitive.LongLists;
import summary.accumulator.Accumulator;
import summary.accumulator.MapFreqAccumulator;
import summary.accumulator.MergingAccumulator;
import summary.compressor.freq.*;
import summary.custom.CMSSketchGen;
import summary.custom.YahooMGGen;
import summary.gen.DyadicItemDictCompressorGen;
import summary.gen.ItemCounterCompressorGen;
import summary.gen.SketchGen;

import java.util.List;

public class FreqSketchGenFactory implements SketchGenFactory<Long, LongList> {
    public SketchGen<Long, LongList> getSketchGen(
            String sketch,
            List<Long> xToTrack,
            int maxLength
            ) {
        if (sketch.equals("top_values")) {
            return new ItemCounterCompressorGen(new TrackedFreqCompressor(xToTrack));
        } else if (sketch.equals("truncation")) {
            return new ItemCounterCompressorGen(new TruncationFreqCompressor());
        } else if (sketch.equals("cooperative")) {
            return new ItemCounterCompressorGen(new CoopFreqCompressor(0));
        } else if (sketch.equals("yahoo_mg")) {
            return new YahooMGGen();
        } else if (sketch.equals("cms_min")) {
            return new CMSSketchGen();
        } else if (
                sketch.equals("pps")
                        || sketch.equals("pps_coop")
        ) {
            return new ItemCounterCompressorGen(new HaircombCompressor(0));
        } else if (sketch.equals("dyadic_truncation")) {
            int maxHeight = (int) FastMath.log(2.0, maxLength);
            return new DyadicItemDictCompressorGen(
                    () -> new TruncationFreqCompressor(),
                    maxHeight
            );
        } else if (
                sketch.equals("random_sample")
                || sketch.equals("random_sample_prop")
                || sketch.equals("random_sample_strat")
        ) {
            return new ItemCounterCompressorGen(new USampleFreqCompressor(0));
        }
        throw new RuntimeException("Invalid sketch name");
    }

    @Override
    public Accumulator<Long, LongList> getAccumulator(String sketch) {
        if (sketch.equals("top_values")
                || sketch.equals("truncation")
                || sketch.equals("cooperative")
                || sketch.equals("pps")
                || sketch.equals("pps_coop")
                || sketch.equals("dyadic_truncation")
                || sketch.equals("random_sample")
                || sketch.equals("random_sample_prop")
                || sketch.equals("random_sample_strat")
        ) {
            return new MapFreqAccumulator();
        } else if (sketch.equals("yahoo_mg")) {
            return new MergingAccumulator<>(new YahooMGGen(), LongLists.immutable.empty());
        } else if (sketch.equals("cms_min")) {
            return new MergingAccumulator<>(new CMSSketchGen(), LongLists.immutable.empty());
        }
        throw new RuntimeException("Invalid sketch name");
    }

    @Override
    public LinearQueryProcessor<Long> getLinearQueryProcessor(
            String sketch,
            int maxLength
    ) {
        if (sketch.equals("dyadic_truncation")) {
            int maxHeight = (int) FastMath.log(2.0, maxLength);
            return new DyadicLinearAccProcessor<>(
                    this.getAccumulator(sketch),
                    maxHeight
            );
        } else {
            return new LinearAccProcessor<>(this.getAccumulator(sketch));
        }
    }

    @Override
    public CubeQueryProcessor<Long> getCubeQueryProcessor(String sketch) {
        return new CubeAccProcessor<>(getAccumulator(sketch));
    }

    @Override
    public PlanOptimizer<LongList> getPlanOptimizer(String sketch, boolean isCube) {
        if (isCube) {
            SizeOptimizer<LongList> sizeOpt = null;
            BiasOptimizer<LongList> biasOpt = null;

            // size
            if (sketch.equals("pps_coop")) {
                sizeOpt = new CoopSizeOptimizer<>(1.0/3);
            } else if (sketch.equals("random_sample_prop")) {
                sizeOpt = new PropSizeOptimizer<>();
            } else if (sketch.equals("random_sample_strat")) {
                sizeOpt = new CoopSizeOptimizer<>(1.0/2);
            } else {
                sizeOpt = new NopSizeOptimizer<>();
            }

            // bias
            if (sketch.equals("pps_coop")) {
                biasOpt = new FreqBiasOptimizer();
            } else {
                biasOpt = new NopBiasOptimizer<>();
            }

            CubeOptimizer<LongList> opt = new CubeOptimizer<>(
                    sizeOpt,
                    biasOpt
            );
            return opt;
        } else {
            return new LinearOptimizer<>();
        }
    }
}
