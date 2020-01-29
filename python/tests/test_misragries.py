from unittest import TestCase
import numpy as np
import sketch.misragries


class TestMisraGries(TestCase):
    def test_tiny(self):
        np.random.seed(0)
        xs = np.random.zipf(2.0, 1000)
        mg = sketch.misragries.MisraGries(20)
        for x in xs:
            mg.update(x)
        print(mg.get_dict())
