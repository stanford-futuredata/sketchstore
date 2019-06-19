import unittest
import numpy as np


import q_process_linear_out

class QuantileProcessorTest(unittest.TestCase):
    def test_counttovec(self):
        xs = {1: 2, 2: 10, 100:5}
        x_to_track = [1, 2, 3, 200]
        res_vec = q_process_linear_out.count_to_vec(x_to_track, xs)
        self.assertEqual(len(x_to_track), len(res_vec))
        self.assertEqual(2, res_vec[0])
        self.assertEqual(12, res_vec[1])
        self.assertEqual(12, res_vec[2])
        self.assertEqual(17, res_vec[-1])
