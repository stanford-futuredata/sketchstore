import math
from collections import defaultdict
from typing import Dict, Any, Mapping
from sketch.frequent_cy import find_t

import numpy as np
import random

#
# def find_t(counts, s):
#     sum_rest = np.sum(counts)
#     cur_t = sum_rest / s
#     found_tail = False
#     tail_idx = 0
#     for tail_idx in range(len(counts)):
#         if tail_idx > 0:
#             sum_rest -= counts[tail_idx-1]
#         cur_t = sum_rest / (s-tail_idx)
#         if counts[tail_idx] < cur_t:
#             found_tail = True
#             break
#     if not found_tail:
#         cur_t = 0
#         tail_idx = len(counts)
#     return cur_t, tail_idx


class IncrementalRangeCompressor:
    def __init__(self, size):
        self.deltas = defaultdict(float)
        self.size = size

    def reset(self):
        self.deltas = defaultdict(float)

    def compress(
            self,
            item_dict: Dict[Any, float],
    ) -> Dict[Any, float]:
        seg_counts = np.array(list(item_dict.values()), dtype=float)
        t,_ = find_t(seg_counts, self.size)

        keys_to_store = set()
        for k, v in item_dict.items():
            self.deltas[k] += v
            if v > t:
                keys_to_store.add(k)

        # ordered_deficit = sorted(
        #     [e for e in self.deltas.items()],
        #     key=lambda e: -e[1]
        # )
        ordered_deficit = sorted(
            [e for e in item_dict.items()],
            key=lambda e: -self.deltas.get(e[0])
        )

        for top_k, top_v in ordered_deficit:
            if len(keys_to_store) >= self.size:
                break
            if top_k in keys_to_store:
                continue
            keys_to_store.add(top_k)

        items_to_store = dict()
        for cur_key in keys_to_store:
            deficit_amt = self.deltas[cur_key]
            if item_dict.get(cur_key, 0) >= t:
                store_val = item_dict[cur_key]
            else:
                # min_val = max(self.deltas[cur_key] - t, 0)
                max_val = item_dict.get(cur_key, 0.0) + t
                store_val = np.clip(deficit_amt, 0, max_val)
            items_to_store[cur_key] = store_val
            self.deltas[cur_key] -= store_val

        return items_to_store


class RandomSampleCompressor:
    def __init__(self, size, seed=0, unbiased=True):
        self.size = size
        self.random = np.random.RandomState(seed=seed)
        self.unbiased = unbiased

    def compress(
            self,
            item_dict: Dict[Any, int],
    ) -> Dict[Any, float]:
        new_size = self.size
        items = []
        for cur_key, cur_count in item_dict.items():
            items += ([cur_key] * int(cur_count))
        items = np.array(items)
        if len(items) < new_size:
            new_size = len(items)

        sampled = self.random.choice(items, size=new_size, replace=False)

        compressed_items = dict()
        inc_amt = 1
        if self.unbiased:
            inc_amt = len(items) / new_size
        for item in sampled:
            compressed_items[item] = compressed_items.get(item, 0.0) + inc_amt

        return compressed_items


class TopValueCompressor:
    def __init__(self, max_value):
        self.max_value = max_value

    def compress(
            self,
            item_dict: Dict[Any, int],
    ) -> Dict[Any, float]:
        compressed_items = dict()
        for k,v in item_dict.items():
            if k <= self.max_value:
                compressed_items[k] = v
        return compressed_items


class TruncationCompressor:
    def __init__(self, size):
        self.size = size
        self.threshold = 0

    def compress(
            self,
            item_dict: Dict[Any, int],
    ) -> Dict[Any, float]:
        new_size = self.size
        item_list = sorted(item_dict.items(), key=lambda x: -x[1])
        compressed_items = dict()
        if len(item_list) < new_size:
            new_size = len(item_list)
        for i in range(new_size):
            cur_key, cur_count = item_list[i]
            compressed_items[cur_key] = cur_count
        if new_size < len(item_list):
            self.threshold = item_list[new_size][1]
        else:
            self.threshold = 0
        return compressed_items


class HairCombCompressor:
    def __init__(self, size, seed=0, unbiased=True):
        self.size = size
        self.random = random.Random()
        self.random.seed(seed)
        self.threshold = 0
        self.tail_idx = 0
        self.unbiased = unbiased

    def compress(
            self,
            item_dict: Dict[Any, float],
    ) -> Dict[Any, float]:
        if self.size == 0:
            return dict()

        item_list = sorted(item_dict.items(), key=lambda x: -x[1])
        n = len(item_list)
        counts = np.array([x[1] for x in item_list])
        self.threshold, self.tail_idx = find_t(counts, self.size)
        compressed_items = dict()

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
                if self.unbiased:
                    compressed_items[cur_item[0]] = float(self.threshold)
                else:
                    compressed_items[cur_item[0]] = float(cur_item[1])

        return compressed_items


class PPSCompressor:
    def __init__(self, size, seed=0, unbiased=True):
        self.size = size
        self.random = random.Random()
        self.random.seed(seed)
        self.threshold = 0
        self.tail_idx = 0
        self.unbiased = unbiased

    def set_size(self, size):
        self.size = size

    def compress(
            self,
            item_dict: Mapping[Any, float],
    ) -> Dict[Any, float]:
        item_list = sorted(item_dict.items(), key=lambda x: -x[1])
        counts = np.array([x[1] for x in item_list])
        self.threshold, self.tail_idx = find_t(counts, self.size)
        self.threshold = int(math.ceil(self.threshold))
        compressed_items = dict()

        for key, count in item_list:
            if count > self.threshold:
                compressed_items[key] = count
            else:
                thresh_ratio = count * 1.0 / self.threshold
                r = self.random.random()
                if r < thresh_ratio:
                    if self.unbiased:
                        target_val = self.threshold
                    else:
                        target_val = count
                    compressed_items[key] = target_val

        return compressed_items
