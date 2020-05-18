package runner;

import board.BoardGen;
import board.StoryBoard;
import board.planner.LinearFreqPlanner;
import board.planner.LinearPlanner;
import board.planner.LinearQuantilePlanner;
import io.IOUtil;
import io.SimpleCSVDataSource;
import io.SimpleCSVDataSourceDouble;
import io.SimpleCSVDataSourceLong;
import org.eclipse.collections.api.PrimitiveIterable;
import org.eclipse.collections.api.list.primitive.DoubleList;
import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.impl.list.mutable.FastList;
import summary.gen.QuantileSketchGenFactory;
import summary.gen.SketchGen;
import summary.gen.FreqSketchGenFactory;
import summary.gen.SketchGenFactory;
import tech.tablesaw.api.Table;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

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
        Path boardDir = Paths.get(outputDir, experiment, "boards");
        Files.createDirectories(boardDir);

        xTrackSource.setHasHeader(true);
        FastList<T> xToTrack = xTrackSource.get(
                xToTrackPath, 0
        );

        int curGranularity = granularity;
        int curSize = sizes.get(0);
        planner.plan(
                t, metricCol, curGranularity, curSize
        );

        for (String curSketch: sketches) {
            SketchGen<T, TL> sGen = sketchGenFactory.getSketchGen(curSketch, xToTrack);
            BoardGen<T, TL> bGen = new BoardGen<>(sGen);
            StoryBoard<T> board = bGen.generate(
                    planner.getSegments(),
                    planner.getDimensions(),
                    planner.getSizes(),
                    planner.getBiases()
            );

            String outputPath = String.format("%s/%s",
                    boardDir.toString(),
                    IOUtil.getBoardName(
                            curSketch,
                            curSize,
                            curGranularity
                    ));
            File outFile = new File(outputPath);
            IOUtil.writeBoard(board, outFile);
        }
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
