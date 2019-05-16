import unittest

import storyboard.planner_cy
import numpy as np


class SpaceSavingTest(unittest.TestCase):
    def test_rawgroup(self):
        dims = [1,2]
        vals = np.array([1,2])
        rg = storyboard.planner_cy.RawGroupL(dims, vals)
        print(rg)
        print(rg.dims)
        for x in rg.vals:
            print(x)

