import numpy as np
import random
from collections import defaultdict
import copy


class ExactCounterSketch:
    def __init__(self):
        self.items = defaultdict(int)

    def add(self, xs):
        for x in xs:
            self.items[x] += 1

    def get_dict(self):
        return copy.copy(self.items)


class SpaceSavingSketch:
    def __init__(self, size=100, seed=0, unbiased=False):
        self.size = size
        self.items = [None] * size
        self.counts = np.zeros(shape=size, dtype=int)
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
            p_replace = 1.0/(min_count+1)
            r = self.random.random()
            self.counts[min_idx] = min_count+1
            if self.unbiased and r < p_replace:
                self.items[min_idx] = x

    def add(self, xs):
        for x in xs:
            self.add_single(x)

    def get_dict(self):
        entries = defaultdict(int)
        for i in range(self.size):
            entries[self.items[i]] = self.counts[i]
        return entries
