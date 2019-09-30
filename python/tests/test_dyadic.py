import unittest
import sketch.compress_dyadic as dyadic
import numpy as np
import pandas as pd
from collections import defaultdict


class TestDyadicFrequency(unittest.TestCase):
    def test_tiny(self):
        new_size = 2
        dy = dyadic.DyadicFrequencyCompressor(size=new_size, max_height=1)
        counts = defaultdict(int)
        counts.update({
            1: 10,
            2: 5,
            3: 3,
            4: 2
        })
        cum_counts = []
        for i in range(4):
            new_counts = dy.compress(counts)
            cum_counts.append(new_counts)
        self.assertEqual(2, len(cum_counts[1]))
        self.assertEqual(4, len(cum_counts[1][1]))
        self.assertEqual(4, cum_counts[1][1][4])

    def test_quantile(self):
        new_size = 2
        dy = dyadic.DyadicQuantileCompressor(size=new_size, max_height=1)
        xs = np.linspace(0, 1, 101)

        cum_counts = []
        for i in range(4):
            new_counts = dy.compress(xs)
            cum_counts.append(new_counts)
            print(new_counts)

    def test_small(self):
        new_size = 2
        dy = dyadic.DyadicFrequencyCompressor(size=new_size, max_height=2)
        for i in range(8):
            x_stream = np.random.zipf(1.1, size=1000)
            counts = dict(pd.Series(x_stream).value_counts())
            new_counts = dy.compress(counts)
