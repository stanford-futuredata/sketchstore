package runner;

import board.BoardGen;
import board.StoryBoard;
import board.planner.LinearFreqPlanner;
import board.planner.LinearPlanner;
import board.planner.LinearQuantilePlanner;
import io.*;
import org.eclipse.collections.api.PrimitiveIterable;
import org.eclipse.collections.api.list.primitive.DoubleList;
import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.impl.factory.primitive.IntLists;
import org.eclipse.collections.impl.list.mutable.FastList;
import runner.factory.QuantileSketchGenFactory;
import summary.gen.SketchGen;
import runner.factory.FreqSketchGenFactory;
import runner.factory.SketchGenFactory;
import tech.tablesaw.api.Table;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoadRunner<T, TL extends PrimitiveIterable> {
    RunConfig config;

    String experiment;
    boolean quantile;
    String csvPath;
    String xToTrackPath;
    List<Integer> colTypes;
    String metricCol;
    String outputDir;

    int granularity;
    List<String> sketches;
    List<Integer> sizes;

    public LoadRunner(RunConfig config) {
        this.config = config;

        experiment = config.get("experiment");
        quantile = config.get("quantile");
        csvPath = config.get("csv_path");
        xToTrackPath = config.get("x_to_track");
        colTypes = config.get("col_types");
        metricCol = config.get("metric_col");
        outputDir = config.get("out_dir");

        granularity = config.get("granularity");
        sizes = config.get("sizes");
        sketches = config.get("sketches");
    }

    public void runLinearLoad(
            SimpleCSVDataSource<T> xTrackSource,
            LinearPlanner<TL> planner,
            SketchGenFactory<T, TL> sketchGenFactory
    ) throws Exception {
        Table t = IOUtil.loadTable(csvPath, colTypes);
        Path boardDir = Paths.get(outputDir, "boards", experiment);
        Files.createDirectories(boardDir);

        xTrackSource.setHasHeader(true);
        FastList<T> xToTrack = xTrackSource.get(
                xToTrackPath, 0
        );

        int curGranularity = granularity;
        int curSize = sizes.get(0);
        Timer planTime = new Timer();
        planTime.start();
        planner.plan(
                t, metricCol, curGranularity, curSize
        );
        planTime.end();

        FastList<Map<String, String>> results = new FastList<>();

        for (String curSketch: sketches) {
            System.out.println("Loading: "+curSketch);

            SketchGen<T, TL> sGen = sketchGenFactory.getSketchGen(
                    curSketch,
                    xToTrack,
                    granularity
            );
            BoardGen<T, TL> bGen = new BoardGen<>(sGen);
            Timer constructTime = new Timer();
            constructTime.start();
            StoryBoard<T> board = bGen.generate(
                    planner.getSegments(),
                    planner.getDimensions(),
                    planner.getSizes(),
                    planner.getBiases()
            );
            constructTime.end();

            String outputPath = String.format("%s/%s",
                    boardDir.toString(),
                    IOUtil.getBoardName(
                            curSketch,
                            curSize,
                            curGranularity
                    ));
            File outFile = new File(outputPath);
            IOUtil.writeBoard(board, outFile);

            HashMap<String, String> curResults = new HashMap<>();
            curResults.put("sketch", curSketch);
            curResults.put("granularity", Integer.toString(granularity));
            curResults.put("size", Integer.toString(curSize));
            curResults.put("construct_time", Double.toString(constructTime.getTotalMs()));
            curResults.put("plan_time", Double.toString(planTime.getTotalMs()));
            results.add(curResults);
        }

        Path resultsDir = Paths.get(outputDir, "results", experiment);
        Files.createDirectories(resultsDir);
        Path resultsFile = resultsDir.resolve("load_time.csv");

        CSVOutput.writeAllResults(results, resultsFile.toString());
    }

    public static void main(String[] args) throws Exception {
        String confFile = args[0];
        RunConfig config = RunConfig.fromJsonFile(confFile);
        boolean quantile = config.get("quantile");
        if (quantile) {
            System.out.println("Starting Loader for Quantiles");
            LoadRunner<Double, DoubleList> loader = new LoadRunner<>(config);
            SimpleCSVDataSource<Double> xTrackSource = new SimpleCSVDataSourceDouble();
            LinearPlanner<DoubleList> planner = new LinearQuantilePlanner();
            SketchGenFactory<Double, DoubleList> sketchGenFactory = new QuantileSketchGenFactory();
            loader.runLinearLoad(
                    xTrackSource,
                    planner,
                    sketchGenFactory
            );
        } else {
            System.out.println("Starting Loader for Frequencies");
            LoadRunner<Long, LongList> loader = new LoadRunner<>(config);
            SimpleCSVDataSource<Long> xTrackSource = new SimpleCSVDataSourceLong();
            LinearPlanner<LongList> planner = new LinearFreqPlanner();
            SketchGenFactory<Long, LongList> sketchGenFactory = new FreqSketchGenFactory();
            loader.runLinearLoad(
                    xTrackSource,
                    planner,
                    sketchGenFactory
            );
        }

    }
}
