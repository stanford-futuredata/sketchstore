import unittest
import numpy as np
import pandas as pd
import math

import sketch.sketch_gen as board_sketch
import storyboard.board_gen as board_gen
import storyboard.board_query as board_query
import sketch.compress_dyadic as cd


class TestBoardQuery(unittest.TestCase):
    def test_dyadic(self):
        x_stream = np.random.zipf(1.1, size=10_000)
        cur_granularity = 128
        sketch_size = 64
        segments = np.array_split(x_stream, cur_granularity)

        sketch_gen = board_sketch.DyadicItemDictGen(
            h_compressor=cd.DyadicFrequencyCompressor(max_height=20)
        )
        dyadic_height = int(math.log2(len(segments)))
        sketch_size /= (dyadic_height + 1)
        print("Dyadic Height: {}, Size:{}".format(dyadic_height, sketch_size))
        board_constructor = board_gen.BoardGen(sketch_gen)
        segment_times = np.cumsum([len(cur_seg) for cur_seg in segments])
        df = board_constructor.generate(
            segments=segments,
            tags=[{
                "t": t, "size": sketch_size
            } for t in segment_times],
        )
        tot_results, tot_sum = board_query.query_linear_dyadic(
            df,
            seg_start=1,
            seg_end=7,
            x_to_track=list(range(10)),
            quantile=False,
            dyadic_base=2,
        )
        print(tot_results)
        print(tot_sum)
