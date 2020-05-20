package runner;

import board.StoryBoard;
import board.query.ErrorMetric;
import board.query.LinearAccProcessor;
import board.query.LinearSelector;
import board.query.QueryProcessor;
import board.workload.LinearWorkload;
import io.*;
import org.eclipse.collections.api.PrimitiveIterable;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.list.primitive.DoubleList;
import org.eclipse.collections.api.list.primitive.IntList;
import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.api.tuple.Pair;
import org.eclipse.collections.impl.list.mutable.FastList;
import summary.accumulator.MapFreqAccumulator;
import summary.accumulator.SortedQuantileAccumulator;

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
            LinearSelector selector,
            QueryProcessor<T> processor
    ) throws Exception {
        Path boardDir = Paths.get(outputDir, experiment, "boards");
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
            sketchTotalTimer.start();
            for (IntList curInterval : workloadIntervals) {
                int startIdx = curInterval.get(0);
                int endIdx = curInterval.get(1);
                selector.setRange(startIdx, endIdx);
                DoubleList trueResults = processor.query(trueBoard, xToTrack);
                DoubleList queryResults = processor.query(board, xToTrack);
                double trueTotal = processor.total(trueBoard);
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
                for (Pair<String, Double> curError : errorQuantities.keyValuesView()) {
                    curResults.put(curError.getOne(), Double.toString(curError.getTwo()));
                }
                results.add(curResults);
            }
            sketchTotalTimer.end();
            System.out.println("Sketch Ran in Time: "+sketchTotalTimer.getTotalMs());
        }

        Path resultsDir = Paths.get(outputDir, experiment, "results");
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
            LinearAccProcessor<Double, DoubleList> p_raw = new LinearAccProcessor<>(
                    new SortedQuantileAccumulator()
            );
            runner.run(
                    xTrackSource,
                    p_raw,
                    p_raw
            );
        } else {
            QueryRunner<Long, LongList> runner = new QueryRunner<>(config);
            SimpleCSVDataSource<Long> xTrackSource = new SimpleCSVDataSourceLong();
            LinearAccProcessor<Long, LongList> p_raw = new LinearAccProcessor<>(
                    new MapFreqAccumulator()
            );
            runner.run(
                    xTrackSource,
                    p_raw,
                    p_raw
            );
        }
    }
}
