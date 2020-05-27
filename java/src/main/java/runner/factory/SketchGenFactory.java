package runner.factory;

import board.planner.CubeOptimizer;
import board.planner.PlanOptimizer;
import board.query.CubeQueryProcessor;
import board.query.LinearQueryProcessor;
import org.eclipse.collections.api.PrimitiveIterable;
import summary.accumulator.Accumulator;
import summary.gen.SketchGen;

import java.util.List;

public interface SketchGenFactory<T, TL extends PrimitiveIterable> {
    SketchGen<T, TL> getSketchGen(
            String sketch,
            List<T> xToTrack,
            int maxLength
    );
    Accumulator<T, TL> getAccumulator(
            String sketch
    );
    LinearQueryProcessor<T> getLinearQueryProcessor(
            String sketch,
            int maxLength
    );
    CubeQueryProcessor<T> getCubeQueryProcessor(
            String sketch
    );
    PlanOptimizer<TL> getPlanOptimizer(
            String sketch,
            boolean isCube
    );
}
