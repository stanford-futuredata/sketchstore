package summary.accumulator;

import org.eclipse.collections.api.DoubleIterable;
import org.eclipse.collections.api.list.primitive.DoubleList;
import org.eclipse.collections.api.tuple.primitive.DoubleIntPair;
import org.eclipse.collections.impl.tuple.primitive.PrimitiveTuples;

import java.util.Arrays;
import java.util.Comparator;

public class ArgSort {
    public static int[] argSortSlow(DoubleList xs) {
        int n = xs.size();
        DoubleIntPair[] xps = new DoubleIntPair[n];
        for (int i = 0; i < n; i++) {
            xps[i] = PrimitiveTuples.pair(xs.get(i), i);
        }
        Arrays.sort(xps);
        int[] args = new int[n];
        for(int i = 0; i < n; i++) {
            args[i] = xps[i].getTwo();
        }
        return args;
    }
    public static int[] argSortSlow2(DoubleList xs) {
        int n = xs.size();
        Integer[] args = new Integer[n];
        for (int i = 0; i < n; i++) {
            args[i] = i;
        }
        Arrays.sort(args, (Integer a, Integer b) -> {
            double val = xs.get(a) - xs.get(b);
            if (val < 0) {
                return -1;
            } else if (val > 0) {
                return 1;
            } else {
                return 0;
            }
        });
        int[] argsRaw = new int[n];
        for (int i = 0; i < n; i++) {
            argsRaw[i] = args[i];
        }
        return argsRaw;
    }

    public static int[] argSort(DoubleList xs) {
        return argSort(xs, 0, xs.size());
    }

    public static int[] argSort(DoubleList xs, int startIdx, int endIdx) {
        int n = endIdx-startIdx;
        if (n <= 0) {
            return new int[0];
        } else if (n == 1) {
            int[] retVal = new int[1];
            retVal[0] = startIdx;
            return retVal;
        } else if (n == 2) {
            int[] retVal = new int[n];
            if (xs.get(startIdx) < xs.get(startIdx+1)) {
                retVal[0] = startIdx;
                retVal[1] = startIdx+1;
            } else {
                retVal[1] = startIdx;
                retVal[0] = startIdx+1;
            }
            return retVal;
        } else {
            int midIdx = (startIdx) + n / 2;
            int[] leftArgs = argSort(xs, startIdx, midIdx);
            int[] rightArgs = argSort(xs, midIdx, endIdx);
            return mergeArgs(xs, leftArgs, rightArgs);
        }
    }

    public static int[] mergeArgs(DoubleList xs, int[] a, int[] b) {
        int[] c = new int[a.length + b.length];
        int i = 0, j = 0;
        for (int k = 0; k < c.length; k++) {
            if      (i >= a.length) c[k] = b[j++];
            else if (j >= b.length) c[k] = a[i++];
            else if (xs.get(a[i]) <= xs.get(b[j])) {
                c[k] = a[i++];
            } else {
                c[k] = b[j++];
            }
        }
        return c;
    }
}
