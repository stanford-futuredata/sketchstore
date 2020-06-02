package runner;

import board.StoryBoard;
import io.IOUtil;
import summary.Sketch;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class BoardDebug {
    public static void main(String[] args) throws Exception {
        String confFile = args[0];
        RunConfig config = RunConfig.fromJsonFile(confFile);
        String outputDir = config.get("out_dir");
        String experiment = config.get("experiment");
        List<Integer> sizes = config.get("sizes");
        int granularity = config.get("granularity", 0);

        String curSketch = "cms_min";
        int curSize = sizes.get(0);

        Path boardDir = Paths.get(outputDir, "boards", experiment);
        String boardPath = String.format("%s/%s",
                boardDir,
                IOUtil.getBoardName(
                        curSketch,
                        curSize,
                        granularity
                ));
        File fIn = new File(boardPath);
        StoryBoard<Long> board = IOUtil.loadBoard(fIn);

        int nCols = board.dimensionCols.size();
        int nRows = board.dimensionCols.get(0).size();
        int[] match = {0, 0, 0, 0};
//        int[] match = {1, 5, 9, 2};

        for (int i = 0; i < nRows; i++) {
            boolean matches = true;
            for (int j = 0; j < nCols; j++) {
                if (board.dimensionCols.get(j).get(i) != match[j]) {
                    matches = false;
                    break;
                }
            }
            if (matches) {
                Sketch<Long> sketchObj =board.sketchCol.get(i);
                System.out.println("Size: "+sketchObj.size());
            }
        }
    }
}
