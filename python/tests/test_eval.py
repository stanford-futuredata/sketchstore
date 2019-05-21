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
    def test_empty(self):
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
        self.assertEqual(n_rows, sum(res.values))
