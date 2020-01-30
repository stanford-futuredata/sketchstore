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
        num_queries: int = 100,
        query_lens=None,
):
    if query_lens is None:
        query_lens = [
            2**i for i in range(int(math.log2(granularity)))
        ]
    workload = []
    r = np.random.RandomState(seed)
    for cur_query_len in query_lens:
        start_idxs = r.randint(0, granularity - cur_query_len, size=num_queries)
        workload.extend([(start_idx, start_idx + cur_query_len) for start_idx in start_idxs])
    return workload


def run_workload(
        workload: List[Tuple],
        x_to_track: np.ndarray,
        true_board: pd.DataFrame,
        est_board: pd.DataFrame,
        totals_df: pd.DataFrame,
        sketch_name: str,
        quantile: bool,
        accumulator_size: int = 0,
):
    results = []
    dyadic_base = -1
    if "dyadic" in sketch_name:
        dyadic_base = linear_board.get_dyadic_base(sketch_name)
    for start_idx, end_idx in tqdm(workload):
        true_counts = board_query.query_linear(
            true_board, seg_start=start_idx, seg_end=end_idx, x_to_track=x_to_track,
            quantile=quantile, dyadic_base=-1)
        if accumulator_size == 0:
            est_counts = board_query.query_linear(
                est_board, seg_start=start_idx, seg_end=end_idx, x_to_track=x_to_track,
                quantile=quantile, dyadic_base=dyadic_base)
        else:
            if quantile:
                est_counts = board_query.query_linear_acc_quant(
                    est_board, seg_start=start_idx, seg_end=end_idx, x_to_track=x_to_track,
                    acc_size=accumulator_size,
                )
            else:
                est_counts = board_query.query_linear_mg(
                    est_board, seg_start=start_idx, seg_end=end_idx, x_to_track=x_to_track,
                    acc_size=accumulator_size,
                )
        true_tot = board_query.query_linear_tot(totals_df, start_idx, end_idx)
        cur_results = board_query.calc_errors(true_counts, est_counts)
        # print(true_counts)
        # print(est_counts)
        cur_results["start_idx"] = start_idx
        cur_results["end_idx"] = end_idx
        cur_results["query_len"] = end_idx - start_idx
        cur_results["sketch"] = sketch_name
        cur_results["total"] = true_tot
        cur_results["acc_size"] = accumulator_size
        results.append(cur_results)
    return results


def calc_results(
        workload: List,
        data_name: str,
        granularity: int,
        sketch_name: str,
        sketch_size: int,
        baseline_size: int,
        quantile: bool,
        accumulator_size: int = 0,
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
    print("Estimating: {}".format(sketch_name))

    cur_board = pd.read_pickle(sketch_file)
    cur_results = run_workload(workload, x_to_track=x_to_track,
                               true_board=true_board, est_board=cur_board, totals_df=totals_df,
                               sketch_name=sketch_name,
                               quantile=quantile, accumulator_size=accumulator_size)

    results_df = pd.DataFrame(cur_results)
    results_df["dataset"] = data_name
    results_df["granularity"] = granularity
    results_df["quantile"] = quantile
    out_dir, _ = os.path.split(true_file)
    if accumulator_size == 0:
        out_file = os.path.join(out_dir, "{}_{}_errors.csv".format(sketch_name, sketch_size))
    else:
        out_file = os.path.join(out_dir, "{}_{}_acc{}_errors.csv".format(
            sketch_name, sketch_size, accumulator_size
            ))
    results_df.to_csv(out_file, index=False)
    return results_df


from linear_board import space_experiment


def run_query_length_experiments(experiment_id):
    cur_experiment = space_experiment[experiment_id]
    data_name = cur_experiment["data_name"]
    granularity = cur_experiment["granularity"]
    baseline_sizes = cur_experiment["baseline_sizes"]
    cur_sketches = cur_experiment["sketches"]
    quantile = cur_experiment["quantile"]
    query_lens = cur_experiment.get("query_lens", None)
    num_queries = cur_experiment.get("num_queries", 100)
    workload = gen_workload(
        granularity,
        seed=0,
        num_queries=num_queries,
        query_lens=query_lens,
    )
    for cur_size in baseline_sizes:
        print("Cur Size: {}".format(cur_size))
        for cur_sketch in cur_sketches:
            results_df = calc_results(
                workload=workload,
                data_name=data_name,
                granularity=granularity,
                sketch_name=cur_sketch,
                sketch_size=cur_size,
                baseline_size=cur_size,
                quantile=quantile,
            )


def run_acc_experiments(experiment_id=0):
    cur_experiment = space_experiment[experiment_id]
    data_name = cur_experiment["data_name"]
    granularity = cur_experiment["granularity"]
    baseline_sizes = cur_experiment["baseline_sizes"]
    accumulator_sizes = cur_experiment.get("accumulator_sizes",[0])
    cur_sketches = cur_experiment["sketches"]
    quantile = cur_experiment["quantile"]
    query_lens = cur_experiment.get("query_lens", None)
    num_queries = cur_experiment.get("num_queries", 100)
    workload = gen_workload(
        granularity,
        seed=0,
        num_queries=num_queries,
        query_lens=query_lens,
    )
    for cur_base_size in baseline_sizes:
        print("Baseline Size: {}".format(cur_base_size))
        for acc_size in accumulator_sizes:
            print("Accumulator Size: {}".format(acc_size))
            for cur_sketch in cur_sketches:
                results_df = calc_results(
                    workload=workload,
                    data_name=data_name,
                    granularity=granularity,
                    sketch_name=cur_sketch,
                    sketch_size=cur_base_size,
                    baseline_size=cur_base_size,
                    quantile=quantile,
                    accumulator_size=acc_size,
                )


def main():
    # run_query_length_experiments(experiment_id=7)
    run_acc_experiments(experiment_id=9)


if __name__ == "__main__":
    main()
