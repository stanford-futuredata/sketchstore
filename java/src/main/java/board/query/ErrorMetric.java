package board.query;

import org.apache.commons.math3.util.FastMath;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.list.primitive.DoubleList;
import org.eclipse.collections.api.map.MutableMap;

public class ErrorMetric {
    public static MutableMap<String, Double> calcErrors(
            DoubleList trueValues,
            DoubleList estValues
    ) {
        int n = trueValues.size();
        double sum=0, sq_sum=0, max=0;
        for (int i = 0; i < n; i++) {
            double curError = FastMath.abs(trueValues.get(i) - estValues.get(i));
            sum += curError;
            sq_sum += curError*curError;
            if (curError > max) {
                max = curError;
            }
        }
        MutableMap<String, Double> results = Maps.mutable.of(
                "mean", sum/n,
                "rmse", Math.sqrt(sq_sum/n),
                "max", max
        );
        return results;
    }
}
