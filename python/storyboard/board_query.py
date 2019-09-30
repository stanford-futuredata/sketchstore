import math
from typing import List, Mapping, Iterable

import numpy as np
import pandas as pd
from tqdm import tqdm

import sketch.sketch_gen as board_sketch


def calc_errors(
        true_sums,
        est_sums,
        total
):
    errors = np.abs(np.array(true_sums) - np.array(est_sums))
    e_avg = np.mean(errors)
    e_rmse = np.mean(errors**2)
    e_max = np.max(errors)
    return e_avg/total, e_rmse/total, e_max/total


def query_linear(
        df: pd.DataFrame,
        seg_start: int,
        seg_end: int,
        x_to_track: List,
        quantile: bool = False,
):
    mask = (df["seg_idx"] >= seg_start) & (df["seg_idx"] < seg_end)
    tot_sum = df["total"][mask].sum()
    summaries: Iterable[board_sketch.BoardSketch] = df["data"][mask]
    tot_results = [0] * len(x_to_track)
    for cur_summary in summaries:
        tot_results = [
            tot_results[i] + cur_summary.estimate(x_to_track[i], rank=quantile)
            for i in range(len(x_to_track))
        ]
    return tot_results, tot_sum


def query_linear_dyadic(
        df: pd.DataFrame,
        seg_start: int,
        seg_end: int,
        x_to_track: List,
        quantile: bool = False,
):
    pass
