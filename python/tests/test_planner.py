import unittest

from storyboard.planner import FreqPlanner, RawGroup
import numpy as np


class FreqPlannerTest(unittest.TestCase):
    def test_simple(self):
        fp = FreqPlanner(
            pred_weights=[1, 1, 1]
        )
        groups = [
            RawGroup([0, 0], np.arange(100)),
            RawGroup([0, 1], np.arange(10)),
            RawGroup([0, 2], np.arange(1)),
            RawGroup([1, 0], np.arange(200)),
            RawGroup([1, 1], np.arange(20)),
            RawGroup([1, 2], np.arange(2)),
        ]
        a_weights = fp.get_a_weights(groups)
        sizes = fp.get_sizes(groups, 100)
