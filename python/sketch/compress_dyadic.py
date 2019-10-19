import math
from typing import Dict, Any, Mapping, List

import numpy as np
import random

from sortedcontainers import SortedList

from sketch.compress_freq import TruncationCompressor, PPSCompressor, UniformSamplingCompressor
from sketch.compress_quant import SkipCompressor


class DyadicFrequencyCompressor:
    def __init__(self,  max_height, base: int=2):
        self.max_height = max_height
        self.count_hierarchy = {h: dict() for h in range(max_height+1)}
        self.base = base
        self.countdowns = [self.base**i for i in range(max_height+1)]
        self.truncator = TruncationCompressor()
        self.current_idx = 0

    def compress(self, item_dict: Dict[Any, float], size: float) -> List[Dict[Any, float]]:
        output_counts = []
        for level_idx in range(self.max_height+1):
            cur_level_count = self.count_hierarchy[level_idx]
            for k, v in item_dict.items():
                cur_level_count[k] = cur_level_count.get(k, 0) + v
            self.countdowns[level_idx] -= 1
            if self.countdowns[level_idx] == 0:
                cur_output = self.truncator.compress(
                    cur_level_count,
                    size=int(math.ceil(size*self.base**level_idx))
                )
                output_counts.append(cur_output)
                self.countdowns[level_idx] = self.base**level_idx
                self.count_hierarchy[level_idx] = dict()

        return output_counts


class DyadicQuantileCompressor:
    def __init__(self, max_height, base=2):
        self.max_height = max_height
        self.x_hierarchy = {h: [] for h in range(max_height+1)}
        self.base = base
        self.countdowns = [self.base**i for i in range(max_height+1)]
        self.truncator = SkipCompressor(seed=0, biased=True)
        self.current_idx = 0

    def compress(
            self,
            seg_xs: np.ndarray,
            size: int
    ) -> List[Dict[Any, float]]:
        output_counts = []
        for level_idx in range(self.max_height+1):
            cur_level_xs = self.x_hierarchy[level_idx]
            # cur_level_xs.update(seg_xs)
            cur_level_xs.extend(seg_xs)
            # for x in seg_xs:
            #     cur_level_xs.append(x)
            self.countdowns[level_idx] -= 1
            if self.countdowns[level_idx] == 0:
                cur_output = self.truncator.compress(
                    cur_level_xs,
                    size=int(math.ceil(size*self.base**level_idx)))
                output_counts.append(cur_output)
                self.countdowns[level_idx] = self.base**level_idx
                self.x_hierarchy[level_idx] = []

        return output_counts

