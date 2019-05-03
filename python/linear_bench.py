from collections import defaultdict

import numpy as np
import pandas as pd
from tqdm import tqdm

import sketch.compress_freq as cf
import sketch.frequent as f


class LinearBenchRunner:
    def __init__(self):
        self.size = 10
        self.check_points = [1, 5, 10, 15, 20, 50]

    def gen_results(
            self,
            summary_name: str,
            seg_count: int,
            counts,
    ):
        results = []
        for checkpoint in self.check_points:
            cur_result = {
                "merge_type": "linear",
                "summary": summary_name,
                "size": self.size,
                "n_segments": seg_count,
                "check_point": checkpoint,
                "count": counts.get(checkpoint, 0.0)
            }
            results.append(cur_result)
        return results

    def run(self):
        print("Linear Benchmark")
        segments = gen_data(1000)
        compressors = [
            cf.IncrementalRangeCompressor(),
            cf.TruncationCompressor(),
            cf.RandomSampleCompressor(),
            cf.PPSCompressor(),
        ]
        compressor_names = [
            "incremental",
            "truncation",
            "random_sample",
            "pps",
        ]
        results = []
        ss_total = f.SpaceSavingSketch(size=self.size, unbiased=False)
        ec_total = f.ExactCounterSketch()

        compressed_totals = [
            f.ExactCounterSketch() for i in range(len(compressors))
        ]
        for cur_seg_idx, cur_seg in tqdm(enumerate(segments)):
            ec_total.add(cur_seg)
            ss_total.add(cur_seg)
            results += self.gen_results(
                "exact",
                seg_count=cur_seg_idx + 1,
                counts=ec_total.get_dict(),
            )
            results += self.gen_results(
                "spacesaving",
                seg_count=cur_seg_idx + 1,
                counts=ss_total.get_dict(),
            )

            ec_current = f.ExactCounterSketch()
            ec_current.add(cur_seg)
            exact_dict = ec_current.get_dict()

            for compressor_idx in range(len(compressors)):
                cur_compressor = compressors[compressor_idx]
                cur_compressor_name = compressor_names[compressor_idx]
                cur_compressor_totals = compressed_totals[compressor_idx]
                compressed_counts = cur_compressor.compress(
                    item_dict=exact_dict,
                    new_size=self.size,
                )
                cur_compressor_totals.update(compressed_counts)
                results += self.gen_results(
                    cur_compressor_name,
                    seg_count=cur_seg_idx + 1,
                    counts=cur_compressor_totals.get_dict()
                )
        return results


def main():
    rr = LinearBenchRunner()
    results = rr.run()
    r_df = pd.DataFrame(results)
    r_df.to_csv("output/linear_bench.csv", index=False)


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
