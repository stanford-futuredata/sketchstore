import unittest

from storyboard.planner import FreqGroup, FreqProcessor
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
            pred_weights=[.3,.3],
            max_time_segments=1,
        )
        a_weights = storyboard.planner.get_a_weights_poiss(wp, groups)
        print(a_weights)
        self.assertGreater(a_weights[0], .5)


    def test_storyboard(self):
        df = testdata.bench_gen.gen_data(
            1000,
            [(2, 0),
             (5, 1)],
            f_skew=1.2,
            f_card=100
        )
        wp = storyboard.planner.WorkloadProperties(
            pred_weights=[.3, .3],
            max_time_segments=1,
        )
        fp = storyboard.planner.FreqProcessor(
            total_size=30,
            workload_prop=wp,
        )
        groups = fp.create_storyboard(
            df_input=df,
            dim_col_names=["d0", "d1"],
            val_col_name="f"
        )
        self.assertEquals(10, len(groups))
        print(groups)
