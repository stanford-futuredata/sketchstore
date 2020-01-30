import unittest
import numpy as np
import pandas as pd
import math

import sketch.sketch_gen as board_sketch
import storyboard.board_gen as board_gen
import sketch.compress_dyadic as cd
import sketch.compress_freq as cf
import sketch.compress_quant as cq
import storyboard.query_cy as board_query


class TestBoardQuery(unittest.TestCase):
    def test_acc_gk(self):
        np.random.seed(0)
        x_stream = np.random.uniform(0,1,10000)
        cur_granularity = 128
        sketch_size = 64
        segments = np.array_split(x_stream, cur_granularity)
        sketch_gen = board_sketch.SeqDictCompressorGen(
            name="pps",
            compressor=cq.SkipCompressor()
        )
        board_constructor = board_gen.BoardGen(sketch_gen)
        segment_times = np.cumsum([len(cur_seg) for cur_seg in segments])
        df = board_constructor.generate(
            segments=segments,
            tags=[{
                "t": t, "size": sketch_size
            } for t in segment_times],
        )
        x_to_track = np.linspace(0,1,10)
        tot_results_true = board_query.query_linear(
            df,
            seg_start=1,
            seg_end=7,
            x_to_track=x_to_track,
            quantile=1,
            dyadic_base=0,
        )
        tot_results_est = board_query.query_linear_acc_quant(
            df,
            seg_start=1,
            seg_end=7,
            x_to_track=x_to_track,
            acc_size=20,
        )
        print(tot_results_true)
        print(tot_results_est)


    def test_acc_mg(self):
        x_stream = np.random.zipf(1.1, size=100_000)
        cur_granularity = 128
        sketch_size = 64
        segments = np.array_split(x_stream, cur_granularity)
        sketch_gen = board_sketch.ItemDictCompressorGen(
            name="coop",
            compressor=cf.IncrementalRangeCompressor()
        )
        board_constructor = board_gen.BoardGen(sketch_gen)
        segment_times = np.cumsum([len(cur_seg) for cur_seg in segments])
        df = board_constructor.generate(
            segments=segments,
            tags=[{
                "t": t, "size": sketch_size
            } for t in segment_times],
        )
        tot_results_true = board_query.query_linear(
            df,
            seg_start=1,
            seg_end=7,
            x_to_track=np.arange(10),
            quantile=0,
            dyadic_base=0,
        )
        tot_results_est = board_query.query_linear_mg(
            df,
            seg_start=1,
            seg_end=7,
            x_to_track=np.arange(10),
            acc_size=20,
        )
        self.assertEqual(tot_results_true[1], tot_results_est[1])
        print(tot_results_true)
        print(tot_results_est)


    def test_dyadic(self):
        x_stream = np.random.zipf(1.1, size=100_000)
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
        tot_results = board_query.query_linear_dyadic(
            df,
            seg_start=1,
            seg_end=7,
            x_to_track=np.arange(10),
            quantile=False,
            dyadic_base=2,
        )
        self.assertGreater(tot_results[1], 420)
        self.assertLess(tot_results[1], 480)
        print(tot_results)
