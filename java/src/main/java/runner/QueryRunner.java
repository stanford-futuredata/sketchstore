package runner;

import board.BoardGen;
import board.StoryBoard;
import board.planner.LinearFreqPlanner;
import board.query.LinearFreqAccProcessor;
import board.query.LinearSelector;
import board.query.QueryProcessor;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.impl.list.mutable.FastList;
import summary.ItemDictCompressorGen;
import summary.SketchGen;
import summary.accumulator.ExactFreqAccumulator;
import summary.compressor.CoopFreqCompressor;
import tech.tablesaw.api.Table;
import tech.tablesaw.columns.Column;

import javax.sound.sampled.Line;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class QueryRunner {
    RunConfig config;

    String experiment;
    boolean quantile;
    String outputDir;
    String xToTrackPath;
    int numQueries;

    int granularity;
    List<String> sketchNames;
    List<Integer> sizes;


    public QueryRunner(RunConfig config) {
        this.config = config;

        experiment = config.get("experiment");
        quantile = config.get("quantile");
        outputDir = config.get("out_dir");

        granularity = config.get("granularity");
        sizes = config.get("sizes");
        sketchNames = config.get("sketches");

        xToTrackPath = config.get("x_to_track");
        numQueries = config.get("num_queries");
    }

    public void run() throws Exception {
        Path boardDir = Paths.get(outputDir, experiment, "boards");
        String boardPath = String.format("%s/%s",
                boardDir,
                IOUtil.getBoardName(
                    sketchNames.get(0),
                    0,
                    sizes.get(0),
                    granularity
        ));
        File fIn = new File(boardPath);
        StoryBoard<Long> board = IOUtil.loadBoard(fIn);

        Table t = IOUtil.loadTable(xToTrackPath, Lists.fixedSize.of(0));
        Column<Long> col = (Column<Long>)t.column("x_to_track");
        List<Long> xToTrack = col.asList();

        LinearSelector selector;
        QueryProcessor<Long> processor;
        LinearFreqAccProcessor p_raw = new LinearFreqAccProcessor(
                new ExactFreqAccumulator()
        );
        selector = p_raw;
        processor = p_raw;

        selector.setRange(0,4);
        FastList<Double> queryResults = processor.query(board, xToTrack);
        System.out.println(queryResults);
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Starting Loader");
        String confFile = args[0];
        RunConfig config = RunConfig.fromJsonFile(confFile);
        QueryRunner runner = new QueryRunner(config);
        runner.run();
    }
}
