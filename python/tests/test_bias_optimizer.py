import unittest
import pandas as pd
import numpy as np

import storyboard.bias_optimizer as bopt


class BiasOptimizerTest(unittest.TestCase):
    def test_bias(self):
        xs = [
            np.array(pd.Series(np.random.zipf(a=1.1, size=30000)).value_counts())
            for i in range(3)
        ]
        bs = bopt.opt_sequence(xs, sizes=[2, 2, 2], n_iter=50)
        self.assertAlmostEqual(512, bs[0], 5)
        self.assertAlmostEqual(512, bs[2], 5)
