import math
from typing import Dict, Any, Mapping, Iterable
import numpy as np
import random

from sketch.frequent_cy import find_t
from sketch.compressor import ItemDictCompressor


def apply_bias(item_dict: Dict[Any, float], bias: float) -> Dict[Any, float]:
    return {
        k: v-bias for k, v in item_dict.items()
        if v > bias
    }


class IncrementalRangeCompressor(ItemDictCompressor):
    def __init__(self, max_t=None):
        self.deltas = dict()
        self.max_t = max_t
        self.cur_t = 0

    def reset(self):
        self.deltas = dict()
        self.cur_t = 0

    def compress(self, item_dict: Dict[Any, float], size: int) -> Dict[Any, float]:
        item_list = sorted(item_dict.items(), key=lambda x: -x[1])
        counts = np.array([x[1] for x in item_list], dtype=float)
        t,_ = find_t(counts, size)

        keys_to_store = set()
        for k, v in item_dict.items():
            self.deltas[k] = self.deltas.get(k, 0) + v
            if v > t:
                keys_to_store.add(k)

        # ordered_deficit = sorted(
        #     [e for e in self.deltas.items()],
        #     key=lambda e: -e[1]
        # )
        ordered_deficit = sorted(
            [e for e in item_dict.items() if e[0] not in keys_to_store],
            key=lambda e: -self.deltas.get(e[0])
        )

        for top_k, top_v in ordered_deficit:
            if len(keys_to_store) >= size:
                break
            if top_k in keys_to_store:
                continue
            keys_to_store.add(top_k)

        items_to_store = dict()
        for cur_key in keys_to_store:
            deficit_amt = self.deltas.get(cur_key, 0)
            if item_dict.get(cur_key, 0) >= t:
                store_val = item_dict[cur_key]
            else:
                # min_val = max(self.deltas[cur_key] - t, 0)
                # max_val = item_dict.get(cur_key, 0.0) + t
                max_val = t
                store_val = np.clip(deficit_amt, 0, max_val)
            items_to_store[cur_key] = store_val
            self.deltas[cur_key] -= store_val

        self.cur_t += 1
        if self.cur_t == self.max_t:
            self.reset()
        return items_to_store


class NopCompressor(ItemDictCompressor):
    def compress(self, item_dict: Dict[Any, float], size: int) -> Dict[Any, float]:
        return item_dict


class TopValueCompressor(ItemDictCompressor):
    def __init__(self, x_to_track):
        self.x_to_track = list(x_to_track)

    def compress(self, item_dict: Dict[Any, float], size: int) -> Dict[Any, float]:
        return {k: item_dict.get(k, 0) for k in self.x_to_track}


class TruncationCompressor(ItemDictCompressor):
    def __init__(self):
        self.threshold = 0

    def compress(self, item_dict: Dict[Any, float], size: int) -> Dict[Any, float]:
        new_size = size
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


class HairCombCompressor(ItemDictCompressor):
    def __init__(self, seed=0):
        self.random = random.Random()
        self.random.seed(seed)
        self.threshold = 0
        self.tail_idx = 0

    def compress(self, item_dict: Dict[Any, float], size: int) -> Dict[Any, float]:
        if size == 0:
            return dict()

        item_list = sorted(item_dict.items(), key=lambda x: -x[1])
        n = len(item_list)
        b_counts = np.array([x[1] for x in item_list], dtype=float)

        self.threshold, self.tail_idx = find_t(b_counts, size)
        compressed_items = dict()

        for i in range(self.tail_idx):
            cur_item = item_list[i]
            compressed_items[cur_item[0]] = cur_item[1]

        rand_shift = self.random.uniform(0, self.threshold)
        running_sum = 0.0
        for i in range(self.tail_idx, n):
            cur_item = item_list[i]
            # running_sum += cur_item[1]
            running_sum += b_counts[i]
            if b_counts[i] == 0:
                break
            if running_sum > rand_shift:
                running_sum -= self.threshold
                compressed_items[cur_item[0]] = float(self.threshold)

        return compressed_items


class PPSCompressor(ItemDictCompressor):
    def __init__(self, seed=0):
        self.random = random.Random()
        self.random.seed(seed)
        self.threshold = 0
        self.tail_idx = 0

    def compress(self, item_dict: Dict[Any, float], size: int) -> Dict[Any, float]:
        item_list = sorted(item_dict.items(), key=lambda x: -x[1])
        counts = np.array([x[1] for x in item_list], dtype=float)
        self.threshold, self.tail_idx = find_t(counts, size)
        self.threshold = int(math.ceil(self.threshold))
        compressed_items = dict()

        for key, count in item_list:
            if count > self.threshold:
                compressed_items[key] = count
            else:
                thresh_ratio = count * 1.0 / self.threshold
                r = self.random.random()
                if r < thresh_ratio:
                    target_val = self.threshold
                    compressed_items[key] = target_val

        return compressed_items


class UniformSamplingCompressor(ItemDictCompressor):
    def __init__(self, seed=0):
        self.random = np.random.RandomState(seed)

    def compress(self, item_dict: Dict[Any, float], size: int) -> Dict[Any, float]:
        items = list(item_dict.keys())
        weights = np.array(list(item_dict.values()))
        tot_weight = np.sum(weights)
        ps = weights / tot_weight
        x_sampled = self.random.choice(items, size=size, replace=True, p=ps)

        sample_increment = tot_weight / size
        return_dict = dict()
        for x in x_sampled:
            return_dict[x] = return_dict.get(x, 0) + sample_increment
        return return_dict

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
