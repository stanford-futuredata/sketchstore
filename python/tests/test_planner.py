import unittest

from storyboard.planner import RawGroup, FreqProcessor
import storyboard.planner
import numpy as np
import math


class FreqPlannerTest(unittest.TestCase):
    def test_simple(self):
        groups = [
            RawGroup([0, 0], np.arange(100)),
            RawGroup([0, 1], np.arange(10)),
            RawGroup([0, 2], np.arange(1)),
            RawGroup([1, 0], np.arange(200)),
            RawGroup([1, 1], np.arange(20)),
            RawGroup([1, 2], np.arange(2)),
        ]
        wp = storyboard.planner.WorkloadProperties(
            pred_weights=[1,1,1],
            max_time_segments=1,
        )
        a_weights = storyboard.planner.get_a_weights(wp, groups)
        total_size = 100
        sizes = storyboard.planner.scale_a_weights(a_weights, total_size)
        self.assertLessEqual(math.fabs(np.sum(sizes) - total_size), 1)

    def test_grouping(self):
        dim_cols = [
            np.array([1, 1, 0, 0, 0]),
            np.array([0, 1, 0, 1, 0]),
        ]
        val_col = np.array([
            1, 2, 3, 4, 5
        ])
        raw_groups = storyboard.planner.group_vals(dim_cols, val_col)
        self.assertEquals(4, len(raw_groups))
