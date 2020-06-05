package runner;

import board.StoryBoard;
import board.query.CubeQueryProcessor;
import board.query.ErrorMetric;
import board.query.LinearQueryProcessor;
import board.workload.CubeWorkload;
import board.workload.LinearWorkload;
import io.*;
import org.eclipse.collections.api.PrimitiveIterable;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.list.primitive.DoubleList;
import org.eclipse.collections.api.list.primitive.IntList;
import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;
import runner.factory.FreqSketchGenFactory;
import runner.factory.QuantileSketchGenFactory;
import runner.factory.SketchGenFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueryRunner<T, TL extends PrimitiveIterable> {
    RunConfig config;

    String experiment;
    boolean quantile;
    String outputDir;
    String xToTrackPath;
    int numQueries;
    List<String> sketches;
    List<Integer> sizes;

    int granularity;
    List<Integer> accumulatorSizes;

    List<String> dimensionCols;
    List<Double> queryWorkloadProbs;

    boolean isCube;


    public QueryRunner(RunConfig config) {
        this.config = config;

        experiment = config.get("experiment");
        quantile = config.get("quantile");
        outputDir = config.get("out_dir");

        sizes = config.get("sizes");
        sketches = config.get("sketches");

        xToTrackPath = config.get("x_to_track");
        numQueries = config.get("num_queries");

        granularity = config.get("granularity", 0);
        accumulatorSizes = config.get("accumulator_sizes", Lists.mutable.of(0));
        dimensionCols = config.get("dimension_cols", Lists.mutable.empty());
        queryWorkloadProbs = config.get("query_workload_probs", Lists.mutable.<Double>empty());

        isCube = (!dimensionCols.isEmpty());
    }

    public FastList<Map<String, String>> runLinear(
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
                granularity,
                0
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

            Timer sketchTotalTimer = new Timer();
            for (int accumulatorSize : accumulatorSizes) {
                LinearQueryProcessor<T> p_raw = genFactory.getLinearQueryProcessor(
                        curSketch,
                        granularity,
                        accumulatorSize
                );

                // Warm-Up
                for (IntList curInterval : workloadIntervals) {
                    int startIdx = curInterval.get(0);
                    int endIdx = curInterval.get(1);
                    p_raw.setRange(startIdx, endIdx);
                    p_raw.query(board, xToTrack);
                    p_true.setRange(startIdx, endIdx);
                    p_true.query(trueBoard, xToTrack);
                    p_true.total(trueBoard);
                }
                System.runFinalization();
                System.gc();
                System.out.println("Warmed Up");

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
                    curResults.put("query_len", Integer.toString(endIdx - startIdx));
                    curResults.put("total", Double.toString(trueTotal));
                    curResults.put("query_time", Double.toString(queryTimer.getTotalMs()));
                    curResults.put("accumulator_size", Integer.toString(accumulatorSize));
                    errorQuantities.forEachKeyValue((String errType, Double errValue) -> {
                        curResults.put(errType, errValue.toString());
                    });
                    results.add(curResults);
                }
            }
            System.out.println("Sketch Ran in Time: "+sketchTotalTimer.getTotalMs());
        }

        Path resultsDir = Paths.get(outputDir, "results", experiment);
        Files.createDirectories(resultsDir);
        Path resultsFile = resultsDir.resolve("errors.csv");

        CSVOutput.writeAllResults(results, resultsFile.toString());

        return results;
    }

    public FastList<Map<String, String>> runCube(
            SimpleCSVDataSource<T> xTrackSource,
            SketchGenFactory<T, TL> genFactory
    ) throws Exception {
        Path boardDir = Paths.get(outputDir, "boards", experiment);
        int curSize = sizes.get(0);

        xTrackSource.setHasHeader(true);
        FastList<T> xToTrack = xTrackSource.get(
                xToTrackPath, 0
        );

        String boardPath = String.format("%s/%s",
                boardDir,
                IOUtil.getBoardName(
                        "top_values",
                        curSize,
                        granularity
                ));
        File fIn = new File(boardPath);
        StoryBoard<T> trueBoard = IOUtil.loadBoard(fIn);
        LongList dimensionCardinalities = trueBoard.getDimCardinalities();

        CubeQueryProcessor<T> p_true = genFactory.getCubeQueryProcessor("top_values");

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

            CubeQueryProcessor<T> p_raw = genFactory.getCubeQueryProcessor(curSketch);
            Timer sketchTotalTimer = new Timer();
            Timer queryTimer = new Timer();

            for (double curWorkloadProbability : queryWorkloadProbs) {
                System.out.println("Running with Workload Prob: "+curWorkloadProbability);
                CubeWorkload workloadGen = new CubeWorkload(0);
                FastList<LongList> workloadDimensions = workloadGen.generate(
                        dimensionCardinalities,
                        curWorkloadProbability,
                        numQueries
                );

                // Warm-Up
                for (LongList curDimensions: workloadDimensions) {
                    p_raw.setDimensions(curDimensions);
                    p_raw.query(board, xToTrack);
                }
                System.runFinalization();
                System.gc();

                MutableMap<LongList, Map<String, String>> memoized = new UnifiedMap<>();

                int queryNum = 0;
                for (LongList curFilterDimensions: workloadDimensions) {
                    if (memoized.containsKey(curFilterDimensions)) {
                        results.add(memoized.get(curFilterDimensions));
                        continue;
                    }
                    p_true.setDimensions(curFilterDimensions);
                    DoubleList trueResults = p_true.query(trueBoard, xToTrack);
                    double trueTotal = p_true.total(trueBoard);
                    p_raw.setDimensions(curFilterDimensions);

                    sketchTotalTimer.start();
                    queryTimer.reset();
                    queryTimer.start();
                    DoubleList queryResults = p_raw.query(board, xToTrack);
                    queryTimer.end();
                    sketchTotalTimer.end();

//                    System.out.println("true");
//                    System.out.println(trueResults);
//                    System.out.println("query");
//                    System.out.println(queryResults);
//
                    MutableMap<String, Double> errorQuantities = ErrorMetric.calcErrors(
                            trueResults,
                            queryResults
                    );

                    int numFilters = curFilterDimensions.select((long x) -> (x >= 0)).size();

                    MutableMap<String, String> curResults = baseResults.clone();
                    curResults.put("sketch", curSketch);
                    curResults.put("size", Integer.toString(curSize));
                    curResults.put("query_len", Integer.toString(numFilters));
                    curResults.put("total", Double.toString(trueTotal));
                    curResults.put("query_time", Double.toString(queryTimer.getTotalMs()));
                    curResults.put("workload_query_prob", Double.toString(curWorkloadProbability));
                    errorQuantities.forEachKeyValue((String errType, Double errValue) -> {
                        curResults.put(errType, errValue.toString());
                    });
                    results.add(curResults);
                    queryNum++;
                    memoized.put(curFilterDimensions, curResults);
                }
                System.out.println("Query Runner #: "+queryNum);
            }
            System.out.println("Sketch Ran in Time: " + sketchTotalTimer.getTotalMs());
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
            if (runner.isCube) {
                runner.runCube(xTrackSource, sketchGenFactory);
            } else {
                runner.runLinear(
                        xTrackSource,
                        sketchGenFactory
                );
            }
        } else {
            QueryRunner<Long, LongList> runner = new QueryRunner<>(config);
            SimpleCSVDataSource<Long> xTrackSource = new SimpleCSVDataSourceLong();
            SketchGenFactory<Long, LongList> sketchGenFactory = new FreqSketchGenFactory();
            if (runner.isCube) {
                runner.runCube(xTrackSource, sketchGenFactory);
            } else {
                runner.runLinear(
                        xTrackSource,
                        sketchGenFactory
                );
            }
        }
    }
}
