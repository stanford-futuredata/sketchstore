import unittest
import sketch.kll as kll
import numpy as np
import pandas as pd
from collections import defaultdict


class TestKLL(unittest.TestCase):
    def test_tiny(self):
        sketch = kll.KLL(k=10)
        xs = np.random.uniform(0, 1, size=10000)
        for x in xs:
            sketch.update(x)
        sketch.compress()
        res = sketch.get_dict()
        print(len(res))
        print(res)
        print(sum([v for k,v in res.items() if k < .5]))
