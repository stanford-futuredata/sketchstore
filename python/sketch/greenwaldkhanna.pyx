# cython: language_level=3

cimport cython
cimport numpy as np
import numpy as np

from libc.math cimport pow, ceil, floor, log

cdef class Tuple:
    cpdef float val
    cpdef float g
    cpdef float delta

    def __init__(self, float val, float g, float delta):
        self.val = val
        self.g = g
        self.delta = delta

    def __repr__(self):
        return '{}[{},{}]'.format(self.val, self.g, self.delta)


cdef class GKArray:
    cpdef float eps
    cpdef list tuples
    cpdef public float nSize

    def __init__(self, float eps):
        self.eps = eps
        self.tuples = []
        self.nSize = 0

    def __repr__(self):
        return str(self.tuples)

    cdef mergeInternal(self, list addTuples):
        cdef Tuple t
        cdef float numAdded = sum([t.g for t in addTuples])
        self.nSize += numAdded
        cdef int i1, i2, n1, n2
        i1 = 0
        n1 = len(self.tuples)
        i2 = 0
        n2 = len(addTuples)
        cdef float threshold = 2.0*self.eps*self.nSize

        cdef list newTuples = []
        cdef Tuple tLast, tNext, t1, t2
        cdef int i = 0
        cdef int useAdded = 0
        while i1 < n1 or i2 < n2:
            useAdded = 0
            if i > 0:
                tLast = newTuples[i-1]

            if i1 < n1:
                t1 = self.tuples[i1]
            else:
                t1 = None
            if i2 < n2:
                t2 = addTuples[i2]
            else:
                t2 = None

            if i1 == n1:
                tNext = t2
                useAdded = 1
                i2 += 1
            elif i2 == n2:
                tNext = t1
                i1 += 1
            else:
                if t1.val <= t2.val:
                    tNext = t1
                    i1 += 1
                else:
                    tNext = t2
                    useAdded = 1
                    i2 += 1
            
            if (i1 < n1) and useAdded:
                tNext.delta = t1.g + t1.delta
            
            if ((i > 1) and tLast.g + tNext.g + tNext.delta <= threshold):
                tNext.g += tLast.g
                newTuples[i-1] = tNext
            else:
                newTuples.append(tNext)
                i += 1
        self.tuples = newTuples

    def add_pairs(self, list val_weight_pairs):
        sorted_vals = sorted(val_weight_pairs, key=lambda x: x[0])
        cdef float threshold = max(self.eps * self.nSize, 1)
        cdef list val_list = []
        cdef tuple vp
        cdef float cur_weight
        for vp in sorted_vals:
            cur_weight = vp[1]
            while cur_weight > threshold:
                cur_weight -= threshold
                val_list.append(Tuple(vp[0], threshold, 0))
            val_list.append(Tuple(vp[0], cur_weight, 0))

        self.mergeInternal(val_list)

    cpdef dict get_dict(self):
        cdef Tuple t
        cdef dict ret_val = dict()
        for t in self.tuples:
            ret_val[t.val] = ret_val.get(t.val, 0) + t.g
        return ret_val
