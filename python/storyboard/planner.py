from typing import List, Tuple, Sequence
import itertools
import numpy as np


class RawGroup:
    def __init__(self, dims: List[int], vals: Sequence):
        self.dims = dims
        self.vals = vals


class FreqPlanner:
    def __init__(
            self,
            pred_weights=None,
            max_time_segments=None
    ):
        self.pred_weights = pred_weights
        self.max_time_segments = max_time_segments

    def get_sizes(self, groups: List[RawGroup], total_space: int):
        as_g = self.get_a_weights(groups)
        a_scaled = as_g * total_space / np.sum(as_g)
        # round while preserving sum
        return np.diff(np.round(np.insert(np.cumsum(a_scaled), 0, 0)))
        # return np.round(a_scaled)

    def get_a_weights(self, groups: List[RawGroup]):
        num_groups = len(groups)
        num_dims = len(groups[0].dims)

        as_g = np.zeros(num_groups)
        ns_g = np.array([len(g.vals) for g in groups])
        for num_predicates in range(len(self.pred_weights)):
            dim_sets = list(itertools.combinations(range(num_dims), num_predicates))
            for dim_set in dim_sets:
                query_totals = dict()
                for cur_group in groups:
                    idx_tuple = tuple(cur_group.dims[i] for i in dim_set)
                    query_totals[idx_tuple] = query_totals.get(idx_tuple, 0)+len(cur_group.vals)
                cur_dim_set_weight = self.pred_weights[num_predicates] / (len(dim_sets) * len(query_totals))
                for group_idx, cur_group in enumerate(groups):
                    idx_tuple = tuple(cur_group.dims[i] for i in dim_set)
                    as_g[group_idx] += 1.0/(query_totals[idx_tuple]**2) * cur_dim_set_weight

        as_g *= ns_g**2
        return as_g**(1.0/3)

    def get_spec(self):
        pass
