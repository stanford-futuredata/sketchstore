package runner;

import board.StoryBoard;
import board.query.LinearFreqAccProcessor;
import board.query.LinearSelector;
import board.query.QueryProcessor;
import io.IOUtil;
import io.SimpleCSVDataSource;
import io.SimpleCSVDataSourceLong;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.impl.list.mutable.FastList;
import summary.accumulator.ExactFreqAccumulator;
import tech.tablesaw.api.Table;
import tech.tablesaw.columns.Column;

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

    public void run() throws Exception {
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

        for (String curSketch: sketches) {
            String boardPath = String.format("%s/%s",
                    boardDir,
                    IOUtil.getBoardName(
                            curSketch,
                            curSize,
                            granularity
                    ));
            File fIn = new File(boardPath);
            StoryBoard<Long> board = IOUtil.loadBoard(fIn);

            selector.setRange(0, 4);

            FastList<Double> queryResults = processor.query(board, xToTrack);
            System.out.println(curSketch);
            System.out.println(queryResults);
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Starting Query Runner");
        String confFile = args[0];
        RunConfig config = RunConfig.fromJsonFile(confFile);
        QueryRunner runner = new QueryRunner(config);
        runner.run();
    }
}
