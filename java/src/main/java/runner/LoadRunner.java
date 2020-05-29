package runner;

import board.BoardGen;
import board.StoryBoard;
import board.planner.*;
import io.*;
import org.eclipse.collections.api.PrimitiveIterable;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.primitive.DoubleList;
import org.eclipse.collections.api.list.primitive.IntList;
import org.eclipse.collections.api.list.primitive.LongList;
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

    List<String> sketches;
    List<Integer> sizes;

    int granularity;

    boolean isCube;
    List<String> dimensionCols;
    double workloadProb;

    public LoadRunner(RunConfig config) {
        this.config = config;

        experiment = config.get("experiment");
        quantile = config.get("quantile");
        csvPath = config.get("csv_path");
        xToTrackPath = config.get("x_to_track");
        colTypes = config.get("col_types");
        metricCol = config.get("metric_col");
        outputDir = config.get("out_dir");

        sizes = config.get("sizes");
        sketches = config.get("sketches");

        // Linear
        granularity = config.get("granularity", 0);
        // Cube
        dimensionCols = config.get("dimension_cols", Lists.mutable.empty());
        workloadProb = config.get("workload_prob", -1.0);
        isCube = !dimensionCols.isEmpty();
    }

    public Map<String, Object> getPlannerParams() {
        Map<String, Object> params = new HashMap<>();
        if (isCube) {
            params.put("dimension_cols", dimensionCols);
            params.put("workload_prob", workloadProb);
        } else {
            params.put("num_segments", granularity);
        }
        return params;
    }

    public void runLoad(
            SimpleCSVDataSource<T> xTrackSource,
            Planner<TL> planner,
            SketchGenFactory<T, TL> sketchGenFactory
    ) throws Exception {
        Table t = IOUtil.loadTable(csvPath, colTypes);
        Path boardDir = Paths.get(outputDir, "boards", experiment);
        Files.createDirectories(boardDir);

        xTrackSource.setHasHeader(true);
        FastList<T> xToTrack = xTrackSource.get(
                xToTrackPath, 0
        );

        Map<String, Object> plannerParams = getPlannerParams();
        int curSize = sizes.get(0);
        planner.setParams(plannerParams);
        planner.plan(t, metricCol);

        FastList<Map<String, String>> results = new FastList<>();

        for (String curSketch: sketches) {
            System.out.println("Loading: "+curSketch);

            Timer optimizeTimer = new Timer();
            PlanOptimizer<TL> planOptimizer = sketchGenFactory.getPlanOptimizer(
                    curSketch,
                    isCube
            );
            planOptimizer.setParams(plannerParams);
            optimizeTimer.start();
            planOptimizer.optimizePlan(
                    planner.getSegments(),
                    planner.getDimensions(),
                    curSize
            );
            optimizeTimer.end();

//            System.out.println("Generating with sizes: ");
//            System.out.println(planOptimizer.getSpaces());
            LongList biases = planOptimizer.getBiases();
            IntList spaces = planOptimizer.getSpaces();
            System.out.print("space: ");
            for (int i = 0; i < 10; i++) {
                System.out.print(spaces.get(i)+" ");
            }
            System.out.println();
            System.out.print("bias: ");
            for (int i = 0; i < 10; i++) {
                System.out.print(biases.get(i)+" ");
            }
            System.out.println();

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
                    planOptimizer.getSpaces(),
                    planOptimizer.getBiases()
            );
            constructTime.end();

            String outputPath = String.format("%s/%s",
                    boardDir.toString(),
                    IOUtil.getBoardName(
                            curSketch,
                            curSize,
                            granularity
                    ));
            File outFile = new File(outputPath);
            IOUtil.writeBoard(board, outFile);

            HashMap<String, String> curResults = new HashMap<>();
            curResults.put("sketch", curSketch);
            curResults.put("size", Integer.toString(curSize));
            curResults.put("construct_time", Double.toString(constructTime.getTotalMs()));
            curResults.put("plan_time", Double.toString(optimizeTimer.getTotalMs()));
            plannerParams.forEach((String k, Object v) -> {
                if (v instanceof Number) {
                    curResults.put(k, v.toString());
                }
            });
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
        System.out.println("Starting Loader");
//        System.in.read();
        if (quantile) {
            System.out.println("Quantiles");
            LoadRunner<Double, DoubleList> loader = new LoadRunner<>(config);
            SimpleCSVDataSource<Double> xTrackSource = new SimpleCSVDataSourceDouble();
            Planner<DoubleList> planner;
            if (loader.isCube) {
                planner = new CubeQuantilePlanner();
            } else {
                planner = new LinearQuantilePlanner();
            }
            SketchGenFactory<Double, DoubleList> sketchGenFactory = new QuantileSketchGenFactory();
            loader.runLoad(
                    xTrackSource,
                    planner,
                    sketchGenFactory
            );
        } else {
            System.out.println("Frequencies");
            LoadRunner<Long, LongList> loader = new LoadRunner<>(config);
            SimpleCSVDataSource<Long> xTrackSource = new SimpleCSVDataSourceLong();
            Planner<LongList> planner;
            if (loader.isCube) {
                planner = new CubeFreqPlanner();
            } else {
                planner = new LinearFreqPlanner();
            }
            SketchGenFactory<Long, LongList> sketchGenFactory = new FreqSketchGenFactory();
            loader.runLoad(
                    xTrackSource,
                    planner,
                    sketchGenFactory
            );
        }

    }
}
