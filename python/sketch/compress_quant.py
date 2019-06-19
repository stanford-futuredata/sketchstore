import math
from typing import Dict, Any, List
import numpy as np
import random
import sketch.quantile_cy
import tdigest.tdigest
import sortednp as snp


class RankTracker:
    def __init__(self, x_tracked: List):
        self.x_tracked = np.array(x_tracked)

    def compress(
            self,
            xs,
    ) -> Dict[Any, float]:
        # negate to make ranges (l,r]
        bin_edges = np.concatenate([[-np.inf], self.x_tracked])
        bin_weights, _ = np.histogram(-xs, -bin_edges[::-1])
        x_counts = {self.x_tracked[i]: bin_weights[::-1][i] for i in range(len(self.x_tracked))}
        return x_counts

class SkipCompressor:
    def __init__(self, size, seed=0, biased=False):
        self.size = size
        self.random = random.Random()
        self.random.seed(seed)
        self.biased = biased

    def compress(
            self,
            x_sorted
    ) -> Dict[Any, float]:
        n = len(x_sorted)
        skip = int(math.ceil(n/self.size))
        saved = dict()

        start_idx = 0
        end_idx = skip
        while end_idx <= n:
            if self.biased:
                seg_offset = skip // 2
            else:
                seg_offset = self.random.randrange(0, skip)
            to_save = x_sorted[start_idx + seg_offset]
            saved[to_save] = skip
            start_idx = end_idx
            end_idx += skip

        if end_idx > n and start_idx < n:
            seg_size = n - start_idx
            if self.biased:
                seg_offset = seg_size // 2
            else:
                seg_offset = self.random.randrange(0, seg_size)
            to_save = x_sorted[start_idx + seg_offset]
            saved[to_save] = seg_size

        return saved


class QRandomSampleCompressor:
    def __init__(self, size, seed=0, unbiased=True):
        self.size = size
        self.random = np.random.RandomState(seed=seed)

    def compress(
            self,
            xs
    ) -> Dict[Any, float]:
        new_size = self.size
        sampled = self.random.choice(xs, size=new_size, replace=False)

        compressed_items = dict()
        inc_amt = len(xs) / new_size
        for x in sampled:
            compressed_items[x] = compressed_items.get(x, 0.0) + inc_amt

        return compressed_items


def loss(f):
    return f**2
# def loss(f):
#     a=1
#     return np.cosh(a*f)

def find_next_c(xvals, saved, saved_weight, new_weight, seg_start=None, seg_end=None):
    # print("finding")
    # print("range:{}-{}".format(xvals[0],xvals[-1]))
    # print("saved:{}".format(saved))
    d = np.asarray(sketch.quantile_cy.fast_delta(xvals, saved, saved_weight))

    x_left_idx = 0
    x_right_idx = len(xvals)
    if seg_start is not None:
        x_left_idx = np.searchsorted(xvals, seg_start, side="left")
    if seg_end is not None:
        x_right_idx = np.searchsorted(xvals, seg_end, side="right")
    d = d[x_left_idx:x_right_idx]

    scale_f = new_weight
    l_diff = loss((d-new_weight)/scale_f) - loss(d/scale_f)
    l_diff_suff = np.cumsum(l_diff[::-1])[::-1]
    l_diff_best = np.argmax(-l_diff_suff)
    # print("best:{}".format(xvals[l_diff_best]))
    return xvals[x_left_idx+l_diff_best], np.sum(loss(d/scale_f))


class CoopCompressor:
    def __init__(self, size):
        self.size = size
        self.running_stored = np.array([], dtype=float)
        self.stored_weights = np.array([], dtype=float)
        self.running_actual = np.array([], dtype=float)

    def compress(
            self,
            xs
    ) -> Dict[Any, float]:
        x_segs = np.array_split(xs, self.size)

        self.running_actual = np.append(self.running_actual, xs)
        self.running_actual.sort()

        to_save = dict()
        for cur_seg in x_segs:
            seg_start, seg_end = cur_seg[0], cur_seg[-1]
            cur_seg_weight = len(cur_seg)
            # print("merging")
            # print(len(cur_seg))
            # print(len(self.running_actual))
            # print(self.running_actual.dtype)
            # print(cur_seg.dtype)
            # self.running_actual = snp.merge(
            #     self.running_actual, cur_seg
            # )
            # self.running_actual = np.append(self.running_actual, cur_seg)
            # self.running_actual.sort()
            # print("got here")

            cur_to_save, _ = find_next_c(
                self.running_actual,
                self.running_stored,
                self.stored_weights,
                cur_seg_weight,
                seg_start=seg_start,
                seg_end=seg_end
            )
            # print("seg: {}-{}".format(seg_start, seg_end))
            # print("saved: {}".format(cur_to_save))

            to_save[cur_to_save] = cur_seg_weight
            to_save_idx = np.searchsorted(self.running_stored, cur_to_save)
            self.running_stored = np.concatenate((
                self.running_stored[:to_save_idx], [cur_to_save], self.running_stored[to_save_idx:]
            ))
            self.stored_weights = np.concatenate((
                self.stored_weights[:to_save_idx], [cur_seg_weight], self.stored_weights[to_save_idx:]
            ))

        return to_save