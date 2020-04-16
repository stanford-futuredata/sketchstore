package runner;

import board.BoardGen;
import board.SketchBoard;
import board.planner.LinearFreqPlanner;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.impl.list.mutable.FastList;
import summary.ItemDictCompressorGen;
import summary.SketchGen;
import summary.compressor.CoopFreqCompressor;
import tech.tablesaw.api.Table;

import java.io.File;
import java.util.List;

public class LoadBoard {
    RunConfig config;

    boolean quantile;
    String csvPath;
    List<Integer> colTypes;
    String metricCol;
    String outputPath;

    int granularity;
    List<Integer> sizes;

    public LoadBoard(RunConfig config) {
        this.config = config;

        quantile = config.get("quantile");
        csvPath = config.get("csv_path");
        colTypes = config.get("col_types");
        metricCol = config.get("metric_col");
        outputPath = config.get("output_path");

        granularity = config.get("granularity");
        sizes = config.get("sizes");
    }

    public void run() throws Exception {
        Table t = IOUtil.loadTable(csvPath, colTypes);
        LinearFreqPlanner planner = new LinearFreqPlanner(
                granularity,
                sizes.get(0)
        );
        List<String> dimCols = Lists.fixedSize.<String>empty();
        planner.plan(
                t, metricCol, dimCols
        );

        SketchGen<Long, LongList> sGen = new ItemDictCompressorGen(
                new CoopFreqCompressor(0)
        );
        BoardGen<Long, LongList> bGen = new BoardGen<>(sGen);
        SketchBoard<Long> board = bGen.generate(
                planner.getSegments(),
                planner.getDimensions(),
                planner.getSizes(),
                planner.getBiases()
        );

        File outFile = new File(outputPath);
        IOUtil.writeBoard(board, outFile);
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Starting Loader");
        String confFile = args[0];
        RunConfig config = RunConfig.fromJsonFile(confFile);
        LoadBoard loader = new LoadBoard(config);
        loader.run();
    }
}
