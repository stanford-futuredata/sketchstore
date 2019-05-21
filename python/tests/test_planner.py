import unittest

from storyboard.planner import FreqGroup
import storyboard.eval
import storyboard.planner
import numpy as np
import math
import testdata.bench_gen
import itertools
import pandas as pd

class FreqPlannerTest(unittest.TestCase):
    def test_simple(self):
        dims = list(itertools.product([0,1], [0,1,2]))
        g_sizes = [100, 10, 1, 200, 20, 2]
        groups = []
        for g_idx in range(len(dims)):
            cur_dim = list(dims[g_idx])
            g_size = g_sizes[g_idx]
            cur_values = pd.Series(np.random.zipf(1.5, g_size)).value_counts()
            groups.append(
                FreqGroup(cur_dim, g_size, cur_values)
            )

        wp = storyboard.planner.WorkloadProperties(
            pred_weights=[.2, .3],
            pred_cardinalities=[2, 3],
            max_time_segments=1,
        )
        a_weights = storyboard.planner.get_a_weights_poiss(wp, groups)
        print(a_weights)
        self.assertGreater(a_weights[0], .5)


    def test_storyboard(self):
        # df, dim_names = testdata.bench_gen.gen_data(
        #     50000,
        #     [(10, 1),
        #      (5, 1),
        #      (3, 1)],
        #     f_skew=1.2,
        #     f_card=100
        # )
        df, dim_names = testdata.bench_gen.gen_data(
            50000,
            [(3, 2),
             (2, 1)],
            f_skew=1.2,
            f_card=1000
        )
        n_dims = len(dim_names)

        dim_cards = df.nunique()[dim_names].values
        wps = [
            storyboard.planner.WorkloadProperties(
                pred_weights=[.1] * n_dims,
                pred_cardinalities=dim_cards,
                max_time_segments=1,
            ),
            storyboard.planner.WorkloadProperties(
                pred_weights=[0] * n_dims,
                pred_cardinalities=dim_cards,
                max_time_segments=1,
            ),
            storyboard.planner.WorkloadProperties(
                pred_weights=[1] * n_dims,
                pred_cardinalities=dim_cards,
                max_time_segments=1,
            )
        ]
        fps = [
            storyboard.planner.FreqProcessor(
                total_size=300,
                workload_prop=wp,
            ) for wp in wps
        ]
        sbs = [
            fp.create_storyboard(
                df_input=df,
                dim_col_names=dim_names,
                val_col_name="f"
            )
            for fp in fps
        ]
        for groups in sbs:
            print(",".join([str(g) for g in groups]))
            eval = storyboard.eval.StoryboardVarianceEstimator(wps[0], 0)
            res = eval.eval_error(groups)
            print("evaluated: {}".format(str(res)))
            res2 = eval.est_error(groups, n_trials=1000)
            print("estimated: {}".format(res2))

