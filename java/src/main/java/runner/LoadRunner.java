package runner;

import board.BoardGen;
import board.StoryBoard;
import board.planner.LinearFreqPlanner;
import io.IOUtil;
import io.SimpleCSVDataSource;
import io.SimpleCSVDataSourceDouble;
import io.SimpleCSVDataSourceLong;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.impl.list.mutable.FastList;
import summary.ItemDictCompressorGen;
import summary.SketchGen;
import summary.SketchUtil;
import summary.compressor.CoopFreqCompressor;
import tech.tablesaw.api.Table;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class LoadRunner {
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

    public void runQuantile() throws Exception {
        throw new Exception("Not Implemented");
    }

    public void runFreq() throws Exception {
        Table t = IOUtil.loadTable(csvPath, colTypes);
        Path boardDir = Paths.get(outputDir, experiment, "boards");
        Files.createDirectories(boardDir);

        SimpleCSVDataSource<Long> xTrackSource = new SimpleCSVDataSourceLong(xToTrackPath, 0);
        xTrackSource.setHasHeader(true);
        FastList<Long> xToTrack = xTrackSource.get();

        int curGranularity = granularity;
        int curSize = sizes.get(0);
        LinearFreqPlanner planner = new LinearFreqPlanner(
                curGranularity,
                curSize
        );
        List<String> dimCols = Lists.fixedSize.empty();
        planner.plan(
                t, metricCol, dimCols
        );

        for (String curSketch: sketches) {
            SketchGen<Long, LongList> sGen = SketchUtil.getFreqSketchGen(curSketch, xToTrack);
            BoardGen<Long, LongList> bGen = new BoardGen<>(sGen);
            StoryBoard<Long> board = bGen.generate(
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
        System.out.println("Starting Loader");
        String confFile = args[0];
        RunConfig config = RunConfig.fromJsonFile(confFile);
        boolean quantile = config.get("quantile");
        LoadRunner loader = new LoadRunner(config);
        if (quantile) {
            loader.runQuantile();
        } else {
            loader.runFreq();
        }
    }
}
