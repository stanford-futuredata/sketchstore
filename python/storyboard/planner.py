from typing import List, Tuple, Sequence, Mapping, Dict, Any
import itertools
import numpy as np
import pandas as pd

import sketch.compress_freq
import storyboard.bias_optimizer as bopt


class FreqGroup:
    def __init__(self, dims: List, size: float, vals: Dict[Any, float]):
        self.dims = dims
        self.size = size
        self.vals = vals

    def __str__(self):
        return "{}->{}/{}".format(str(self.dims), self.size, len(self.vals))

    def __repr__(self):
        return "{}->{}/{}:{}".format(str(self.dims), self.size, len(self.vals), str(self.vals))


class WorkloadProperties:
    def __init__(
            self,
            pred_weights: Sequence[float],
            pred_cardinalities: Sequence[int],
            max_time_segments: int
    ):
        self.pred_weights = pred_weights
        self.pred_cardinalities = pred_cardinalities
        self.max_time_segments = max_time_segments


def scale_a_weights(as_g, total_space: int):
    a_scaled = as_g * total_space / np.sum(as_g)
    # round while preserving sum
    return np.diff(np.round(np.insert(np.cumsum(a_scaled), 0, 0))).astype(int)
    # return np.round(a_scaled)


def get_a_weights_poiss(wp: WorkloadProperties, groups: List[FreqGroup]):
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
                    cur_dim_set_weight *= wp.pred_weights[d_idx] / wp.pred_cardinalities[d_idx]
                else:
                    cur_dim_set_weight *= (1-wp.pred_weights[d_idx])
            # print("{}:{}".format(dim_set, cur_dim_set_weight))
            for group_idx, cur_group in enumerate(groups):
                idx_tuple = tuple(cur_group.dims[i] for i in dim_set)
                # print(cur_group)
                # print(idx_tuple)
                # print(query_totals[idx_tuple])
                # print(cur_dim_set_weight)
                as_g[group_idx] += cur_dim_set_weight/(query_totals[idx_tuple]**2)

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
        group_item_counts = [
            FreqGroup(
                list(cur_key),
                len(cur_group),
                cur_group[val_col_name].value_counts())
            for cur_key, cur_group in df_input.groupby(dim_col_names)
        ]

        a_weights = get_a_weights_poiss(self.workload_prop, group_item_counts)
        group_sizes = scale_a_weights(a_weights, self.total_size)
        biases = bopt.opt_sequence(
            x_counts=[g.vals for g in group_item_counts],
            sizes=group_sizes,
            n_iter=50,
            # n_iter=0
        )

        group_summaries = []
        for group_idx in range(len(group_item_counts)):
            cur_group = group_item_counts[group_idx]
            cur_size = group_sizes[group_idx]
            cur_bias = biases[group_idx]

            pps = sketch.compress_freq.HairCombCompressor(
                size=cur_size,
                seed=group_idx,
                unbiased=True,
                bias=cur_bias
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

