import math
import os
from typing import List, Tuple, Sequence, Dict

import numpy as np
import pandas as pd
from tqdm import tqdm
import json

import sketch.sketch_gen as board_sketch
import storyboard.board_gen as board_gen
# import storyboard.board_query as board_query
import storyboard.query_cy as board_query
import storyboard.size_optimizer
from storyboard.size_optimizer import WorkloadProperties
import cube_board


def sample_query(
        p_weights,
        dim_names: Sequence[str],
        dim_unique_values: Sequence[Sequence],
        r: np.random.RandomState
) -> Dict:
    n_dims = len(p_weights)
    query_filter = dict()
    for dim_idx in range(n_dims):
        if r.uniform() < p_weights[dim_idx]:
            # filter on dimension
            cur_dim_value = r.choice(dim_unique_values[dim_idx])
            query_filter[dim_names[dim_idx]] = cur_dim_value
    return query_filter


def gen_workload(
        df_board: pd.DataFrame,
        wp: WorkloadProperties,
        seed: int = 0,
        num_queries: int = 200
):
    workload = []
    r = np.random.RandomState(seed)
    p_weights = wp.pred_weights
    p_cards = wp.pred_cardinalities
    dim_names = wp.dim_names
    dim_unique_values = [
        df_board[cur_dim].unique()
        for cur_dim in dim_names
    ]

    for cur_query_idx in range(num_queries):
        cur_query = sample_query(
            p_weights=p_weights,
            dim_names=dim_names,
            dim_unique_values=dim_unique_values,
            r=r
        )
        workload.append(cur_query)
    return workload


def run_workload(
        workload: List[Dict],
        x_to_track: np.ndarray,
        true_board: pd.DataFrame,
        est_board: pd.DataFrame,
        totals_df: pd.DataFrame,
        sketch_name: str,
        quantile: bool,
):
    results = []
    memoized = dict()
    for cur_query in tqdm(workload):
        qstr = str(cur_query)
        if qstr in memoized:
            cur_results = memoized[qstr]
        else:
            true_counts = board_query.query_cube(
                true_board, query_values=cur_query, x_to_track=x_to_track,
                quantile=quantile)
            est_counts = board_query.query_cube(
                est_board, query_values=cur_query, x_to_track=x_to_track,
                quantile=quantile)
            true_tot = board_query.query_cube_tot(totals_df, cur_query)
            cur_results = board_query.calc_errors(true_counts, est_counts)
            cur_results["dim_values"] = str(cur_query)
            cur_results["query_len"] = len(cur_query)
            cur_results["sketch"] = sketch_name
            cur_results["total"] = true_tot

            memoized[qstr] = cur_results
        results.append(cur_results)
    return results


def transform_sketch_to_error_file(sketch_file: str, workload_p: float):
    out_file = sketch_file[:sketch_file.rfind(".")]+"_errors@p{}.csv".format(int(workload_p*100))
    return out_file


def calc_results(
        data_name: str,
        split_strategy: str,
        board_size: int,
        sketch_name: str,
        bias_opt: bool,
        quantile: bool,
        workload_p: float, # workload p to test with, could be different from construction strategy
        num_queries: int = 200,
):
    if quantile:
        true_sketch = "q_top_values"
    else:
        true_sketch = "top_values"
    x_to_track = cube_board.get_tracked(data_name)
    dim_names,x_name = cube_board.get_dim_names(data_name=data_name)
    print("Loading Boards")
    true_file = cube_board.get_file_name(
        data_name=data_name,
        split_strategy="uniform",
        board_size=board_size,
        sketch_name=true_sketch,
        bias=False,
    )
    sketch_file = cube_board.get_file_name(
        data_name=data_name,
        split_strategy=split_strategy,
        board_size=board_size,
        sketch_name=sketch_name,
        bias=bias_opt,
    )
    true_board = pd.read_pickle(true_file)
    totals_df = pd.read_csv(
        cube_board.get_totals_name(data_name)
    )
    wp = cube_board.get_workload_properties(
        true_board, dim_names, workload_p
    )
    workload = gen_workload(
        true_board, wp=wp,
        seed=0, num_queries=num_queries)
    print("Estimating: {}".format(sketch_name))

    cur_board = pd.read_pickle(sketch_file)
    print("Running Workload")
    cur_results = run_workload(workload, x_to_track=x_to_track,
                               true_board=true_board, est_board=cur_board, totals_df=totals_df,
                               sketch_name=sketch_name,
                               quantile=quantile)

    results_df = pd.DataFrame(cur_results)
    results_df["_dataset"] = data_name
    results_df["_split_strategy"] = split_strategy
    results_df["_quantile"] = quantile
    results_df["_bias_opt"] = bias_opt
    results_df["_board_size"] = board_size
    out_file = transform_sketch_to_error_file(sketch_file, workload_p=workload_p)
    print(out_file)
    results_df.to_csv(out_file, index=False)
    return results_df


def main():
    experiment_id = 1
    cur_experiment = cube_board.experiment_runs[experiment_id]

    data_name = cur_experiment["data_name"]
    board_size = cur_experiment["board_size"]
    quantile = cur_experiment["quantile"]
    workload_p = cur_experiment["workload_p"]
    sketch_types = cur_experiment["sketch_types"]

    for cur_sketch, split_strategy, bias_opt in sketch_types:
        results_df = calc_results(
            data_name=data_name,
            split_strategy=split_strategy,
            board_size=board_size,
            sketch_name=cur_sketch,
            bias_opt=bias_opt,
            quantile=quantile,
            workload_p=workload_p,
            num_queries=10000,
        )


if __name__ == "__main__":
    main()
