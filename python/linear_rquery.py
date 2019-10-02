import math
import os
from typing import List, Tuple

import numpy as np
import pandas as pd
from tqdm import tqdm

import sketch.sketch_gen as board_sketch
import storyboard.board_gen as board_gen
# import storyboard.board_query as board_query
import storyboard.query_cy as board_query
import linear_board


def gen_workload(
        granularity: int,
        seed: int = 0,
        num_queries: int = 100
):
    workload = []
    cur_query_len = 1
    r = np.random.RandomState(seed)
    while cur_query_len < granularity:
        start_idxs = r.randint(0, granularity - cur_query_len, size=num_queries)
        workload.extend([(start_idx, start_idx + cur_query_len) for start_idx in start_idxs])
        cur_query_len *= 2
    return workload


def run_workload(
        workload: List[Tuple],
        x_to_track: np.ndarray,
        true_board: pd.DataFrame,
        est_board: pd.DataFrame,
        totals_df: pd.DataFrame,
        sketch_name: str,
        quantile: bool,
):
    results = []
    dyadic_base = -1
    if "dyadic" in sketch_name:
        dyadic_base = linear_board.get_dyadic_base(sketch_name)
    for start_idx, end_idx in tqdm(workload):
        true_counts = board_query.query_linear(
            true_board, seg_start=start_idx, seg_end=end_idx, x_to_track=x_to_track,
            quantile=quantile, dyadic_base=-1)
        est_counts = board_query.query_linear(
            est_board, seg_start=start_idx, seg_end=end_idx, x_to_track=x_to_track,
            quantile=quantile, dyadic_base=dyadic_base)
        true_tot = board_query.query_linear_tot(totals_df, start_idx, end_idx)
        cur_results = board_query.calc_errors(true_counts, est_counts)
        cur_results["start_idx"] = start_idx
        cur_results["end_idx"] = end_idx
        cur_results["query_len"] = end_idx - start_idx
        cur_results["sketch"] = sketch_name
        cur_results["total"] = true_tot
        results.append(cur_results)
    return results


def calc_results(
        data_name: str,
        granularity: int,
        sketch_name: str,
        sketch_size: int,
        baseline_size: int,
        quantile: bool,
):
    if quantile:
        true_sketch= "q_top_values"
    else:
        true_sketch = "top_values"
    x_to_track = linear_board.get_tracked(data_name)
    true_file = linear_board.get_file_name(
        data_name=data_name,
        granularity=granularity,
        sketch_name=true_sketch,
        sketch_size=baseline_size,
    )
    sketch_file = linear_board.get_file_name(
        data_name=data_name,
        granularity=granularity,
        sketch_name=sketch_name,
        sketch_size=sketch_size,
    )
    true_board = pd.read_pickle(true_file)
    totals_df = pd.read_csv(
        linear_board.get_totals_name(data_name, granularity=granularity)
    )
    workload = gen_workload(granularity, seed=0, num_queries=20)
    print("Estimating: {}".format(sketch_name))

    cur_board = pd.read_pickle(sketch_file)
    cur_results = run_workload(workload, x_to_track=x_to_track,
                               true_board=true_board, est_board=cur_board, totals_df=totals_df,
                               sketch_name=sketch_name,
                               quantile=quantile)

    results_df = pd.DataFrame(cur_results)
    results_df["dataset"] = data_name
    results_df["granularity"] = granularity
    results_df["quantile"] = quantile
    out_dir, _ = os.path.split(true_file)
    out_file = os.path.join(out_dir, "{}_{}_errors.csv".format(sketch_name, sketch_size))
    results_df.to_csv(out_file, index=False)
    return results_df


experiment_runs = [
    {
        "data_name": "caida_10M",
        "quantile": False,
        "granularity": 2048,
        "baseline_size": 64,
        "sketch_sizes": {
            "cooperative": 64,
            "random_sample": 64,
            "cms_min": 64,
            "truncation": 64,
            "pps": 64,
            "dyadic_b2": 5,
            "dyadic_b3": 9
        }
    }, # 0
    {
        "data_name": "uniform_1M",
        "quantile": True,
        "granularity": 2048,
        "baseline_size": 64,
        "sketch_sizes": {
            "q_cooperative": 64,
            "q_random_sample": 64,
            "q_truncation": 64,
            "q_pps": 64,
            "kll": 64,
            "q_dyadic_b2": 5,
            "q_dyadic_b3": 9
        }
    },  # 1

]


def main():
    experiment_id = 0
    cur_experiment = experiment_runs[experiment_id]
    data_name = cur_experiment["data_name"]
    granularity = cur_experiment["granularity"]
    baseline_size = cur_experiment["baseline_size"]
    sketch_sizes = cur_experiment["sketch_sizes"]
    quantile = cur_experiment["quantile"]
    cur_sketches = [
        # "q_cooperative", "q_random_sample", "kll", "q_truncation", "q_pps", "q_dyadic_b2"
        "cooperative", "random_sample", "cms_min", "truncation", "pps", "dyadic_b2"
    ]
    for cur_sketch in cur_sketches:
        results_df = calc_results(
            data_name=data_name,
            granularity=granularity,
            sketch_name=cur_sketch,
            sketch_size=sketch_sizes[cur_sketch],
            baseline_size=baseline_size,
            quantile=quantile,
        )


if __name__ == "__main__":
    main()
