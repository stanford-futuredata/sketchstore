package runner;

import board.StoryBoard;
import board.query.ErrorMetric;
import board.query.LinearAccProcessor;
import board.query.LinearQueryProcessor;
import board.workload.LinearWorkload;
import io.*;
import org.eclipse.collections.api.PrimitiveIterable;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.list.primitive.DoubleList;
import org.eclipse.collections.api.list.primitive.IntList;
import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.api.tuple.Pair;
import org.eclipse.collections.impl.factory.primitive.IntLists;
import org.eclipse.collections.impl.list.mutable.FastList;
import runner.factory.FreqSketchGenFactory;
import runner.factory.QuantileSketchGenFactory;
import runner.factory.SketchGenFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class QueryRunner<T, TL extends PrimitiveIterable> {
    RunConfig config;

    String experiment;
    boolean quantile;
    String outputDir;
    String xToTrackPath;
    int numQueries;

    int granularity;
    List<String> sketches;
    List<Integer> sizes;

    public QueryRunner(RunConfig config) {
        this.config = config;

        experiment = config.get("experiment");
        quantile = config.get("quantile");
        outputDir = config.get("out_dir");

        granularity = config.get("granularity");
        sizes = config.get("sizes");
        sketches = config.get("sketches");

        xToTrackPath = config.get("x_to_track");
        numQueries = config.get("num_queries");
    }

    public FastList<Map<String, String>> run(
            SimpleCSVDataSource<T> xTrackSource,
            SketchGenFactory<T, TL> genFactory
    ) throws Exception {
        Path boardDir = Paths.get(outputDir, "boards", experiment);
        int curSize = sizes.get(0);

        xTrackSource.setHasHeader(true);
        FastList<T> xToTrack = xTrackSource.get(
                xToTrackPath, 0
        );

        LinearWorkload workloadGen = new LinearWorkload(0);
        FastList<IntList> workloadIntervals = workloadGen.generate(granularity, numQueries);

        String boardPath = String.format("%s/%s",
                boardDir,
                IOUtil.getBoardName(
                        "top_values",
                        curSize,
                        granularity
                ));
        File fIn = new File(boardPath);
        StoryBoard<T> trueBoard = IOUtil.loadBoard(fIn);
        LinearQueryProcessor<T> p_true = genFactory.getLinearQueryProcessor(
                "top_values",
                granularity
        );

        FastList<Map<String, String>> results = new FastList<>();
        MutableMap<String, String> baseResults = Maps.mutable.empty();
        baseResults.put("experiment", experiment);

        for (String curSketch: sketches) {
            if (curSketch.equals("top_values")) {
                continue;
            }
            System.out.println("Running Sketch: "+curSketch);
            boardPath = String.format("%s/%s",
                    boardDir,
                    IOUtil.getBoardName(
                            curSketch,
                            curSize,
                            granularity
                    ));
            fIn = new File(boardPath);
            StoryBoard<T> board = IOUtil.loadBoard(fIn);

            LinearQueryProcessor<T> p_raw = genFactory.getLinearQueryProcessor(
                    curSketch,
                    granularity
            );

            // Warm-Up
            for (IntList curInterval : workloadIntervals) {
                int startIdx = curInterval.get(0);
                int endIdx = curInterval.get(1);
                p_raw.setRange(startIdx, endIdx);
                p_raw.query(board, xToTrack);
            }
            System.runFinalization();
            System.gc();

            Timer sketchTotalTimer = new Timer();
            Timer queryTimer = new Timer();
            for (IntList curInterval : workloadIntervals) {
                int startIdx = curInterval.get(0);
                int endIdx = curInterval.get(1);
                p_true.setRange(startIdx, endIdx);
                DoubleList trueResults = p_true.query(trueBoard, xToTrack);
                double trueTotal = p_true.total(trueBoard);
                p_raw.setRange(startIdx, endIdx);

                sketchTotalTimer.start();
                queryTimer.reset();
                queryTimer.start();
                DoubleList queryResults = p_raw.query(board, xToTrack);
                queryTimer.end();
                sketchTotalTimer.end();

                MutableMap<String, Double> errorQuantities = ErrorMetric.calcErrors(
                        trueResults,
                        queryResults
                );

                MutableMap<String, String> curResults = baseResults.clone();
                curResults.put("sketch", curSketch);
                curResults.put("size", Integer.toString(curSize));
                curResults.put("start_idx", Integer.toString(startIdx));
                curResults.put("end_idx", Integer.toString(endIdx));
                curResults.put("query_len", Integer.toString(endIdx-startIdx));
                curResults.put("total", Double.toString(trueTotal));
                curResults.put("query_time", Double.toString(queryTimer.getTotalMs()));
                errorQuantities.forEachKeyValue((String errType, Double errValue) -> {
                    curResults.put(errType, errValue.toString());
                });
                results.add(curResults);
            }
            System.out.println("Sketch Ran in Time: "+sketchTotalTimer.getTotalMs());
        }

        Path resultsDir = Paths.get(outputDir, "results", experiment);
        Files.createDirectories(resultsDir);
        Path resultsFile = resultsDir.resolve("errors.csv");

        CSVOutput.writeAllResults(results, resultsFile.toString());

        return results;
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Starting Query Runner");
//        System.in.read();
        String confFile = args[0];
        RunConfig config = RunConfig.fromJsonFile(confFile);
        boolean quantile = config.get("quantile");
        if (quantile) {
            QueryRunner<Double, DoubleList> runner = new QueryRunner<>(config);
            SimpleCSVDataSource<Double> xTrackSource = new SimpleCSVDataSourceDouble();
            SketchGenFactory<Double, DoubleList> sketchGenFactory = new QuantileSketchGenFactory();
            runner.run(
                    xTrackSource,
                    sketchGenFactory
            );
        } else {
            QueryRunner<Long, LongList> runner = new QueryRunner<>(config);
            SimpleCSVDataSource<Long> xTrackSource = new SimpleCSVDataSourceLong();
            SketchGenFactory<Long, LongList> sketchGenFactory = new FreqSketchGenFactory();
            runner.run(
                    xTrackSource,
                    sketchGenFactory
            );
        }
    }
}
