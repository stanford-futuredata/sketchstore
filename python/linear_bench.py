from collections import defaultdict

import numpy as np
import pandas as pd
from tqdm import tqdm
import json
import pickle

import sketch.compress_freq as cf
import sketch.frequent as f


class LinearBenchRunner:
    def __init__(
            self,
            size=20,
            num_segments=200,
    ):
        self.size = size
        self.num_segments = num_segments

    def run(self):
        print("Linear Benchmark")
        segments = gen_data(self.num_segments)

        compressors = [
            cf.TopValueCompressor(100),
            cf.RandomSampleCompressor(self.size),
            cf.TruncationCompressor(self.size),
            cf.PPSCompressor(self.size),
            # cf.HairCombCompressor(self.size),
            cf.IncrementalRangeCompressor(self.size),
        ]
        compressor_names = [
            "topvalue",
            "random_sample",
            "truncation",
            "pps",
            "incremental",
        ]

        sketches = [
            lambda: f.SpaceSavingSketch(size=self.size, unbiased=False),
            lambda: f.CountMinSketch(size=self.size, unbiased=False),
            lambda: f.CountMinSketch(size=self.size, unbiased=True),
        ]
        sketch_names = [
            "spacesaving",
            "cms_min",
            "cms_mean",
        ]
        results = []

        for cur_seg_idx, cur_seg in tqdm(enumerate(segments)):
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


def main():
    rr = LinearBenchRunner(size=20, num_segments=400)
    results = rr.run()
    with open("output/linear_bench.out", "w") as f:
        f.write(repr(results))


def gen_data(num_segments=10):
    r = np.random.RandomState(seed=0)
    segments = []
    for i in range(num_segments):
        # seg_size = r.geometric(.001)
        # shift = r.randint(10)
        seg_size = 1000
        shift = 0
        xs = r.zipf(1.1, size=seg_size)
        segments.append(xs + shift)
    return segments


if __name__ == "__main__":
    main()
