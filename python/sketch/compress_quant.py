import math
from typing import Dict, Any, List, Iterable, Sequence
import numpy as np
import random

import sketch.quantile_cy
from sketch.compressor import SeqDictCompressor


class RankTracker(SeqDictCompressor):
    def __init__(self, x_tracked: Sequence):
        self.x_tracked = np.array(x_tracked)

    def compress(self, xs: np.ndarray, size: int) -> Dict[Any, float]:
        # negate to make ranges (l,r]
        bin_edges = np.concatenate([[-np.inf], self.x_tracked])
        bin_weights, _ = np.histogram(-xs, -bin_edges[::-1])
        x_counts = {self.x_tracked[i]: bin_weights[::-1][i] for i in range(len(self.x_tracked))}
        return x_counts


class SkipCompressor(SeqDictCompressor):
    def __init__(self, seed=0, biased=False):
        self.random = random.Random()
        self.random.seed(seed)
        self.biased = biased

    def compress(self, xs: np.ndarray, size: int) -> Dict[Any, float]:
        x_sorted = np.sort(xs)
        n = len(x_sorted)
        skip = int(math.ceil(n/size))
        saved = dict()

        start_idx = 0
        end_idx = skip
        while end_idx <= n:
            if self.biased:
                seg_offset = skip // 2
            else:
                seg_offset = self.random.randrange(0, skip)
            to_save = x_sorted[start_idx + seg_offset]
            saved[to_save] = saved.get(to_save, 0) + skip
            start_idx = end_idx
            end_idx += skip

        if end_idx > n and start_idx < n:
            seg_size = n - start_idx
            if self.biased:
                seg_offset = seg_size // 2
            else:
                seg_offset = self.random.randrange(0, seg_size)
            to_save = x_sorted[start_idx + seg_offset]
            saved[to_save] = saved.get(to_save, 0) + seg_size

        return saved


class QRandomSampleCompressor(SeqDictCompressor):
    def __init__(self, seed=0):
        self.random = np.random.RandomState(seed=seed)

    def compress(self, xs: np.ndarray, size: int) -> Dict[Any, float]:
        new_size = size
        sampled = self.random.choice(xs, size=new_size, replace=True)

        compressed_items = dict()
        inc_amt = len(xs) / new_size
        for x in sampled:
            compressed_items[x] = compressed_items.get(x, 0.0) + inc_amt

        return compressed_items


# def loss(f):
#     return f**2
def loss(f):
    a = 1/math.sqrt(1024)
    return np.cosh(a*f)

def find_next_c(
        xvals, saved, saved_weight, new_weight, seg_start=None, seg_end=None
):
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


class CoopCompressor(SeqDictCompressor):
    def __init__(self):
        self.running_stored = np.array([], dtype=float)
        self.stored_weights = np.array([], dtype=float)
        self.running_actual = np.array([], dtype=float)

    def compress(self, xs: np.ndarray, size: int) -> Dict[Any, float]:
        xs = np.sort(xs)
        x_segs = np.array_split(xs, size)

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


def find_next_c_2(
        xvals: np.ndarray,
        xdeltas: np.ndarray,
        cur_seg_weight: float,
        seg_start,
        seg_end
):
    x_left_idx = 0
    x_right_idx = len(xvals)
    if seg_start is not None:
        x_left_idx = np.searchsorted(xvals, seg_start, side="left")
    if seg_end is not None:
        x_right_idx = np.searchsorted(xvals, seg_end, side="right")
    d = xdeltas[x_left_idx:x_right_idx]

    scale_f = cur_seg_weight
    l_diff = loss((d-cur_seg_weight)/scale_f) - loss(d/scale_f)
    l_diff_suff = np.cumsum(l_diff[::-1])[::-1]
    l_diff_best = np.argmax(-l_diff_suff)

    xdeltas[x_left_idx+l_diff_best:] -= cur_seg_weight
    # print("best:{}".format(xvals[l_diff_best]))
    return xvals[x_left_idx+l_diff_best]


class CoopCompressorFinite(SeqDictCompressor):
    def __init__(self):
        self.true_weight = dict()
        self.stored_weight = dict()

    def compress(self, xs: np.ndarray, size: int) -> Dict[Any, float]:
        for x in xs:
            self.true_weight[x] = self.true_weight.get(x, 0) + 1

        xvals, xdeltas = sketch.quantile_cy.get_deltas_2(
            self.true_weight,
            self.stored_weight
        )
        # print("iter")
        # print(self.stored_weight)
        # print(xvals)
        # print(xdeltas)

        xs = np.sort(xs)
        x_segs = np.array_split(xs, size)

        to_save = dict()
        for cur_seg in x_segs:
            seg_start, seg_end = cur_seg[0], cur_seg[-1]
            cur_seg_weight = len(cur_seg)

            cur_to_save = find_next_c_2(
                xvals,
                xdeltas,
                cur_seg_weight,
                seg_start=seg_start,
                seg_end=seg_end
            )
            # print("seg: {}-{}".format(seg_start, seg_end))
            # print("saved: {}".format(cur_to_save))
            self.stored_weight[cur_to_save] = self.stored_weight.get(cur_to_save, 0) + cur_seg_weight
            to_save[cur_to_save] = to_save.get(cur_to_save, 0) + cur_seg_weight
        return to_save

