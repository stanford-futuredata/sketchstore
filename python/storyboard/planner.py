from typing import List, Tuple, Sequence, Mapping
import itertools
import numpy as np
import pandas as pd

from sketch.compress_freq import PPSCompressor


class RawGroup:
    def __init__(self, dims: List, vals: Sequence):
        self.dims = dims
        self.vals = vals

    def __str__(self):
        return "{}->{}".format(str(self.dims), str(self.vals))

    def __repr__(self):
        return self.__str__()


class FreqSummaryGroup:
    def __init__(self, dims: List, counters: Mapping[int, float]):
        self.dims = dims
        self.counters = counters

    def __str__(self):
        return "{}->{}".format(str(self.dims), str(self.counters))

    def __repr__(self):
        return self.__str__()


class WorkloadProperties:
    def __init__(
            self,
            pred_weights: Sequence[float],
            max_time_segments: int
    ):
        self.pred_weights = pred_weights
        self.max_time_segments = max_time_segments


def list_to_counters(x_list: Sequence) -> Mapping[object, float]:
    return pd.Series(x_list).value_counts()


def group_vals(dim_vals: List[np.ndarray], val_col: np.ndarray) -> List[RawGroup]:
    n_dims = len(dim_vals)
    n_rows = len(val_col)
    if n_dims == 0:
        return [RawGroup((), val_col)]

    group_vals = dict()
    for row_idx in range(n_rows):
        row_key = tuple((dim_col[row_idx] for dim_col in dim_vals))
        if row_key not in group_vals:
            group_vals[row_key] = list()
        group_vals[row_key].append(val_col[row_idx])

    group_list = list()
    for cur_key, cur_vals in group_vals.items():
        group_list.append(RawGroup(list(cur_key), cur_vals))
    return group_list


def scale_a_weights(as_g, total_space: int):
    a_scaled = as_g * total_space / np.sum(as_g)
    # round while preserving sum
    return np.diff(np.round(np.insert(np.cumsum(a_scaled), 0, 0))).astype(int)
    # return np.round(a_scaled)


def get_a_weights(wp: WorkloadProperties, groups: List[RawGroup]):
    pred_weights = wp.pred_weights
    num_groups = len(groups)
    num_dims = len(groups[0].dims)

    as_g = np.zeros(num_groups)
    ns_g = np.array([len(g.vals) for g in groups])
    for num_predicates in range(len(pred_weights)):
        dim_sets = list(itertools.combinations(range(num_dims), num_predicates))
        for dim_set in dim_sets:
            query_totals = dict()
            for cur_group in groups:
                idx_tuple = tuple(cur_group.dims[i] for i in dim_set)
                query_totals[idx_tuple] = query_totals.get(idx_tuple, 0)+len(cur_group.vals)
            cur_dim_set_weight = pred_weights[num_predicates] / (len(dim_sets) * len(query_totals))
            for group_idx, cur_group in enumerate(groups):
                idx_tuple = tuple(cur_group.dims[i] for i in dim_set)
                as_g[group_idx] += 1.0/(query_totals[idx_tuple]**2) * cur_dim_set_weight

    as_g *= ns_g**2
    return as_g**(1.0/3)


class FreqProcessor:
    def __init__(
            self,
            total_size: int,
            workload_prop: WorkloadProperties,
            dim_col_idxs: Sequence[int],
            val_col_idx: int,
    ):
        self.total_size = total_size
        self.workload_prop = workload_prop
        self.dim_col_idxs = dim_col_idxs
        self.val_col_idx = val_col_idx

    def create_storyboard(
            self, df_input: pd.DataFrame,
    ) -> List[FreqSummaryGroup]:
        dim_cols = [df_input.iloc[:, d_idx] for d_idx in self.dim_col_idxs]
        val_col = df_input.iloc[:, self.val_col_idx]
        raw_groups = group_vals(dim_cols, val_col)

        a_weights = get_a_weights(self.workload_prop, raw_groups)
        group_sizes = scale_a_weights(a_weights, self.total_size)
        group_biases = [0] * len(raw_groups)
        pps = PPSCompressor(0, unbiased=True)

        group_summaries = []
        for group_idx in range(len(raw_groups)):
            cur_group = raw_groups[group_idx]
            cur_size = group_sizes[group_idx]
            cur_bias = group_biases[group_idx]

            cur_group_counters = list_to_counters(cur_group.vals)
            new_counters = pps.compress(cur_group_counters, cur_size)
            cur_summ = FreqSummaryGroup(
                cur_group.dims,
                new_counters
            )
            group_summaries.append(cur_summ)
        return group_summaries

    def write_storyboard(self, sb: List[FreqSummaryGroup], fname: str):
        pass

