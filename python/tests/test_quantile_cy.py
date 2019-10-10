import unittest

import sketch.quantile_cy
import numpy as np

class SpaceSavingTest(unittest.TestCase):
    def test_find_t(self):
        d_true = {1.0: 1, 2.0: 2, 3.0: 1}
        d_est = {2.0: 4}
        xvals, xdeltas = sketch.quantile_cy.get_deltas_2(d_true, d_est)
        print(xvals)
        print(xdeltas)
