from typing import Dict, Any

import numpy as np
import random
import copy
import probables
import bounter


class ExactCounterSketch:
    def __init__(self):
        self.items = dict()

    def add(self, xs):
        for x in xs:
            self.items[x] = self.items.get(x, 0.0) + 1

    def get_dict(self):
        return copy.copy(self.items)

    def update(self, counts: Dict[Any, float]):
        for k, v in counts.items():
            self.items[k] = self.items.get(k, 0.0) + v


class CountMinSketchFast:
    def __init__(self, size=100, seed=0, unbiased=False, max_val=100):
        self.size = size
        self.max_val = max_val
        self.cms = bounter.count_min_sketch.CountMinSketch(width=size, depth=5)

    def add(self, xs):
        self.cms.update((str(x) for x in xs))

    def get_dict(self):
        item_counts = dict()
        for i in range(self.max_val):
            item_counts[i] = self.cms[str(i)]
        return item_counts


class CountMinSketchOld:
    def __init__(self, size=100, seed=0, unbiased=False, max_val=100):
        self.size = size
        self.max_val = max_val
        if unbiased:
            self.cms = probables.countminsketch.CountMeanMinSketch(width=size, depth=5)
        else:
            self.cms = probables.countminsketch.CountMinSketch(width=size, depth=5)

    def add(self, xs):
        for x in xs:
            self.cms.add(str(x))

    def get_dict(self):
        item_counts = dict()
        for i in range(self.max_val):
            item_counts[i] = self.cms.check(str(i))
        return item_counts

    def update(self, counts: Dict[Any, float]):
        for k, v in counts.items():
            self.cms.add(str(k), int(v))
            

class SpaceSavingSketch:
    def __init__(self, size=100, seed=0, unbiased=False):
        self.size = size
        self.items = [None] * size
        self.counts = np.zeros(shape=size, dtype=float)
        self.random = random.Random()
        self.random.seed(seed)
        self.unbiased = unbiased

    def add_single(self, x):
        if x in self.items:
            x_idx = self.items.index(x)
            self.counts[x_idx] += 1
        else:
            min_idx = np.argmin(self.counts)
            min_count = self.counts[min_idx]
            self.counts[min_idx] = min_count+1
            if self.unbiased:
                p_replace = 1.0/(min_count+1)
                r = self.random.random()
                if r <= p_replace:
                    self.items[min_idx] = x
            else:
                self.items[min_idx] = x

    def add(self, xs):
        for x in xs:
            self.add_single(x)

    def get_dict(self):
        entries = dict()
        for i in range(self.size):
            entries[self.items[i]] = self.counts[i]
        return entries
