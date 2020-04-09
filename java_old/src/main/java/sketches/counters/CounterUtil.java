package sketches.counters;

import java.util.*;

public class CounterUtil<K> {
    private List<KeyCount<K>> xs;
    private int tailIdx = 0;
    private int size = 0;
    private double threshold = 0;

    public CounterUtil(List<KeyCount<K>> xArg) {
        this.xs = xArg;
    }
    public void process(int sizeArg) {
        this.size = sizeArg;
        Collections.sort(xs, (o1, o2) -> o2.count - o1.count);

        int n = xs.size();
        double sum_rest = 0;
        for (KeyCount<K> kc : xs) {
            sum_rest += kc.count;
        }
        threshold = sum_rest / size;
        boolean foundTail = false;

        for (tailIdx = 0; tailIdx < n; tailIdx++) {
            if (tailIdx > 0) {
                sum_rest -= xs.get(tailIdx-1).count;
            }
            threshold = sum_rest / (size - tailIdx);
            if (xs.get(tailIdx).count < threshold) {
                foundTail = true;
                break;
            }
        }
        if (!foundTail) {
            threshold = 0;
            tailIdx = n;
        }
    }
    public int getTailIdx() {
        return tailIdx;
    }
    public double getThreshold() {
        return threshold;
    }
}
