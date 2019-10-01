import math
import os
from typing import List, Tuple

import numpy as np
import pandas as pd
from tqdm import tqdm

import sketch.sketch_gen as board_sketch
import storyboard.board_gen as board_gen
import storyboard.board_query as board_query
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
    dyadic_base = None
    if "dyadic" in sketch_name:
        dyadic_base = linear_board.get_dyadic_base(sketch_name)
    for start_idx, end_idx in tqdm(workload):
        true_counts = board_query.query_linear(
            true_board, seg_start=start_idx, seg_end=end_idx, x_to_track=x_to_track,
            quantile=quantile)
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
        sketch_names: List[str],
        sketch_sizes: List[int],
        quantile: bool,
) -> pd.DataFrame:
    true_sketch = "top_values"
    x_to_track = linear_board.get_tracked(data_name)

    board_files = {
        cur_sketch: linear_board.get_file_name(
            data_name=data_name,
            granularity=granularity,
            sketch_name=cur_sketch,
            sketch_size=cur_size,
        )
        for cur_sketch, cur_size in zip(sketch_names, sketch_sizes)
    }
    true_board = pd.read_pickle(board_files[true_sketch])
    totals_df = pd.read_csv(linear_board.get_totals_name(data_name, granularity=granularity))
    workload = gen_workload(granularity, seed=0, num_queries=20)

    result_rows = []
    for cur_sketch_name in sketch_names:
        if cur_sketch_name == true_sketch:
            continue
        print("Estimating: {}".format(cur_sketch_name))
        cur_board = pd.read_pickle(board_files[cur_sketch_name])
        cur_results = run_workload(workload, x_to_track=x_to_track,
                                   true_board=true_board, est_board=cur_board, totals_df=totals_df,
                                   sketch_name=cur_sketch_name,
                                   quantile=quantile)
        result_rows.extend(cur_results)

    results_df = pd.DataFrame(result_rows)
    results_df["dataset"] = data_name
    results_df["granularity"] = granularity
    results_df["quantile"] = quantile
    out_dir, _ = os.path.split(board_files[true_sketch])
    out_file = os.path.join(out_dir, "errors.csv")
    results_df.to_csv(out_file, index=False)
    return results_df


def main():
    data_name = "caida_1M"
    granularity = 2048
    sketch_names = ["top_values", "cooperative", "dyadic_b2"]
    sketch_sizes = [64, 64, 5]
    # sketch_names = ["top_values", "dyadic_b2"]
    results_df = calc_results(
        data_name=data_name,
        granularity=granularity,
        sketch_names=sketch_names,
        sketch_sizes=sketch_sizes,
        quantile=False
    )


if __name__ == "__main__":
    main()
