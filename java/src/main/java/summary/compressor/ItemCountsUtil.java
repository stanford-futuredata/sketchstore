package summary.compressor;

import org.eclipse.collections.api.list.primitive.DoubleList;

public class ItemCountsUtil {
    public static double find_t(DoubleList counts, int s) {
        double sumRest = counts.sum();
        double curT = sumRest / s;
        boolean foundTail = false;
        int tailIdx;
        for (tailIdx = 0; tailIdx < counts.size(); tailIdx++) {
            if (tailIdx > 0) {
                sumRest -= counts.get(tailIdx-1);
            }
            curT = sumRest / (s - tailIdx);
            if (counts.get(tailIdx) < curT) {
                foundTail = true;
                break;
            }
        }
        if (!foundTail) {
            curT = 0;
            tailIdx = counts.size();
        }
        return curT;
    }
}
