import numpy as np
from typing import List

class Tuple(object):
    __slots__ = ["val", "g", "delta"]

    def __init__(self, val, g, delta):
        self.val = val
        self.g = g
        self.delta = delta

    def __repr__(self):
        return '{}[{},{}]'.format(self.val, self.g, self.delta)


class GKArray(object):
    def __init__(self, eps=None):
        self.eps = eps
        self.tuples = []
        self.nSize = 0

    def __repr__(self):
        return str(self.tuples)

    def mergeInternal(self, addTuples: List[Tuple]):
        numAdded = sum([t.g for t in addTuples])
        oldSize = self.nSize
        self.nSize += numAdded
        tuples = self.tuples

        i1 = 0
        n1 = len(tuples)
        i2 = 0
        n2 = len(addTuples)

        threshold = 2.0*self.eps*self.nSize

        newTuples = []
        tLast = None
        i = 0
        while i1 < n1 or i2 < n2:
            if i > 0:
                tLast = newTuples[i-1]
            if i1 == n1:
                t2 = addTuples[i2]
                tNext = t2
                i2 += 1
            elif i2 == n2:
                t1 = tuples[i1]
                tNext = t1
                i1 += 1
            else:
                t1 = tuples[i1]
                t2 = addTuples[i2]
                if t1.val <= t2.val:
                    tNext = t1
                    tNext.delta += (t2.g + t2.delta - 1)
                    i1 += 1
                else:
                    tNext = t2
                    tNext.delta += (t1.g + t1.delta - 1)
                    i2 += 1
            if ((i > 1) and tLast.g + tNext.g + tNext.delta <= threshold):
                tNext.g += tLast.g
                newTuples[i-1] = tNext
            else:
                newTuples.append(tNext)
                i += 1
        self.tuples = newTuples

    def add_pairs(self, val_weight_pairs):
        sorted_vals = sorted(val_weight_pairs, key=lambda x: x[0])
        tuples = [Tuple(vp[0], vp[1], 0) for vp in sorted_vals]
        self.mergeInternal(tuples)

    def get_dict(self):
        return {t.val: t.g for t in self.tuples}
