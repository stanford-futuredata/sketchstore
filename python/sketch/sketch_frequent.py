from typing import Dict, Any

import numpy as np
import random
import copy
import probables
import bounter


class CountMinSketchFast:
    def __init__(self, size=100, seed=0, unbiased=False, x_to_track=None):
        self.size = size
        if x_to_track is None:
            x_to_track = range(100)
        self.x_to_track = set(x_to_track)
        self.cms = bounter.count_min_sketch.CountMinSketch(width=size, depth=5)

    def add(self, xs):
        self.cms.update((str(x) for x in xs))

    def get_dict(self):
        return {i: self.cms[str(i)] for i in self.x_to_track}


class CountMinSketchOld:
    def __init__(self, size=100, seed=0, unbiased=False, x_to_track=None):
        self.size = size
        if x_to_track is None:
            x_to_track = set(range(100))
        self.x_to_track = x_to_track
        if unbiased:
            self.cms = probables.countminsketch.CountMeanMinSketch(width=size, depth=5)
        else:
            self.cms = probables.countminsketch.CountMinSketch(width=size, depth=5)

    def add(self, xs):
        for x in xs:
            self.cms.add(str(x))

    def get_dict(self):
        return {i: self.cms.check(str(i)) for i in self.x_to_track}

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
