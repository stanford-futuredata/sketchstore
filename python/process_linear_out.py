import ast
import math
from collections import defaultdict
from typing import List, Dict, Sequence
import pickle

import numpy as np
import pandas as pd
from tqdm import tqdm

import sketch.compress_freq as cf
import sketch.sketch_frequent as f
import sketch.compress_dyadic as dyadic
import linear_bench


def combine_counts(counts: Sequence[Dict]):
    combined = dict()
    for cur_count in counts:
        for k,v in cur_count.items():
            combined[k] = combined.get(k, 0) + v
    return combined


def count_to_vec(x_to_track, x_counts: Dict) -> np.ndarray:
    counts = np.array([x_counts.get(x, 0) for x in x_to_track])
    return counts


def rmse(x):
    return np.sqrt(np.mean(x**2))


class LinearBenchProcessor:
    def __init__(
            self,
            x_to_track
    ):
        self.x_to_track = x_to_track

    def calc_range_query(
            self,
            summary_list: List[Dict],
            method_name: str,
            start_idx: int,
            end_idx: int
    ) -> np.ndarray:
        pass

    def calc_cum_query(
            self,
            summary_list: List[Dict],
            method_name: str,
            start_idx,
            end_idx
    ):
        cum_results = []
        if "dyadic" in method_name:
            dyadic_values = {}
            for cur_result in summary_list:
                cur_method = cur_result["method"]
                cur_idx = cur_result["seg_idx"]
                cur_counts = cur_result["counts"]

                if cur_method == method_name:
                    c_height, c_start_idx = cur_idx
                    if (start_idx <= c_start_idx < end_idx and
                        start_idx-1 <= c_start_idx - 2**c_height):
                        dyadic_values = {
                            (h, s_idx): counts for ((h, s_idx), counts) in dyadic_values.items()
                            if s_idx > c_start_idx or s_idx - 2**h < c_start_idx - 2**c_height
                        }
                        dyadic_values[(c_height, c_start_idx)] = cur_counts
                        new_final_count = count_to_vec(self.x_to_track, combine_counts(dyadic_values.values()))
                        # new_final_count = self.count_to_vec(combine_counts(dyadic_values.values()))
                        if (c_start_idx-start_idx) >= len(cum_results):
                            cum_results.append(new_final_count)
                        else:
                            cum_results[c_start_idx-start_idx] = new_final_count
        else:
            cur_total = 0
            for cur_result in summary_list:
                cur_method = cur_result["method"]
                cur_idx = cur_result["seg_idx"]
                cur_counts = cur_result["counts"]
                if cur_method == method_name and start_idx <= cur_idx < end_idx:
                    # cur_total += self.count_to_vec(cur_counts)
                    cur_total += count_to_vec(self.x_to_track, cur_counts)
                    cum_results.append(np.copy(cur_total))
        return cum_results


def run_grains(dataset="caida", x_track_file=None):
    grains = [8, 32, 128, 512, 2048]
    methods = [
        "incremental",
        "pps",
        "cms_min",
        "random_sample",
        "truncation",
        "dyadic_truncation",
        "topvalue",
        "zero_est"
    ]
    if x_track_file is None:
        x_to_track = list(range(1,401))
    else:
        x_df = pd.read_csv(x_track_file)
        x_to_track = x_df["x_track"].values
    x_to_track = np.sort(x_to_track)
    proc = LinearBenchProcessor(x_to_track=x_to_track)
    for cur_grain in grains:
        print("Grain: {}".format(cur_grain))
        cur_results = None
        with open("output/grain_{}_{}.out".format(dataset, cur_grain)) as f:
            cur_results = ast.literal_eval(f.read())

        start_idx = cur_grain//2
        end_idx = cur_grain
        cum_method_results = dict()
        for cur_method in methods:
            if cur_method == "zero_est":
                cum_results = [np.zeros(len(x_to_track)) for _ in range(end_idx-start_idx)]
            else:
                cum_results = proc.calc_cum_query(cur_results, cur_method, start_idx, end_idx)
                # cum_results = [
                #     list(i) for i in proc.calc_cum_query(cur_results, cur_method, start_idx, end_idx)
                # ]
            cum_method_results[cur_method] = cum_results
        with open("output/cum_{}_{}.out".format(dataset, cur_grain), "wb") as f:
            pickle.dump(cum_method_results, f, protocol=0)


def test():
    results = linear_bench.run_test_bench()
    proc = LinearBenchProcessor(x_to_track=range(1,6))
    cum_results = proc.calc_cum_query(results, "topvalue", 0, 4)
    print(cum_results)
    cum_results = proc.calc_cum_query(results, "dyadic_truncation", 0, 4)
    print(cum_results)


def main():
    # run_grains("caida", "notebooks/caida1M-xtrack.csv")
    run_grains("zipf", "notebooks/zipf10M-xtrack.csv")
    # run_grains("caida10M", "/Users/edwardgan/Documents/Projects/datasets/caida-pcap/caida10M-ipdst-xtrack.csv")


if __name__ == "__main__":
    main()
