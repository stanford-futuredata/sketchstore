from unittest import TestCase

import numpy as np
import sketch.greenwaldkhanna

class TestGKArray(TestCase):
    def test_tiny(self):
        np.random.seed(0)
        xs1 = np.linspace(0,1,1000)
        xs2 = np.linspace(1,2,1000)
        gk = sketch.greenwaldkhanna.GKArray(.02)
        gk.add_weighted(xs1, [1]*len(xs1))
        gk.add_weighted(xs2, [2]*len(xs2))
        print(len(gk.entries))
        print(gk.rank(1))
        est = gk.quantile(.5)
        self.assertAlmostEqual(1.25, est, delta=0.1)

