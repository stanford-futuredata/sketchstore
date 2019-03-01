from typing import Dict, Any

import numpy as np
import random
from collections import defaultdict


def find_t(counts, s):
    sum_rest = np.sum(counts)
    cur_t = sum_rest / s
    found_tail = False
    tail_idx = 0
    for tail_idx in range(len(counts)):
        if tail_idx > 0:
            sum_rest -= counts[tail_idx-1]
        cur_t = sum_rest / (s-tail_idx)
        if counts[tail_idx] < cur_t:
            found_tail = True
            break
    if not found_tail:
        cur_t = 0
        tail_idx = len(counts)
    return cur_t, tail_idx


class TruncationCompressor:
    def __init__(self):
        self.threshold = 0

    def compress(
            self,
            item_dict: Dict[Any, int],
            new_size: int,
    ) -> Dict[Any, float]:
        item_list = sorted(item_dict.items(), key=lambda x: -x[1])
        compressed_items = defaultdict(float)
        for i in range(new_size):
            cur_key, cur_count = item_list[i]
            compressed_items[cur_key] = cur_count
        if new_size < len(item_list):
            self.threshold = item_list[new_size][1]
        else:
            self.threshold = 0
        return compressed_items


class HairCombCompressor:
    def __init__(self, seed=0):
        self.random = random.Random()
        self.random.seed(seed)
        self.threshold = 0
        self.tail_idx = 0

    def compress(
            self,
            item_dict: Dict[Any, int],
            new_size: int,
            unbiased: bool = True
    ) -> Dict[Any, float]:
        item_list = sorted(item_dict.items(), key=lambda x: -x[1])
        n = len(item_list)
        counts = np.array([x[1] for x in item_list])
        self.threshold, self.tail_idx = find_t(counts, new_size)
        compressed_items = defaultdict(float)

        for i in range(self.tail_idx):
            cur_item = item_list[i]
            compressed_items[cur_item[0]] = float(cur_item[1])

        rand_shift = self.random.uniform(0, self.threshold)
        running_sum = 0.0
        for i in range(self.tail_idx, n):
            cur_item = item_list[i]
            running_sum += cur_item[1]
            if running_sum > rand_shift:
                running_sum -= self.threshold
                if unbiased:
                    compressed_items[cur_item[0]] = float(self.threshold)
                else:
                    compressed_items[cur_item[0]] = float(cur_item[1])

        return compressed_items


class PPSCompressor:
    def __init__(self, seed=0):
        self.random = random.Random()
        self.random.seed(seed)
        self.threshold = 0
        self.tail_idx = 0

    def compress(
            self,
            item_dict: Dict[Any, int],
            new_size: int,
            unbiased: bool = True
    ) -> Dict[Any, float]:
        item_list = sorted(item_dict.items(), key=lambda x: -x[1])
        counts = np.array([x[1] for x in item_list])
        self.threshold, self.tail_idx = find_t(counts, new_size)
        compressed_items = defaultdict(float)

        for key, count in item_list:
            if count > self.threshold:
                compressed_items[key] = count
            else:
                thresh_ratio = count * 1.0 / self.threshold
                r = self.random.random()
                if r < thresh_ratio:
                    if unbiased:
                        target_val = self.threshold
                    else:
                        target_val = count
                    compressed_items[key] = target_val

        return compressed_items
