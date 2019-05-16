from typing import List, Tuple, Sequence, Mapping
import itertools
import numpy as np
import pandas as pd

import sketch.compress_freq


class FreqGroup:
    def __init__(self, dims: List, size, vals):
        self.dims = dims
        self.size = size
        self.vals = vals

    def __str__(self):
        return "{}->{}/{}".format(str(self.dims), self.size, str(self.vals))

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

#
# def list_to_counters(x_list: Sequence) -> Mapping[object, float]:
#     return pd.Series(x_list).value_counts()
#
#
# def group_vals(dim_vals: List[np.ndarray], val_col: np.ndarray) -> List[RawGroup]:
#     n_dims = len(dim_vals)
#     n_rows = len(val_col)
#     if n_dims == 0:
#         return [FreqGroup([], len(val_col), val_col)]
#
#     group_vals = dict()
#     for row_idx in range(n_rows):
#         row_key = tuple((dim_col[row_idx] for dim_col in dim_vals))
#         if row_key not in group_vals:
#             group_vals[row_key] = list()
#         group_vals[row_key].append(val_col[row_idx])
#
#     group_list = list()
#     for cur_key, cur_vals in group_vals.items():
#         group_list.append(RawGroup(list(cur_key), cur_vals))
#     return group_list


def scale_a_weights(as_g, total_space: int):
    a_scaled = as_g * total_space / np.sum(as_g)
    # round while preserving sum
    return np.diff(np.round(np.insert(np.cumsum(a_scaled), 0, 0))).astype(int)
    # return np.round(a_scaled)


# def get_a_weights(wp: WorkloadProperties, groups: List[RawGroup]):
#     pred_weights = wp.pred_weights
#     num_groups = len(groups)
#     num_dims = len(groups[0].dims)
#
#     as_g = np.zeros(num_groups)
#     ns_g = np.array([len(g.vals) for g in groups])
#     for num_predicates in range(len(pred_weights)):
#         dim_sets = list(itertools.combinations(range(num_dims), num_predicates))
#         for dim_set in dim_sets:
#             query_totals = dict()
#             for cur_group in groups:
#                 idx_tuple = tuple(cur_group.dims[i] for i in dim_set)
#                 query_totals[idx_tuple] = query_totals.get(idx_tuple, 0)+len(cur_group.vals)
#             cur_dim_set_weight = pred_weights[num_predicates] / (len(dim_sets) * len(query_totals))
#             for group_idx, cur_group in enumerate(groups):
#                 idx_tuple = tuple(cur_group.dims[i] for i in dim_set)
#                 as_g[group_idx] += 1.0/(query_totals[idx_tuple]**2) * cur_dim_set_weight
#
#     as_g *= ns_g**2
#     return as_g**(1.0/3)


def get_a_weights_poiss(wp: WorkloadProperties, groups: List[FreqGroup]):
    pred_weights = wp.pred_weights
    num_groups = len(groups)
    num_dims = len(groups[0].dims)

    as_g = np.zeros(num_groups)
    ns_g = np.array([g.size for g in groups])
    for num_predicates in range(num_dims+1):
        dim_sets = list(itertools.combinations(range(num_dims), num_predicates))
        for dim_set in dim_sets:
            query_totals = dict()
            for cur_group in groups:
                idx_tuple = tuple(cur_group.dims[i] for i in dim_set)
                query_totals[idx_tuple] = query_totals.get(idx_tuple, 0)+cur_group.size
            cur_dim_set_weight = 1
            for d_idx in range(num_dims):
                if d_idx in dim_set:
                    cur_dim_set_weight *= pred_weights[d_idx]
                else:
                    cur_dim_set_weight *= (1-pred_weights[d_idx])
            # print("{}:{}".format(dim_set, cur_dim_set_weight))
            for group_idx, cur_group in enumerate(groups):
                idx_tuple = tuple(cur_group.dims[i] for i in dim_set)
                as_g[group_idx] += cur_dim_set_weight/(query_totals[idx_tuple]**2 * len(query_totals))

    as_g *= ns_g**2
    return as_g**(1.0/3)


class FreqProcessor:
    def __init__(
            self,
            total_size: int,
            workload_prop: WorkloadProperties,
    ):
        self.total_size = total_size
        self.workload_prop = workload_prop

    def create_storyboard(
            self,
            df_input: pd.DataFrame,
            dim_col_names: List[str],
            val_col_name: str
    ) -> List[FreqGroup]:
        group_counts = [
            FreqGroup(
                list(cur_key),
                len(cur_group),
                cur_group[val_col_name].value_counts())
            for cur_key, cur_group in df_input.groupby(dim_col_names)
        ]

        a_weights = get_a_weights_poiss(self.workload_prop, group_counts)
        group_sizes = scale_a_weights(a_weights, self.total_size)
        group_biases = [0] * len(group_counts)

        group_summaries = []
        for group_idx in range(len(group_counts)):
            cur_group = group_counts[group_idx]
            cur_size = group_sizes[group_idx]
            cur_bias = group_biases[group_idx]

            pps = sketch.compress_freq.HairCombCompressor(
                size=cur_size,
                seed=group_idx,
                unbiased=True
            )
            new_counters = pps.compress(cur_group.vals)
            cur_summ = FreqGroup(
                cur_group.dims,
                cur_group.size,
                new_counters
            )
            group_summaries.append(cur_summ)
        return group_summaries

    def write_storyboard(self, sb: List[FreqGroup], fname: str):
        pass

