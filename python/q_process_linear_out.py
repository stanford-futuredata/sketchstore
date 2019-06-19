import ast
import math
from collections import defaultdict
from typing import List, Dict, Sequence
import pickle

import numpy as np
import pandas as pd
from tqdm import tqdm

import q_linear_bench
import sketch.dyadic as dyadic


def combine_counts(counts: Sequence[Dict]):
    combined = dict()
    for cur_count in counts:
        for k,v in cur_count.items():
            combined[k] = combined.get(k, 0) + v
    return combined


def rmse(x):
    return np.sqrt(np.mean(x**2))


def dict_to_arrays(x_counts: Dict):
    x_keys = []
    x_weight = []
    for k,v in x_counts.items():
        x_keys.append(k)
        x_weight.append(v)
    return np.array(x_keys), np.array(x_weight)


def count_to_vec(x_to_track: Sequence, x_counts:Dict):
    x_keys, x_weight = dict_to_arrays(x_counts)

    bin_edges = np.concatenate([[-np.inf], x_to_track])
    bin_weights, _ = np.histogram(-x_keys, -bin_edges[::-1], weights=x_weight)
    return np.cumsum(bin_weights[::-1])

    # counts = np.zeros(len(x_to_track))
    # for i in range(len(x_to_track)):
    #     cur_x = x_to_track[i]
    #     counts[i] = np.sum(x_weight[x_keys <= cur_x])
    # return counts


class QuantileLinearBenchProcessor:
    def __init__(
            self,
            x_to_track
    ):
        self.x_to_track = x_to_track

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
                        new_final_count = count_to_vec(
                            self.x_to_track,
                            combine_counts(dyadic_values.values())
                        )
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
                    cur_total += count_to_vec(self.x_to_track, cur_counts)
                    cum_results.append(np.copy(cur_total))
        return cum_results


def run_grains(data_name):
    grains = [8, 32, 128, 512, 2048]
    methods = [
        "ranktrack",
        "coop",
        "skip",
        "pps",
        "zero_est",
        "random_sample",
        "dyadic_truncation"
    ]
    x_to_track = np.linspace(0,1,501)
    proc = QuantileLinearBenchProcessor(x_to_track=x_to_track)
    for cur_grain in grains:
        print("Grain: {}".format(cur_grain))
        cur_results = None
        with open("output/grain_{}_{}.out".format(data_name,cur_grain), "rb") as f:
            cur_results = pickle.load(f)

        start_idx = cur_grain//2
        end_idx = cur_grain
        cum_method_results = dict()
        for cur_method in methods:
            if cur_method == "zero_est":
                cum_results = [np.zeros(len(x_to_track)) for i in range(end_idx-start_idx)]
            else:
                cum_results = proc.calc_cum_query(cur_results, cur_method, start_idx, end_idx)
                # cum_results = [
                #     list(i) for i in proc.calc_cum_query(cur_results, cur_method, start_idx, end_idx)
                # ]
            cum_method_results[cur_method] = cum_results
        with open("output/cum_{}_{}.out".format(data_name,cur_grain), "wb") as f:
            pickle.dump(cum_method_results, f, protocol=0)


def test():
    results = q_linear_bench.run_test_bench()
    proc = QuantileLinearBenchProcessor(x_to_track=[1,3])
    cum_results = proc.calc_cum_query(results, "ranktrack", 0, 4)
    print(cum_results)
    cum_results = proc.calc_cum_query(results, "coop", 0, 4)
    print(cum_results)


def main():
    run_grains("qrand")


if __name__ == "__main__":
    main()
