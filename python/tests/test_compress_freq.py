import unittest
import sketch.compress_freq as c
import sketch.frequent as f
import numpy as np
from collections import defaultdict


class TestCompressFreq(unittest.TestCase):
    def test_findt(self):
        counts = np.array([10, 5, 3, 2])
        t, i = c.find_t(counts, 2)
        self.assertEqual(10, t)
        self.assertEqual(1, i)

    def test_compress(self):
        new_size = 2
        hc = c.HairCombCompressor(new_size, seed=0, unbiased=True)
        counts = defaultdict(int)
        counts.update({
            1: 10,
            2: 5,
            3: 3,
            4: 2
        })
        new_counts = hc.compress(counts)
        self.assertEqual(len(new_counts), new_size)

        pps = c.PPSCompressor(new_size, seed=0, unbiased=True)
        new_counts = pps.compress(counts)
        self.assertEqual(10, new_counts[1])

        tc = c.TruncationCompressor(new_size)
        new_counts = tc.compress(counts)
        self.assertEqual(5, new_counts[2])

        rs = c.RandomSampleCompressor(new_size, seed=0)
        new_counts = rs.compress(counts)

    def test_incremental(self):
        size = 2
        ic = c.IncrementalRangeCompressor(size)
        counts = defaultdict(int)
        counts.update({
            1: 10,
            2: 5,
            3: 3,
            4: 2
        })
        s1 = ic.compress(counts)
        self.assertEqual(10, s1[1])
        self.assertEqual(5, s1[2])
        s2 = ic.compress(counts)
        self.assertEqual(6, s2[3])

    def test_unbiased(self):
        counts = defaultdict(int)
        counts.update({
            1: 10,
            2: 5,
            3: 3,
            4: 2
        })
        new_size = 2

        ec = f.ExactCounterSketch()
        pps = c.PPSCompressor(new_size, seed=0, unbiased=True)
        for i in range(100):
            new_counts = pps.compress(counts)
            ec.update(new_counts)

        total_counts = ec.get_dict()
        self.assertAlmostEqual(1.0, total_counts[1] / 1000.0, places=5)
        self.assertAlmostEqual(1.0, total_counts[2] / 500.0, places=1)

    def test_bias(self):
        counts = defaultdict(int)
        counts.update({
            1: 10,
            2: 5,
            3: 3,
            4: 2
        })
        new_size = 2
        pps = c.HairCombCompressor(new_size, seed=0, unbiased=True, bias=1)
        compressed = pps.compress(counts)
        self.assertAlmostEqual(10, compressed[1], places=5)
        self.assertAlmostEqual(17, sum(compressed.values()), places=5)


