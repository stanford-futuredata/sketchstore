package runner.factory;

import board.planner.CubeOptimizer;
import board.planner.LinearOptimizer;
import board.planner.PlanOptimizer;
import board.planner.bias.BiasOptimizer;
import board.planner.bias.NopBiasOptimizer;
import board.planner.size.*;
import board.query.*;
import org.apache.commons.math3.util.FastMath;
import org.eclipse.collections.api.list.primitive.DoubleList;
import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.impl.factory.primitive.DoubleLists;
import summary.accumulator.Accumulator;
import summary.accumulator.ListQuantileAccumulator;
import summary.accumulator.MapQuantileAccumulator;
import summary.accumulator.MergingAccumulator;
import summary.compressor.quantile.CoopQuantileCompressor;
import summary.compressor.quantile.SkipQuantileCompressor;
import summary.compressor.quantile.TrackedQuantileCompressor;
import summary.compressor.quantile.USampleQuantCompressor;
import summary.custom.YahooKLLGen;
import summary.custom.YahooLowDiscSketchGen;
import summary.gen.DyadicSeqCounterCompressorGen;
import summary.gen.SeqCounterCompressorGen;
import summary.gen.SketchGen;

import java.util.List;

public class QuantileSketchGenFactory implements SketchGenFactory<Double, DoubleList> {
    public SketchGen<Double, DoubleList> getSketchGen(
            String sketch,
            List<Double> xToTrack,
            int maxLength
            ) {
        if (sketch.equals("top_values")) {
            return new SeqCounterCompressorGen(new TrackedQuantileCompressor(xToTrack));
        } else if(sketch.equals("truncation")) {
            return new SeqCounterCompressorGen(new SkipQuantileCompressor(false, 0));
        } else if(sketch.equals("cooperative")) {
            CoopQuantileCompressor compressor = new CoopQuantileCompressor();
            compressor.setMaxAccSize(0);
            return new SeqCounterCompressorGen(compressor);
        } else if (sketch.equals("low_discrep")) {
            return new YahooLowDiscSketchGen();
        } else if(sketch.equals("kll")) {
            return new YahooKLLGen();
        } else if (sketch.equals("dyadic_truncation")) {
            int maxHeight = (int) FastMath.log(2.0, maxLength);
            return new DyadicSeqCounterCompressorGen(
                    () -> new SkipQuantileCompressor(false, 0),
                    maxHeight
            );
        } else if(
                sketch.equals("pps")
                || sketch.equals("pps_coop")
        ) {
            return new SeqCounterCompressorGen(new SkipQuantileCompressor(true, 0));
        } else if (
                sketch.equals("random_sample")
                || sketch.equals("random_sample_strat")
                || sketch.equals("random_sample_prop")
        ) {
            return new SeqCounterCompressorGen(new USampleQuantCompressor(0));
        }
        throw new RuntimeException("Unsupported Sketch: "+sketch);
    }

    @Override
    public Accumulator<Double, DoubleList> getAccumulator(
            String sketch
            ) {
        if (sketch.equals("top_values")
                || sketch.equals("truncation")
                || sketch.equals("cooperative")
                || sketch.equals("dyadic_truncation")
                || sketch.equals("pps")
                || sketch.equals("pps_coop")
                || sketch.equals("random_sample")
                || sketch.equals("random_sample_strat")
                || sketch.equals("random_sample_prop")
        ) {
//            return new MapQuantileAccumulator();
            return new ListQuantileAccumulator();
        } else if (
                sketch.equals("kll")
        ) {
            return new MergingAccumulator<>(
                    new YahooKLLGen(),
                    DoubleLists.immutable.empty()
            );
        } else if (
                sketch.equals("low_discrep")
        ) {
            return new MergingAccumulator<>(
                    new YahooLowDiscSketchGen(),
                    DoubleLists.immutable.empty()
            );
        }
        throw new RuntimeException("Unsupported Sketch: "+sketch);
    }

    @Override
    public LinearQueryProcessor<Double> getLinearQueryProcessor(
            String sketch,
            int maxLength,
            int accumulatorSize
            ) {
        if (sketch.equals("dyadic_truncation")) {
            int maxHeight = (int) FastMath.log(2.0, maxLength);
            return new DyadicLinearAccProcessor<>(
                    this.getAccumulator(sketch),
                    maxHeight,
                    accumulatorSize
            );
        } else {
            return new LinearAccProcessor<>(this.getAccumulator(sketch), accumulatorSize);
        }
    }

    @Override
    public CubeQueryProcessor<Double> getCubeQueryProcessor(String sketch) {
        return new CubeAccProcessor<>(getAccumulator(sketch));
    }

    @Override
    public PlanOptimizer<DoubleList> getPlanOptimizer(String sketch, boolean isCube) {
        if (isCube) {
            SizeOptimizer<DoubleList> sizeOpt = null;
            BiasOptimizer<DoubleList> biasOpt = new NopBiasOptimizer<>();

            if (sketch.equals("pps_coop")) {
                sizeOpt = new CoopSizeOptimizer<>(1.0/3);
            } else if (sketch.equals("random_sample_prop")) {
                sizeOpt = new PropSizeOptimizer<>();
            } else if (sketch.equals("random_sample_strat")) {
                sizeOpt = new CoopSizeOptimizer<>(1.0/2);
            } else if (
                    sketch.equals("kll")
                    || sketch.equals("low_discrep")
            ) {
                sizeOpt = new NopSizeOptimizer<>();
            } else {
                sizeOpt = new RoundedSizeOptimizer<>();
            }
            CubeOptimizer<DoubleList> opt = new CubeOptimizer<>(
                    sizeOpt,
                    biasOpt
            );
            return opt;
        } else {
            return new LinearOptimizer<>();
        }
    }
}
