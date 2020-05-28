package board.planner.bias;

import board.planner.bias.BFGSOptimizer;
import board.planner.bias.FunctionWithGrad;
import board.planner.bias.QuadraticFunction;
import com.github.lbfgs4j.LbfgsMinimizer;
import com.github.lbfgs4j.liblbfgs.Function;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class BFGSOptimizerTest {
    @Test
    public void testQuad() {
        double[] origin = {1,2};
        FunctionWithGrad f = new QuadraticFunction(origin);
        BFGSOptimizer opt = new BFGSOptimizer(f);
        opt.setVerbose(false);
        double[] xSolve = new double[2];
        xSolve = opt.solve(xSolve, 1e-5);
        assertEquals(1.0, xSolve[0], 1e-5);
    }
}
