import unittest
import sketch.compress_freq as c
import numpy as np
from collections import defaultdict


class TestCompressFreq(unittest.TestCase):
    def test_findt(self):
        counts = np.array([10, 5, 3, 2])
        t, i = c.find_t(counts, 2)
        self.assertEqual(10, t)
        self.assertEqual(1, i)

    def test_compress(self):
        hc = c.HairCombCompressor(seed=0)
        counts = defaultdict(int)
        counts.update({
            1: 10,
            2: 5,
            3: 3,
            4: 2
        })
        new_size = 2
        new_counts = hc.compress(counts, new_size=new_size, unbiased=True)
        self.assertEqual(len(new_counts), new_size)

        pps = c.PPSCompressor(seed=0)
        new_counts = pps.compress(counts, new_size=new_size, unbiased=True)
        self.assertEqual(10, new_counts[1])

        tc = c.TruncationCompressor()
        new_counts = tc.compress(counts, new_size=new_size)
        self.assertEqual(5, new_counts[2])
