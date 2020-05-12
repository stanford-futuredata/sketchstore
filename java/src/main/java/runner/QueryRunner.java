package runner;

import board.StoryBoard;
import board.query.ErrorMetric;
import board.query.LinearFreqAccProcessor;
import board.query.LinearSelector;
import board.query.QueryProcessor;
import board.workload.LinearWorkload;
import io.CSVOutput;
import io.IOUtil;
import io.SimpleCSVDataSource;
import io.SimpleCSVDataSourceLong;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.list.primitive.DoubleList;
import org.eclipse.collections.api.list.primitive.IntList;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.api.tuple.Pair;
import org.eclipse.collections.impl.factory.OrderedMaps;
import org.eclipse.collections.impl.list.mutable.FastList;
import summary.accumulator.ExactFreqAccumulator;
import tech.tablesaw.api.Table;
import tech.tablesaw.columns.Column;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class QueryRunner {
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

    public FastList<Map<String, String>> run() throws Exception {
        Path boardDir = Paths.get(outputDir, experiment, "boards");
        int curSize = sizes.get(0);

        SimpleCSVDataSource<Long> xTrackSource = new SimpleCSVDataSourceLong(xToTrackPath, 0);
        xTrackSource.setHasHeader(true);
        FastList<Long> xToTrack = xTrackSource.get();

        LinearSelector selector;
        QueryProcessor<Long> processor;
        LinearFreqAccProcessor p_raw = new LinearFreqAccProcessor(
                new ExactFreqAccumulator()
        );
        selector = p_raw;
        processor = p_raw;
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
        StoryBoard<Long> trueBoard = IOUtil.loadBoard(fIn);

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
            StoryBoard<Long> board = IOUtil.loadBoard(fIn);

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
        System.in.read();
        String confFile = args[0];
        RunConfig config = RunConfig.fromJsonFile(confFile);
        QueryRunner runner = new QueryRunner(config);
        runner.run();
    }
}