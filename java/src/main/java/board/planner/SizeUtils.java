package board.planner;

import org.apache.commons.math3.util.FastMath;

public class SizeUtils {
    public static int[] safeRound(double[] xs) {
        int n = xs.length;
        int[] rounded = new int[n];
        double total = 0;
        int lastRoundedTotal = 0;
        int curRoundedTotal;
        for (int i = 0; i < n; i++) {
            total += xs[i];
            curRoundedTotal = (int)FastMath.round(total);
            if (i == 0) {
                rounded[i] = curRoundedTotal;
            } else {
                rounded[i] = curRoundedTotal - lastRoundedTotal;
            }
            lastRoundedTotal = curRoundedTotal;
        }
        return rounded;
    }
}
