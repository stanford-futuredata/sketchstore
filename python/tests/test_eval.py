import unittest

from storyboard.planner import FreqGroup
import storyboard.eval
import storyboard.planner
import numpy as np
import math
import testdata.bench_gen
import itertools
import pandas as pd


class RawEvalTest(unittest.TestCase):
    def test_all(self):
        n_rows = 5000
        df, dim_names = testdata.bench_gen.gen_data(
            n_rows,
            [(3, 2),
             (2, 1)],
            f_skew=1.2,
            f_card=100
        )
        n_dims = len(dim_names)
        rq = storyboard.eval.RawQueryExecutor(df, dim_names, "f")
        res = rq.exec_query(filter=[None, None])
        self.assertEqual(n_rows, sum(res.values()))


class StoryboardQueryEvalTest(unittest.TestCase):
    def test_all(self):
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

        sq = storyboard.eval.StoryboardQueryExecutor(groups)
        res = sq.exec_query(filter=[None, None])
        self.assertEqual(sum(g_sizes), sum(res.values()))
