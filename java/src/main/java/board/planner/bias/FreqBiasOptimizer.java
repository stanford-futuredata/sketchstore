package board.planner.bias;

import org.apache.commons.math3.util.FastMath;
import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.impl.list.mutable.FastList;
import runner.Timer;
import runner.factory.FreqSketchGenFactory;

import java.io.*;
import java.nio.file.Path;
import java.util.StringTokenizer;

public class FreqBiasOptimizer implements BiasOptimizer<LongList> {
    double[] biasValues;

    public void toFile(
            Path fileName,
            int[] segmentSpaces,
            FastList<SegmentCCDF> segmentCCDFs
    ) throws IOException {
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(fileName.toFile())));
        for (int x : segmentSpaces) {
            out.print(x);
            out.print(" ");
        }
        out.println();
        for (SegmentCCDF ccdf : segmentCCDFs) {
            for (long x : ccdf.occCounts) {
                out.print(x);
                out.print(" ");
            }
            out.println();
            for (long x : ccdf.occCountFrequency) {
                out.print(x);
                out.print(" ");
            }
            out.println();
        }
        out.close();
    }

    @Override
    public void compute(int[] segmentSpaces, FastList<LongList> segmentValues) {
        int nSegments = segmentValues.size();
        FastList<SegmentCCDF> segmentCCDFs = new FastList<>(nSegments);
        for (LongList curValues : segmentValues) {
            segmentCCDFs.add(
                    SegmentCCDF.fromItems(curValues)
            );
        }
        biasValues = new double[nSegments];
        try {
            File tempFile = File.createTempFile("bias_opt", "temp");
//            System.out.println("temp file: "+tempFile.getAbsolutePath());
            tempFile.deleteOnExit();

            Timer serTime = new Timer();
            serTime.start();
            toFile(tempFile.toPath(), segmentSpaces, segmentCCDFs);
            serTime.end();
            System.out.println("Serialized in: "+serTime.getTotalMs());

            Runtime r=Runtime.getRuntime();
            serTime.reset();
            serTime.start();
            Process p = r.exec("../cpp/solver "+tempFile.getAbsolutePath());
            InputStream pIn = p.getInputStream();
            BufferedReader pReader = new BufferedReader(new InputStreamReader(pIn));
            String line = pReader.readLine();
            serTime.end();
            System.out.println("Ran Cpp in: "+serTime.getTotalMs());

            serTime.reset();
            serTime.start();
            StringTokenizer tok = new StringTokenizer(line, " ");
            for (int i = 0; i < nSegments; i++){
                biasValues[i] = Double.parseDouble(tok.nextToken());
            }
            serTime.end();
            System.out.println("Deser Cpp output in: "+serTime.getTotalMs());
            pReader.close();
        } catch (Exception e) {
            throw new RuntimeException("Call Failed: "+e.getMessage());
        }
    }

    @Override
    public double[] getBias() {
        return biasValues;
    }
}
