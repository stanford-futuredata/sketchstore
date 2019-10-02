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


def scale_a_weights(as_g: np.ndarray, total_space: int):
    a_scaled = as_g * total_space / np.sum(as_g)
    # round while preserving sum
    return np.diff(np.round(np.insert(np.cumsum(a_scaled), 0, 0))).astype(int)
    # return np.round(a_scaled)


def get_a_weights_poiss(wp: WorkloadProperties, df_total: pd.DataFrame):
    df_a_weights = df_total.copy()
    df_a_weights["sum"] = 0
    num_dims = len(wp.pred_cardinalities)
    for num_predicates in range(num_dims + 1):
        dim_sets = list(itertools.combinations(range(num_dims), num_predicates))
        for dim_set_idxs in dim_sets:
            cur_prob = wp.calc_prob(dim_set_idxs)
            if len(dim_set_idxs) == 0:
                tot_sum = df_total["total"].sum()
                df_a_weights["sum"] += cur_prob / (tot_sum ** 2)
            else:
                cur_dim_names = [wp.dim_names[i] for i in dim_set_idxs]
                dfg = df_total.groupby(cur_dim_names)[["total"]].sum().reset_index(
                ).rename(columns={"total": "tsum"})
                df_a_weights = df_a_weights.merge(dfg, on=cur_dim_names)
                df_a_weights["sum"] += cur_prob / (df_a_weights["tsum"] ** 2)
                del df_a_weights["tsum"]
    df_a_weights["a"] = (df_a_weights["sum"] * df_a_weights["total"] ** 2) ** (1 / 3)
    return df_a_weights.set_index(wp.dim_names)["a"]
