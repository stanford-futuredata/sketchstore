import unittest
import numpy as np

from sketch import compress_quant, quantile_cy
from sketch.compressor import QuantileResultWrapper
import sketch.kll


class QuantileTest(unittest.TestCase):
    def test_simple(self):
        xs = np.linspace(0, 1, 100)
        xs = np.sort(xs)
        s_comp = compress_quant.SkipCompressor(15)
        res = s_comp.compress(xs)
        self.assertEqual(100, sum(res.values()))

        t_comp = compress_quant.SkipCompressor(2, biased=True)
        res = t_comp.compress(xs)
        r_keys = list(res.keys())
        self.assertAlmostEqual(.25, r_keys[0], 2)

    def test_ranktrack(self):
        xs = np.array([0, 0.1, 0.2, 0.5, 0.9, 1])
        rt = compress_quant.RankTracker(x_tracked=[0, 0.5, 1])
        res = rt.compress(xs)
        self.assertEqual(1, res[0.0])
        self.assertEqual(2, res[1.0])

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

    def test_compressors(self):
        n_seg = 100
        n_per = 1000
        s = 3
        xs = np.linspace(0, 1, n_per).astype(float)
        ccs = [
            # compress_quant.CoopCompressor(),
            compress_quant.CoopCompressorFinite(),
            compress_quant.SkipCompressor(biased=False),
            compress_quant.SkipCompressor(biased=True),
            compress_quant.RankTracker([.1, .5, .9]),
            compress_quant.QRandomSampleCompressor(),
        ]
        q_res = [QuantileResultWrapper() for _ in ccs]
        names = [
            "coop",
            "skiprand",
            "skipbias",
            "ranktrack",
            "random"
        ]
        expected_acc = [1, 1, 0, 3, 1]
        for cc_idx in range(len(ccs)):
            cur_cc = ccs[cc_idx]
            cur_res_acc = q_res[cc_idx]
            print(names[cc_idx])
            for i in range(n_seg):
                new_res = cur_cc.compress(xs, s)
                self.assertLess(len(new_res), s+1)
                cur_res_acc.update(new_res)
            print(cur_res_acc.rank(.9))
            # self.assertAlmostEqual(.1, cur_res_acc.rank(.1)/(n_seg*n_per), expected_acc[cc_idx])
            # self.assertAlmostEqual(.9, cur_res_acc.rank(.9)/(n_seg*n_per), expected_acc[cc_idx])


    def test_kll(self):
        xs = np.random.uniform(0, 1, 1000)
        ks = sketch.kll.KLL(k=2)
        for x in xs:
            ks.update(x)
        ks.compress()
        print(ks.compactors)
