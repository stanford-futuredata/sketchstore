package runner;

import board.BoardGen;
import board.StoryBoard;
import board.planner.LinearFreqPlanner;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.primitive.LongList;
import summary.ItemDictCompressorGen;
import summary.SketchGen;
import summary.compressor.CoopFreqCompressor;
import tech.tablesaw.api.Table;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class LoadRunner {
    RunConfig config;

    String experiment;
    boolean quantile;
    String csvPath;
    List<Integer> colTypes;
    String metricCol;
    String outputDir;

    int granularity;
    List<String> sketchNames;
    List<Integer> sizes;

    public LoadRunner(RunConfig config) {
        this.config = config;

        experiment = config.get("experiment");
        quantile = config.get("quantile");
        csvPath = config.get("csv_path");
        colTypes = config.get("col_types");
        metricCol = config.get("metric_col");
        outputDir = config.get("out_dir");

        granularity = config.get("granularity");
        sizes = config.get("sizes");
        sketchNames = config.get("sketches");
    }

    public void runForParam(
            Table t,
            String curSketchName,
            int curGranularity,
            int curSize
    ) throws IOException {
        SketchGen<Long, LongList> sGen = new ItemDictCompressorGen(
                new CoopFreqCompressor(0)
        );

        LinearFreqPlanner planner = new LinearFreqPlanner(
                curGranularity,
                curSize
        );
        List<String> dimCols = Lists.fixedSize.empty();
        planner.plan(
                t, metricCol, dimCols
        );

        BoardGen<Long, LongList> bGen = new BoardGen<>(sGen);
        StoryBoard<Long> board = bGen.generate(
                planner.getSegments(),
                planner.getDimensions(),
                planner.getSizes(),
                planner.getBiases()
        );

        Path boardDir = Paths.get(outputDir, experiment, "boards");
        Files.createDirectories(boardDir);
        String outputPath = String.format("%s/%s",
                boardDir.toString(),
                IOUtil.getBoardName(
                        curSketchName,
                        0,
                        curSize,
                        curGranularity
                ));
        File outFile = new File(outputPath);
        IOUtil.writeBoard(board, outFile);
    }

    public void run() throws Exception {
        Table t = IOUtil.loadTable(csvPath, colTypes);
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Starting Loader");
        String confFile = args[0];
        RunConfig config = RunConfig.fromJsonFile(confFile);
        LoadRunner loader = new LoadRunner(config);
        loader.run();
    }
}
