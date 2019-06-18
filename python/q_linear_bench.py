import math
from collections import defaultdict

import numpy as np
import pandas as pd
from tqdm import tqdm
import pickle

import sketch.compress_quant as cq
import sketch.quantile as q


class QuantileLinearBenchRunner:
    def __init__(
            self,
            size,
            segments,
            x_to_track=None
    ):
        self.size = size
        self.segments = segments
        if x_to_track is not None:
            self.x_to_track = x_to_track
        else:
            self.x_to_track = [0]

    def run(self):
        print("Running Quantile Linear Bench with size: {} on {} segs".format(
            self.size,
            len(self.segments)
        ))

        compressors = [
            cq.RankTracker(x_tracked=self.x_to_track),
            cq.CoopCompressor(self.size),
            cq.SkipCompressor(self.size, biased=False),
            cq.SkipCompressor(self.size, biased=True)
        ]
        compressor_names = [
            "ranktrack",
            "coop",
            "pps",
            "skip"
        ]

        results = []

        for cur_seg_idx, cur_seg in tqdm(enumerate(self.segments)):
            for compressor_idx, cur_compressor in enumerate(compressors):
                cur_seg = np.sort(cur_seg)
                cur_compressor_name = compressor_names[compressor_idx]
                compressed_counts = cur_compressor.compress(cur_seg)
                results.append({
                    "seg_idx": cur_seg_idx,
                    "method": cur_compressor_name,
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
        xs = r.uniform(0, 1, seg_size)
        segments.append(xs + shift)
    return segments


def run_single_bench():
    segments = gen_data(num_segments=256, seg_size=1024, seed=0)
    rr = QuantileLinearBenchRunner(size=32, segments=segments, x_to_track=np.linspace(0, 1, 101))
    results = rr.run()
    with open("output/q_linear_bench.out", "wb") as f:
        pickle.dump(results, f)


def run_multi_grain():
    workload_granularities = [8, 32, 128, 256, 512, 2048]
    # workload_granularities = [8, 32, 128, 256]
    total_space = 128*32
    x_stream = np.random.uniform(0,1,2_000_000)
    data_name = "qrand"
    for cur_granularity in workload_granularities:
        segments = np.array_split(x_stream, cur_granularity)
        sketch_size = total_space // cur_granularity
        rr = QuantileLinearBenchRunner(size=sketch_size, segments=segments, x_to_track=np.linspace(0,1,501))
        print("Running Grain: {}".format(cur_granularity))
        results = rr.run()
        with open("output/grain_{}_{}.out".format(data_name,cur_granularity), "wb") as f:
            pickle.dump(results, f)


def run_test_bench():
    segments = [
        [1,1,2,3],
        [1,1,2,3],
        [2,2,3,4],
        [3,3,4,5]
    ]
    rr = QuantileLinearBenchRunner(size=2, segments=segments, x_to_track=[1, 3])
    results = rr.run()
    return results

def main():
    # run_single_bench()
    # run_test_bench()
    run_multi_grain()


if __name__ == "__main__":
    main()
