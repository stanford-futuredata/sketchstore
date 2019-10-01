import math
from typing import List, Mapping, Iterable, Sequence, Tuple

import numpy as np
import pandas as pd
from tqdm import tqdm

import sketch.sketch_gen as board_sketch
import storyboard.dyadic_cy as dyadic_cy


def calc_errors(
        true_sums,
        est_sums,
):
    errors = np.abs(np.array(true_sums) - np.array(est_sums))
    e_avg = np.mean(errors)
    e_rmse = np.mean(errors**2)
    e_max = np.max(errors)
    return {
        "mean": e_avg,
        "rmse": e_rmse,
        "max": e_max,
    }


def query_linear_tot(
        df: pd.DataFrame,
        seg_start: int,
        seg_end: int,
):
    mask = (df["seg_idx"] >= seg_start) & (df["seg_idx"] < seg_end)
    tot_sum = df["total"][mask].sum()
    return tot_sum


def query_linear(
        df: pd.DataFrame,
        seg_start: int,
        seg_end: int,
        x_to_track: Sequence,
        quantile: bool = False,
        dyadic_base: int = None,
) -> List:
    if dyadic_base is not None:
        return query_linear_dyadic(df, seg_start, seg_end, x_to_track, quantile, dyadic_base)
    mask = (df["seg_idx"] >= seg_start) & (df["seg_idx"] < seg_end)
    summaries: Iterable[board_sketch.BoardSketch] = df["data"][mask]
    tot_results = [0] * len(x_to_track)
    for cur_summary in summaries:
        tot_results = [
            tot_results[i] + cur_summary.estimate(x_to_track[i], rank=quantile)
            for i in range(len(x_to_track))
        ]
    return tot_results


def query_linear_dyadic(
        df: pd.DataFrame,
        seg_start: int,
        seg_end: int,
        x_to_track: Sequence,
        quantile: bool = False,
        dyadic_base: int = None,
) -> List:
    tot_results = [0] * len(x_to_track)
    dfi = df.set_index(["seg_idx", "level"])
    dyadic_segments = dyadic_cy.dyadic_breakdown(
        seg_start, seg_end, base=dyadic_base
    )
    for dyadic_index in dyadic_segments:
        adjusted_index = (dyadic_index[0]-1, dyadic_index[1])
        cur_summary = dfi["data"].loc[adjusted_index]
        tot_results = [
            tot_results[i] + cur_summary.estimate(x_to_track[i], rank=quantile)
            for i in range(len(x_to_track))
        ]
    return tot_results
