import unittest
import numpy as np

from sketch import compress_quant, quantile, quantile_cy


class QuantileTest(unittest.TestCase):
    def test_simple(self):
        xs = np.random.uniform(0, 1, 100)
        xs = np.sort(xs)
        s_comp = compress_quant.SkipCompressor(15)
        res = s_comp.compress(xs)
        self.assertEqual(100, sum(res.values()))

    def test_converge(self):
        n_total = 10000
        summ_size = 15
        xs = np.linspace(0, 1, n_total)

        tot_result = quantile.QuantileResultWrapper()
        n_segs = 100
        for i in range(n_segs):
            s_comp = compress_quant.SkipCompressor(size=summ_size, seed=i, biased=False)
            new_res = s_comp.compress(xs)
            self.assertEqual(summ_size, len(new_res))
            tot_result.update(new_res)

        self.assertAlmostEqual(.5, tot_result.rank(.5)/(n_total*n_segs), 2)

    def test_calc_delta(self):
        xs = np.linspace(0, 1, 100).astype(float)
        saved = np.array([.1, .5, 1], dtype=float)
        saved_weight = np.array([10.0, 40.0, 50.0], dtype=float)
        delta = quantile_cy.fast_delta(xs, saved, saved_weight)
        self.assertEqual(0, delta[-1])

    def test_find_save(self):
        xs = np.linspace(0, 1, 1000).astype(float)
        saved = np.array([.5], dtype=float)
        saved_weight = np.array([500.0], dtype=float)
        new_weight = 500.0
        new_saved, _ = compress_quant.find_next_c(xs, saved, saved_weight, new_weight=new_weight)
        self.assertAlmostEqual(.25, new_saved, 2)

    def test_compressor(self):
        xs = np.linspace(0, 1, 1000).astype(float)
        cc = compress_quant.CoopCompressor(2)
        for i in range(3):
            res = cc.compress(xs)
            print(res)

