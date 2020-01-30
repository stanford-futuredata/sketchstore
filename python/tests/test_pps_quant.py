from unittest import TestCase

import numpy as np
import sketch.pps_quant
from storyboard.query_cy import CDFSketch


class TestPPSQuantSketch(TestCase):
    def test_tiny(self):
        np.random.seed(0)
        xs1 = np.linspace(0,1,1000)
        xs2 = np.linspace(1,2,1000)
        gk = sketch.pps_quant.PPSQuantSketch(size=40)
        gk.update([(x,1) for x in xs1])
        gk.update([(x,2) for x in xs2])
        print(len(gk.get_dict()))

        cdf = CDFSketch(gk.get_dict())
        print(cdf.estimate(1.25))
