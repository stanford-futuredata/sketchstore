package summary.compressor;

public class ItemCountsUtil {
    public static double find_t(double[] counts, int s) {
        double sumRest = sum(counts);
        double curT = sumRest / s;
        boolean foundTail = false;
        int tailIdx;
        for (tailIdx = 0; tailIdx < counts.length; tailIdx++) {
            if (tailIdx > 0) {
                sumRest -= counts[tailIdx-1];
            }
            curT = sumRest / (s - tailIdx);
            if (counts[tailIdx] < curT) {
                foundTail = true;
                break;
            }
        }
        if (!foundTail) {
            curT = 0;
            tailIdx = counts.length;
        }
        return curT;
    }
    public static double sum(double[] xs) {
        double acc = 0;
        for (double x : xs) {
            acc += x;
        }
        return acc;
    }
}
