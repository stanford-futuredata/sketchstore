import unittest
import numpy as np


import q_process_linear_out

class QuantileProcessorTest(unittest.TestCase):
    def test_counttovec(self):
        xs = {1: 2, 2: 10, 100:5}
        x_to_track = [1, 2, 3, 200]
        res_vec = q_process_linear_out.count_to_vec(x_to_track, xs)
        print(res_vec)
