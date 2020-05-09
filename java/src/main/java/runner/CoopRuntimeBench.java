package runner;

import com.tdunning.math.stats.TDigest;
import io.SimpleCSVDataSource;
import io.SimpleCSVDataSourceDouble;
import org.apache.datasketches.kll.KllFloatsSketch;
import org.eclipse.collections.impl.list.mutable.FastList;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Random;

public class CoopRuntimeBench {
    public static void prepare() throws Exception {
        SimpleCSVDataSourceDouble in = new SimpleCSVDataSourceDouble(
                "/Users/edgan/Documents/datasets/storyboard/power/power.csv",
                0
        );
        FastList<Double> batch = in.get();
        double[] batchArray = new double[batch.size()];
        for (int i = 0; i < batchArray.length; i++) {
            batchArray[i] = batch.get(i);
        }

        FileOutputStream fOut = new FileOutputStream(
                "/Users/edgan/Documents/datasets/storyboard/power/power.jser"
        );
        ObjectOutputStream oOut = new ObjectOutputStream(fOut);
        oOut.writeObject(batchArray);
        oOut.close();
    }

    public static void run() throws Exception {
        KllFloatsSketch kll = new KllFloatsSketch(10000);
//        DoublesSketch sketch = DoublesSketch.builder().setK(10000).build();
        int nBatches = 1000;
        long startTime = System.currentTimeMillis();
        for (int batchIdx = 0; batchIdx < nBatches; batchIdx++) {
            FileInputStream fIn = new FileInputStream("/Users/edgan/Documents/datasets/storyboard/power/power.jser");
            ObjectInputStream oIn = new ObjectInputStream(fIn);
            double[] batch = (double[]) oIn.readObject();
            for (double x : batch) {
                kll.update((float) x);
            }
        }
        long endTime = System.currentTimeMillis();
        System.out.println(endTime - startTime);
        System.out.println(kll.getQuantile(.5));
    }

    public static void runSB() throws Exception {
        FileInputStream fIn = new FileInputStream("/Users/edgan/Documents/datasets/storyboard/power/power.jser");
        ObjectInputStream oIn = new ObjectInputStream(fIn);
        double[] batch = (double[]) oIn.readObject();
        Arrays.sort(batch);
        Random r = new Random(0);

        TDigest tDigest = TDigest.createMergingDigest(10000);
//        KllFloatsSketch kll = new KllFloatsSketch(10000);

        int batchSize = 100;
        int skipLength = batch.length / batchSize;
        int nBatches = 1000;

        long startTime = System.nanoTime();
        for (int batchIdx = 0; batchIdx < nBatches; batchIdx++) {
            int curOffset = r.nextInt(skipLength);
            for (; curOffset < batch.length; curOffset += skipLength) {
//                kll.update((float)batch[curOffset]);
                tDigest.add(batch[curOffset], skipLength);
            }
        }
        long endTime = System.nanoTime();
        System.out.println((endTime - startTime)*1e-6);
//        System.out.println(kll.getQuantile(.5));
        System.out.println(tDigest.quantile(.5));
    }
}
