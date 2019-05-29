import math
from typing import Dict, Any, Mapping, List

import numpy as np
import random

from sketch.compress_freq import TruncationCompressor


class DyadicFrequencyCompressor:
    def __init__(self, size, max_height):
        self.size = size
        self.max_height = max_height
        self.count_hierarchy = {h: dict() for h in range(max_height+1)}
        self.countdowns = [2**i for i in range(max_height+1)]
        self.truncators = [TruncationCompressor(size=int(size*2**i)) for i in range(max_height+1)]
        self.current_idx = 0

    def compress(
            self,
            item_dict: Dict[Any, int],
    ) -> List[Dict[Any, float]]:
        output_counts = []
        for level_idx in range(self.max_height+1):
            cur_level_count = self.count_hierarchy[level_idx]
            for k, v in item_dict.items():
                cur_level_count[k] = cur_level_count.get(k, 0) + v
            self.countdowns[level_idx] -= 1
            if self.countdowns[level_idx] == 0:
                cur_output = self.truncators[level_idx].compress(cur_level_count)
                output_counts.append(cur_output)
                self.countdowns[level_idx] = 2**level_idx
                self.count_hierarchy[level_idx] = dict()

        return output_counts