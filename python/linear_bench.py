import math
from collections import defaultdict

import numpy as np
import pandas as pd
from tqdm import tqdm

import sketch.compress_freq as cf
import sketch.frequent as f
import sketch.dyadic as dyadic


class LinearBenchRunner:
    def __init__(
            self,
            size,
            segments,
            max_value_track=400
    ):
        self.size = size
        self.segments = segments
        self.max_value_track = max_value_track

    def run(self):
        print("Running Linear Bench with size: {} on {} segs".format(
            self.size,
            len(self.segments)
        ))

        compressors = [
            cf.TopValueCompressor(self.max_value_track),
            cf.RandomSampleCompressor(self.size),
            cf.TruncationCompressor(self.size),
            cf.HairCombCompressor(self.size),
            cf.IncrementalRangeCompressor(self.size),
        ]
        compressor_names = [
            "topvalue",
            "random_sample",
            "truncation",
            "pps",
            "incremental",
        ]

        dyadic_height = int(math.log2(len(self.segments)))
        dyadic_size = self.size/(dyadic_height+1)
        print("Dyadic Height: {}, Size:{}".format(dyadic_height, dyadic_size))
        dyadic_compressor = dyadic.DyadicFrequencyCompressor(
            size=dyadic_size,
            max_height=dyadic_height
        )

        sketches = [
            # lambda: f.SpaceSavingSketch(size=self.size, unbiased=False),
            lambda: f.CountMinSketchFast(size=self.size, unbiased=False, max_val=self.max_value_track),
            # lambda: f.CountMinSketchOld(size=self.size, unbiased=True),
        ]
        sketch_names = [
            # "spacesaving",
            "cms_min",
            # "cms_mean",
        ]
        results = []

        for cur_seg_idx, cur_seg in tqdm(enumerate(self.segments)):
            ec_current = f.ExactCounterSketch()
            ec_current.add(cur_seg)
            exact_dict = ec_current.get_dict()

            for compressor_idx, cur_compressor in enumerate(compressors):
                cur_compressor_name = compressor_names[compressor_idx]
                compressed_counts = cur_compressor.compress(item_dict=exact_dict)
                results.append({
                    "seg_idx": cur_seg_idx,
                    "method": cur_compressor_name,
                    "counts": compressed_counts,
                })

            dyadic_summaries = dyadic_compressor.compress(item_dict=exact_dict)
            for summ_height, cur_dyadic_summ in enumerate(dyadic_summaries):
                results.append({
                    "seg_idx": (summ_height, cur_seg_idx),
                    "method": "dyadic_truncation",
                    "counts": cur_dyadic_summ
                })

            for sketch_idx, cur_sketch_constructor in enumerate(sketches):
                cur_sketch_name = sketch_names[sketch_idx]
                cur_sketch = cur_sketch_constructor()
                cur_sketch.add(cur_seg)
                compressed_counts = cur_sketch.get_dict()
                results.append({
                    "seg_idx": cur_seg_idx,
                    "method": cur_sketch_name,
                    "counts": compressed_counts,
                })
        return results


def gen_data(num_segments=10, seg_size=1000, seed=0):
    r = np.random.RandomState(seed=seed)
    segments = []
    for i in range(num_segments):
        # seg_size = r.geometric(.001)
        # shift = r.randint(10)
        shift = 0
        xs = r.zipf(1.1, size=seg_size)
        segments.append(xs + shift)
    return segments


def run_single_bench():
    segments = gen_data(num_segments=256, seg_size=1024, seed=0)
    rr = LinearBenchRunner(size=64, segments=segments)
    results = rr.run()
    with open("output/linear_bench.out", "w") as f:
        f.write(repr(results))


def run_multi_grain():
    workload_granularities = [8, 32, 128, 512, 2048]
    total_space = 64*2048
    # r = np.random.RandomState(seed=0)
    # total_size = 1024*512
    # x_stream = r.zipf(1.1, size=total_size)
    df_in = pd.read_csv("notebooks/caida1M-dest-stream.csv")
    x_stream = df_in["Destination"].values
    data_name = "caida"
    for cur_granularity in workload_granularities:
        segments = np.array_split(x_stream, cur_granularity)
        sketch_size = total_space // cur_granularity
        rr = LinearBenchRunner(size=sketch_size, segments=segments)
        print("Running Grain: {}".format(cur_granularity))
        results = rr.run()
        with open("output/grain_{}_{}.out".format(data_name,cur_granularity), "w") as f:
            f.write(repr(results))


def run_test_bench():
    segments = [
        [1,1,2,3],
        [1,1,2,3],
        [2,2,3,4],
        [3,3,4,5]
    ]
    rr = LinearBenchRunner(size=2, segments=segments, max_value_track=5+1)
    results = rr.run()
    return results

def main():
    # run_single_bench()
    run_multi_grain()
    # run_test_bench()


if __name__ == "__main__":
    main()
