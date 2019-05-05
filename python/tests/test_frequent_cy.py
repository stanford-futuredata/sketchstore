import unittest

import sketch.frequent_cy
import numpy as np

class SpaceSavingTest(unittest.TestCase):
    def test_find_t(self):
        xs = np.array([10, 5, 3, 2])
        print(sketch.frequent_cy.find_t(xs, 3))
