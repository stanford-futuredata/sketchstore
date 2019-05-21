from typing import List, Tuple, Sequence, Mapping
import itertools
import numpy as np
from pandas import DataFrame

from storyboard.planner import WorkloadProperties, FreqGroup


class StoryboardQueryExecutor:
    def __init__(self, groups: List[FreqGroup]):
        self.groups = groups

    def exec_query(self, filter: Sequence, topn:int=3):
        pass


class RawQueryExecutor:
    def __init__(self, df: DataFrame, dim_names: Sequence[str], val_name: str):
        self.df = df
        self.dim_names = dim_names
        self.val_name = val_name

    def exec_query(self, filter: Sequence):
        n_dims = len(filter)
        mask = np.repeat(True, len(self.df))
        for d_idx in range(n_dims):
            if filter[d_idx] is not None:
                cur_dim_name = self.dim_names[d_idx]
                mask &= (self.df[cur_dim_name] == filter[d_idx])
        val_counts = self.df[mask][self.val_name].value_counts()
        return val_counts


class StoryboardVarianceEstimator:
    def __init__(self, wp: WorkloadProperties, seed: int):
        self.wp = wp
        self.seed = seed
        np.random.seed(self.seed)

    def sample_query(self):
        p_weights = self.wp.pred_weights
        p_cards = self.wp.pred_cardinalities
        n_dims = len(p_cards)
        dim_values = []
        for dim_idx in range(n_dims):
            if np.random.uniform() < p_weights[dim_idx]:
                # filter on dimension
                cur_dim_value = np.random.choice(p_cards[dim_idx])
            else:
                # not filtering on dimension
                cur_dim_value = None
            dim_values.append(cur_dim_value)

        max_time = np.random.randint(0, self.wp.max_time_segments)
        time_range = (0, max_time)
        return dim_values, time_range

    def est_error(self, groups: List[FreqGroup], n_trials: int = 3):
        trial_totals = []
        trial_mses = []
        for trail_idx in range(n_trials):
            cur_query_dim_values,_ = self.sample_query()
            n_dims = len(cur_query_dim_values)
            trial_total = 0
            trial_mse = 0
            # print("trial {}: {}".format(trail_idx, cur_query_dim_values))
            for cur_segment in groups:
                matches = all((
                    cur_query_dim_values[i] is None or cur_query_dim_values[i] == cur_segment.dims[i]
                    for i in range(n_dims)
                ))

                if matches:
                    trial_total += cur_segment.size
                    cur_err = min(cur_segment.vals.values())
                    # cur_err = cur_segment.size / len(cur_segment.vals.keys())
                    trial_mse += cur_err**2
            trial_mses.append(trial_mse)
            trial_totals.append(trial_total)
        trial_mses = np.array(trial_mses)
        trial_totals = np.array(trial_totals)
        # print(np.sqrt(trial_mses))
        # print(trial_totals)
        return np.sqrt(np.mean(trial_mses/trial_totals**2))

    def eval_error(self, groups: List[FreqGroup]):
        num_dims = len(groups[0].dims)
        pred_cardinalities = self.wp.pred_cardinalities
        pred_weights = self.wp.pred_weights

        cs_dims = []
        cs_errors = []
        cs_weights = []
        for num_predicates in range(num_dims + 1):
            dim_sets = list(itertools.combinations(range(num_dims), num_predicates))
            for dim_set in dim_sets:
                group_totals = dict()
                group_variances = dict()
                for cur_segment in groups:
                    idx_tuple = tuple(cur_segment.dims[i] for i in dim_set)
                    group_totals[idx_tuple] = group_totals.get(idx_tuple, 0) + cur_segment.size
                    cur_group_err = cur_segment.size / len(cur_segment.vals)
                    group_variances[idx_tuple] = (
                            group_variances.get(idx_tuple, 0)
                            + cur_group_err * cur_group_err
                    )

                dim_set_mse = 0
                group_errors = dict()
                for idx_tuple in group_totals.keys():
                    cur_group_total = group_totals[idx_tuple]
                    cur_group_mse = group_variances[idx_tuple] / (cur_group_total*cur_group_total)
                    dim_set_mse += cur_group_mse
                    group_errors[idx_tuple] = np.sqrt(cur_group_mse)
                # print('dimset')
                # print(dim_set)
                # print(group_errors)

                num_queries = np.prod([pred_cardinalities[d] for d in dim_set])
                dim_set_mse /= num_queries

                cur_dim_set_weight = 1
                for d_idx in range(num_dims):
                    if d_idx in dim_set:
                        cur_dim_set_weight *= pred_weights[d_idx]
                    else:
                        cur_dim_set_weight *= (1 - pred_weights[d_idx])


                cs_dims.append(dim_set)
                cs_errors.append(dim_set_mse)
                cs_weights.append(cur_dim_set_weight)

        cs_errors = np.array(cs_errors)
        cs_weights = np.array(cs_weights)
        # print("evaluator:")
        # print(np.sqrt(cs_errors))
        # print(cs_weights)
        return np.sqrt(np.sum(cs_errors * cs_weights))
