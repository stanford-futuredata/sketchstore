from typing import List, Tuple, Sequence, Mapping, Dict, Any
import itertools
import numpy as np
import pandas as pd


class WorkloadProperties:
    def __init__(
            self,
            dim_names: Sequence[str],
            pred_weights: Sequence[float],
            pred_cardinalities: Sequence[int],
            max_time_segments: int
    ):
        self.dim_names = dim_names
        self.pred_weights = pred_weights
        self.pred_cardinalities = pred_cardinalities
        self.max_time_segments = max_time_segments

    def calc_prob(self, dim_set_idxs: Sequence[int]):
        num_dims = len(self.pred_cardinalities)
        cur_prob = 1
        for d_idx in range(num_dims):
            if d_idx in dim_set_idxs:
                cur_prob *= self.pred_weights[d_idx] / self.pred_cardinalities[d_idx]
            else:
                cur_prob *= (1 - self.pred_weights[d_idx])
        return cur_prob


def scale_a_weights(as_g: np.ndarray, total_space: int, min_amt:int=0):
    baseline_sizes = np.zeros(as_g.shape, dtype=np.int_)
    if min_amt > 0:
        baseline_sizes = np.repeat(min_amt, as_g.shape)
        total_space -= np.sum(baseline_sizes)
    a_scaled = as_g * total_space / np.sum(as_g)
    total_sizes = (baseline_sizes + np.diff(np.round(np.insert(np.cumsum(a_scaled), 0, 0)))).astype(int)
    # round while preserving sum
    return total_sizes
    # return np.round(a_scaled)


def get_a_weights_uniform(dim_names, df_total: pd.DataFrame):
    df_a_weights = df_total.copy()
    df_a_weights["_a"] = 1
    return df_a_weights.set_index(dim_names)["_a"]


def get_a_weights_prop(dim_names, df_total: pd.DataFrame):
    return df_total.set_index(dim_names)["total"]


def get_a_weights_poiss(wp: WorkloadProperties, df_total: pd.DataFrame):
    df_a_weights = df_total.copy()
    df_a_weights["_sum"] = 0
    num_dims = len(wp.pred_cardinalities)
    for num_predicates in range(num_dims + 1):
        dim_sets = list(itertools.combinations(range(num_dims), num_predicates))
        for dim_set_idxs in dim_sets:
            cur_prob = wp.calc_prob(dim_set_idxs)
            if len(dim_set_idxs) == 0:
                tot_sum = df_total["total"].sum()
                df_a_weights["_sum"] += cur_prob / (tot_sum ** 2)
            else:
                cur_dim_names = [wp.dim_names[i] for i in dim_set_idxs]
                dfg = df_total.groupby(cur_dim_names)[["total"]].sum().reset_index(
                ).rename(columns={"total": "_tsum"})
                df_a_weights = df_a_weights.merge(dfg, on=cur_dim_names)
                df_a_weights["_sum"] += cur_prob / (df_a_weights["_tsum"] ** 2)
                del df_a_weights["_tsum"]
    df_a_weights["_a"] = (df_a_weights["_sum"] * df_a_weights["total"] ** 2)
    return df_a_weights.set_index(wp.dim_names)["_a"]
