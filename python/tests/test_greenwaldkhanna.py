from unittest import TestCase

import numpy as np
import sketch.greenwaldkhanna
from storyboard.query_cy import CDFSketch


class TestGKArray(TestCase):
    def test_tiny(self):
        np.random.seed(0)
        xs1 = np.linspace(0,1,1000)
        xs2 = np.linspace(1,2,1000)
        gk = sketch.greenwaldkhanna.GKArray(.02)
        gk.add_pairs([(x,1) for x in xs1])
        gk.add_pairs([(x,2) for x in xs2])
        print(len(gk.get_dict()))

        cdf = CDFSketch(gk.get_dict())
        print(cdf.estimate(1.25))
        # print(gk.rank(1))
        # est = gk.quantile(.5)
        # self.assertAlmostEqual(1.25, est, delta=0.1)

