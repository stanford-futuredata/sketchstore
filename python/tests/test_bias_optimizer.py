import unittest
import pandas as pd
import numpy as np

# import storyboard.bias_optimizer as bopt
import storyboard.bias_solver as bopt


class BiasOptimizerTest(unittest.TestCase):
    def test_bias(self):
        xs = [
            np.array(pd.Series(np.random.zipf(a=1.1, size=30000)).value_counts())
            for i in range(3)
        ]
        sizes = np.array([2,2,2])
        bs = bopt.opt_sequence(xs, sizes=sizes, n_iter=50)
        self.assertLess(bs[0], 450)
        self.assertGreater(bs[0], 400)
