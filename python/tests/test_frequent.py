import unittest

from sketch.frequent import SpaceSavingSketch


class SpaceSavingTest(unittest.TestCase):
    def test_simple(self):
        xs = list(range(100)) + [1] * 50
        ss = SpaceSavingSketch(size=10)
        ss.add(xs)
        ss_counts = ss.get_dict()
        self.assertLess(ss_counts[1], 70)
        self.assertGreater(ss_counts[1], 50)

        ss.add(xs)
        ss_counts = ss.get_dict()
        self.assertLess(ss_counts[1], 120)
        self.assertGreater(ss_counts[1], 100)
